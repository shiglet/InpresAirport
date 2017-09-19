#include "Network/SocketUtilities.h"
#include "Utils/Utilities.h"
#define MAX_CLIENTS 5
#define BUFFER_SIZE 500
pthread_mutex_t currentIndexMutex;
pthread_cond_t currentIndexCond;
pthread_t threadHandle[MAX_CLIENTS];

int currentIndex=-1;
void * ThreadFunc(int * p);
int connectedSocket[MAX_CLIENTS] = {-1};

int main()
{
    int listenningSocket, serviceSocket,j = 0;
    //char buffer[BUFFER_SIZE]={};
    struct sockaddr_in socketAddr;
    Log("Server Checkin InpresAirport",INFO_TYPE);

    pthread_mutex_init(&currentIndexMutex, NULL);  
	pthread_cond_init(&currentIndexCond, NULL); 
	for (int i=0; i<MAX_CLIENTS; i++) 
        connectedSocket[i] = -1; 
    for (int i=0; i<MAX_CLIENTS; i++)  
    {   
        if(pthread_create(&threadHandle[i],NULL,(void*(*)(void*))ThreadFunc,&i) ==0)
        {  
            Log("Secondary Thread number "+ ToString(i) +" launched !",INFO_TYPE);  
            pthread_detach(threadHandle[i]); 
        }
        else
        {
            Log("Error while creating threads ...",ERROR_TYPE);
            exit(-1);
        }
    }

    Log("[Main Thread] Creating socket :",INFO_TYPE);    
    listenningSocket = CreateSocket();

    Log("[Main Thread] Getting address informations",INFO_TYPE);
    socketAddr = GetAddr("192.168.40.128",5000);

    Log("[Main Thread] Binding socket",INFO_TYPE);
    Bind(socketAddr,listenningSocket);
    
    do
    {
        Log("[Main Thread] Start listenning for client",INFO_TYPE);
        Listen(listenningSocket,5);
    
        Log("[Main Thread] Accepting client connection",INFO_TYPE);
        serviceSocket = Accept(socketAddr, listenningSocket);

        for (j=0; j < MAX_CLIENTS && connectedSocket[j] != -1; j++);

        if (j == MAX_CLIENTS)
        {
            Log("[Main Thread] No more avalaible thread",ERROR_TYPE);
            
            Send(serviceSocket, "No more connection avalaible", BUFFER_SIZE, 0);
            Close(serviceSocket);
        }
        else
        {
            /* Il y a une connexion de libre */
            printf("Connexion sur la socket num. %d\n", j);
            pthread_mutex_lock(&currentIndexMutex);
            connectedSocket[j] = serviceSocket;
            currentIndex = j;
            pthread_mutex_unlock(&currentIndexMutex);
            pthread_cond_signal(&currentIndexCond);
        }
    }while(1);
}
void * ThreadFunc(int * p)
{
    char buffer[BUFFER_SIZE]={};
    string msg;
    int treatedClient,socket;

    while(1)
	{
        pthread_mutex_lock(&currentIndexMutex);
        
        while (currentIndex == -1)
            pthread_cond_wait(&currentIndexCond, &currentIndexMutex);

        treatedClient = currentIndex;
        currentIndex = -1;
        socket = connectedSocket[treatedClient];

        pthread_mutex_unlock(&currentIndexMutex);
        do
        {
            Log("Receiving a message",INFO_TYPE);
            Receive(socket,&buffer,sizeof(buffer),0);
            msg = string(buffer);
            memset(&buffer, 0, sizeof(buffer));
            Log("Message received : "+msg+" length : "+ToString(msg.length()),INFO_TYPE);
        }while(msg!="Stop");
        pthread_mutex_lock(&currentIndexMutex);
		connectedSocket[treatedClient] = -1;
        pthread_mutex_unlock(&currentIndexMutex);
        
        Log("Closing socket ",INFO_TYPE);
        Close(socket);
    }
    return 0;
}