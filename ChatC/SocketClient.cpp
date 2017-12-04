#include "SocketClient.h"

SocketClient::SocketClient() : Socket()
{
	struct hostent * infosHost;
	unsigned int taille=sizeof(struct sockaddr_in);
	if((infosHost = gethostbyname("ubuntu"))==0)
	{
		cout<<"Erreur d'acquis"<<endl;
		exit(0);
	}
	cout<<"Acquisition OK !!"<<endl;
	//préparation de la structure sockaddr_in
	memset(&adSocket,0,taille);
	adSocket.sin_family = AF_INET;
	adSocket.sin_port = htons(20000);
	memcpy(&adSocket.sin_addr,infosHost->h_addr,infosHost->h_length);
	if(connect(handler,(struct sockaddr *)&adSocket,taille)==-1)
	{
		cout<<"Erreur lors du connect : "<<errno<<endl;
		close(handler);
		exit(0);
	}
}

SocketClient::SocketClient(string n, int p) : Socket()
{
	setPort(p);
	setNomServeur(n);
	struct hostent * infosHost;
	unsigned int taille=sizeof(struct sockaddr_in);
	/*if((infosHost = gethostbyname(getNomServeur().c_str()))==0)
	{
		cout<<"Erreur d'acquis"<<endl;
		exit(0);
	}
	cout<<"Acquisition OK !!"<<endl;*/
	//préparation de la structure sockaddr_in
	memset(&adSocket,0,taille);
	adSocket.sin_family = AF_INET;
	adSocket.sin_port = htons(getPort());
	//memcpy(&adSocket.sin_addr,infosHost->h_addr,infosHost->h_length);
	adSocket.sin_addr.s_addr=inet_addr(n.c_str());
	if(connect(handler,(struct sockaddr *)&adSocket,taille)==-1)
	{ 
		cout<<"Erreur lors du connect : "<<errno<<endl;
		close(handler);
		exit(0);
	}
}

SocketClient::~SocketClient()
{
}

string SocketClient::getNomServeur()const
{
	return nomServeur;
}
void SocketClient::setNomServeur(const string n)
{
	nomServeur = n;
}