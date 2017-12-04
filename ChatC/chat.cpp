#include <iostream>
#include <unistd.h>
#include <time.h>
#include <stdio.h>
#include <stdlib.h> /* pour exit */
#include <string.h> /* pour memcpy */
#include <sys/types.h>
#include <sys/socket.h> /* pour les types de socket */
#include <sys/time.h> /* pour les types de socket */
#include <netdb.h> /* pour la structure hostent */
#include <errno.h>
#include <netinet/in.h> /* pour la conversion adresse reseau->format dot
ainsi que le conversion format local/format
reseau */
#include <netinet/tcp.h>
#include "Network/SocketUtilities.h"
#include <arpa/inet.h> /* pour la conversion adresse reseau->format dot */
#define LOGIN_GROUP 1
#define LOGIN_OK 2
#define LOGIN_NOK 3
#define MAXSTRING 256
#define LEAVE 5
#define JOIN 1
#define POST_QUESTION 2
#define ANSWER_QUESTION 3
#define POST_EVENT 4

using namespace std;
int hashage(const char *,int);
pthread_t ThrReception;
void * receiveThr(void *);
void SendLogin(string, string,string);
int portMulti=0;
string AdresseMulti;
char login[50];
int cliSocket;
int main()
{
	char mdp[50];
	char msg[MAXSTRING];
	char * tok = NULL;
	struct sockaddr_in socketAddr;
	string message;
	bool authenticated =false;
	vector<string> tokens;
	ReadConfigFile();
	do
	{
		cout<<"Veuillez vous identifier."<<endl<<"Login : ";
		cin>>login;
		cout<<"Mot de passe : ";
		cin>>mdp;
		Log("Creating socket :",INFO_TYPE);    
		cliSocket = CreateSocket();
	
		Log("Getting address informations",INFO_TYPE);
		socketAddr = GetAddr(Config.ChatIP,Config.ChatPort);
	
		Log("Connecting to the server",INFO_TYPE);
		Connect(socketAddr,cliSocket);
		int r = rand() % 10000;
		int p = hashage(mdp,r);
		SendLogin(login,ToString(p),ToString(r));
		message = Receive(cliSocket);
		Close(cliSocket);
		tokens = Tokenize(message);
		if(tokens[0] == ToString(LOGIN_OK))
		{
			authenticated = true;
		}
	}while(!authenticated);
	
	AdresseMulti = tokens[1];
	cout<<"Adresse multicast recupérée : "<<AdresseMulti<<endl;
		
	portMulti=atoi(tokens[2].c_str());
		
	cout<<"Port multicast recupéré : "<<portMulti<<endl;




	pthread_create(&ThrReception,NULL,(void*(*)(void*))receiveThr, NULL);
	bool fini=false;
	struct sockaddr_in addr;
	int fd;

	if ((fd=socket(AF_INET,SOCK_DGRAM,0)) < 0) 
	{
		perror("socket");
		exit(1);
	}

	/* set up destination address */
	memset(&addr,0,sizeof(addr));
	addr.sin_family=AF_INET;
	addr.sin_addr.s_addr=inet_addr(AdresseMulti.c_str());
	addr.sin_port=htons(portMulti);
	message = ToString(JOIN)+Config.TrameSeparator+"0"+Config.TrameSeparator+login+Config.TrameSeparator+login+" a rejoint le groupe";
	do
	{
		if(strcmp("eos",message.c_str())==0)
			fini=true;
		else
		{
		/* now just sendto() our destination! */
			if (sendto(fd,message.c_str(),message.size(),0,(struct sockaddr *) &addr,sizeof(addr)) < 0) 
			{
			   perror("sendto");
			   exit(1);
			}
		}
		getline(cin,message);
		//printf("\r%c[2K", 27);
		if((tok = strtok(&message[0],">>")))
		{
			if(strcmp(tok,"Q")==0)
			{
				srand(time(NULL));
				int tag = rand()%50001;
				int type = POST_QUESTION;
				if((tok = strtok(NULL,">>")))
				{
					strcpy(msg,tok);
					int r = rand() % 10000;
					int messageH = hashage(msg,r);
					message = msg;
					message = ToString(type)+Config.TrameSeparator+"Q"+ToString(tag)+Config.TrameSeparator+login+Config.TrameSeparator+message+Config.TrameSeparator+ToString(messageH)+Config.TrameSeparator+ToString(r);
				}
			}
			else if(strcmp(tok,"E")==0)
			{
				srand(time(NULL));
				int tag = rand()%50001;
				int type = POST_EVENT;
				if((tok = strtok(NULL,">>")))
				{
					strcpy(msg,tok);
					//int messageH = hashage(msg);
					message = msg;
					message = ToString(type)+Config.TrameSeparator+"E"+ToString(tag)+Config.TrameSeparator+login+Config.TrameSeparator+message;
				}
			}
			else if(strcmp(tok,"R")==0)
			{
				if((tok = strtok(NULL,">>")))
				{
					int tag = atoi(tok);
					int type = ANSWER_QUESTION;
					if((tok = strtok(NULL,">>")))
					{
						strcpy(msg,tok);
						//int messageH = hashage(msg);
						message = msg;
						message = ToString(type)+Config.TrameSeparator+"Q"+ToString(tag)+Config.TrameSeparator+login+Config.TrameSeparator+message;
					}
				}
			}
		}


	}while(!fini);

	



	return 0;
}
void SendLogin(string l, string p,string r)
{
    vector<string> vMessage{l,p,r};
    Send(cliSocket,CreateMessage(LOGIN_GROUP,vMessage));
}
int hashage(const char * c,int r)
{
	int len = strlen(c);
	int i =0;
	int sum=0;
    for (i = 0; i < len; i++)
    {
        sum = sum + c[i];
    }
    sum =(sum + r % 67)*r;
    return sum;
}
void * receiveThr(void * p)
{
	char msg[MAXSTRING];
	int hSocket; /* Handle de la socket */
	struct sockaddr_in adresseSocketServeur,adresseSocketClient;;
	unsigned int tailleSockaddr_in;
	int nbreRecv;
	struct ip_mreq mreq;
	/* 1. Creation de la socket */
	hSocket = socket(AF_INET, SOCK_DGRAM, 0);
	if (hSocket == -1)
	{
		cout<<"Erreur de creation de la socket "<<errno<<endl;
		exit(1);
	}
	else cout<<"Creation de la socket OK"<<endl;
	u_int yes=1;  
	 /* allow multiple sockets to use the same PORT number */
    if (setsockopt(hSocket,SOL_SOCKET,SO_REUSEADDR,&yes,sizeof(yes)) < 0) {
       perror("Reusing ADDR failed");
       exit(1);
       }
	/* 3. Preparation de la structure sockaddr_in du serveur */
	tailleSockaddr_in = sizeof(struct sockaddr_in);
	memset(&adresseSocketServeur, 0, tailleSockaddr_in);
	adresseSocketServeur.sin_family = AF_INET;
	adresseSocketServeur.sin_port = htons(portMulti);
	//adresseSocketServeur.sin_addr.s_addr = inet_addr(AdresseMulti);
	adresseSocketServeur.sin_addr.s_addr = htonl(INADDR_ANY);
	cout<<"adresseSocket prete"<<endl;
	/* 4. Le systeme prend connaissance de l'adresse et du port de la socket */
	if (bind(hSocket, (struct sockaddr *)&adresseSocketServeur,tailleSockaddr_in) == -1)
	{
		cout<<"Erreur sur le bind de la socket "<<errno<<endl; 
		exit(1);
	}
	else cout<<"Bind adresse et port socket OK"<<endl;
	/* 5. Parametrage de la socket */
	//memcpy(&mreq.imr_multiaddr, &adresseSocketServeur.sin_addr,tailleSockaddr_in);
	mreq.imr_multiaddr.s_addr = inet_addr(AdresseMulti.c_str());
	mreq.imr_interface.s_addr = htonl(INADDR_ANY);
	//mreq.imr_interface.s_addr = inet_addr("192.168.136.132");
	cout<<"Utilisation de l'adresse "<<inet_ntoa(mreq.imr_interface)<<endl;
	setsockopt (hSocket, IPPROTO_IP, IP_ADD_MEMBERSHIP, &mreq,sizeof(mreq));
	//char msgTemp[MAXSTRING];
	char * tok= NULL;
	char * sep = &(Config.TrameSeparator);
	do
	{
		/* 6.Reception d'un message serveur */
		memset(msg, 0, MAXSTRING);
		if ((nbreRecv = recvfrom(hSocket, msg, MAXSTRING, 0,(struct sockaddr *)&adresseSocketClient,&tailleSockaddr_in)) == -1)
		{
			cout<<"Erreur sur le recvfrom de la socket "<<errno<<endl;
			close(hSocket); /* Fermeture de la socket */
			exit(1);
		}
		msg[nbreRecv+1]=0;
		//strcpy(msgTemp,msg);
		int type=-1;
		char tag[25] = {0};
		char pseudo[25] ={0};
		if((tok=strtok(msg,sep)))
		{
			type = atoi(tok);
		}
		int hash=0,r;
		switch(type)
		{
			case POST_EVENT : 
				if((tok=strtok(NULL,sep)))
				{
					strcpy(tag ,tok);
				}
				if((tok=strtok(NULL,sep)))
				{
					strcpy(pseudo,tok);
				}
				if((tok=strtok(NULL,sep)))
				{
					strcpy(msg, tok);
				}
				cout<<tag<<"#"<<pseudo<<" : "<<msg<<endl;
			break;

			case POST_QUESTION : 
				if((tok=strtok(NULL,sep)))
				{
					strcpy(tag ,tok);
				}
				if((tok=strtok(NULL,sep)))
				{
					strcpy(pseudo,tok);
				}
				if((tok=strtok(NULL,sep)))
				{
					strcpy(msg, tok);
				}
				if((tok=strtok(NULL,sep)))
				{
					hash = atoi(tok);
				}
				if((tok=strtok(NULL,sep)))
				{
					r = atoi(tok);
				}
				if(hashage(msg,r) == hash)
					cout<<tag<<"#"<<pseudo<<" : "<<msg<<endl;
			break;
			case ANSWER_QUESTION :
				if((tok=strtok(NULL,sep)))
				{
					strcpy(tag ,tok);
				}
				if((tok=strtok(NULL,sep)))
				{
					strcpy(pseudo,tok);
				}
				if((tok=strtok(NULL,sep)))
				{
					strcpy(msg, tok);
				}
				cout<<"To "<<tag<<"#"<<pseudo<<" : "<<msg<<endl;

			break;

		}
	}while (strcmp(msg, "Arret du chat !"));
	return 0;
}