#include "Network/SocketUtilities.h"
#include "Protocol/CIMP.h"
#define MAX_CLIENTS 5
pthread_mutex_t currentIndexMutex;
pthread_cond_t currentIndexCond;
pthread_t threadHandle[MAX_CLIENTS];

int currentIndex=-1;
void * ThreadFunc(int * p);
int connectedSocket[MAX_CLIENTS] = {-1};
int main()
{
    int listenningSocket, serviceSocket,j = 0;
    struct sockaddr_in socketAddr;
    Log("Server Checkin InpresAirport",INFO_TYPE);

    Log("Reading config file",INFO_TYPE);
    ReadConfigFile();

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
    socketAddr = GetAddr(Config.Host,Config.CheckPort);

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
    using namespace boost;

    string message,ticketNumber;
    vector<string> tempoTokens;
    int treatedClient,socket;
    int state = NON_AUTHENTICATED;

    while(1)
	{
        pthread_mutex_lock(&currentIndexMutex);
        
        while (currentIndex == -1)
            pthread_cond_wait(&currentIndexCond, &currentIndexMutex);

        treatedClient = currentIndex;
        currentIndex = -1;
        socket = connectedSocket[treatedClient];

        pthread_mutex_unlock(&currentIndexMutex);

        state = NON_AUTHENTICATED;
        do
        {
            Log("Waiting for a receive");
            message = Receive(socket);
            vector<std::string> tokens = Tokenize(message);
            tokens.pop_back();
            if(tokens[0]=="") continue;
            switch(atoi(tokens[0].c_str()))
            {
                case LOGIN_OFFICER :
                    if(state != NON_AUTHENTICATED) break;
                    if(CheckLogin(tokens[1],tokens[2]))
                    {
                        Log("Logging success",SUCCESS_TYPE);
                        state = AUTHENTICATED;
                        Send(socket,ToString(LOGIN_SUCCESS)+Config.EndTrame);
                        break;
                    }
                    Log("Logging failed",ERROR_TYPE);
                    Send(socket,ToString(LOGIN_FAILED)+Config.EndTrame);
                    break;
                case LOGOUT_REQUEST : 
                    if(state == NON_AUTHENTICATED) 
                    {
                        Log("User is not authenticated, disconnection failed...",ERROR_TYPE);
                        Send(socket,ToString(LOGOUT_FAILED) + Config.EndTrame);
                        break;
                    }
                    Log("Succcessfully disconnected",SUCCESS_TYPE);
                    Send(socket,ToString(LOGOUT_SUCCESS) + Config.EndTrame);
                    message = "STOP";
                    break;
                case CHECK_TICKET : 
                    if(state != AUTHENTICATED) break;
                    if(CheckTicket(tokens[1],tokens[2]))
                    {
                        ticketNumber = tokens[1];
                        Log("Check ticket success",SUCCESS_TYPE);
                        state = CHECKING;
                        Send(socket,ToString(CHECK_SUCCESS)+Config.EndTrame);
                        break;
                    }
                    Log("Check ticket failed",ERROR_TYPE);
                    Send(socket,ToString(CHECK_FAILED)+Config.EndTrame);

                    break;
                case CHECK_LUGGAGE :
                    {
                        if(state != CHECKING) break;
                        tempoTokens = tokens;
                        float totalWeight = 0.0, exceededWeight = 0.0, toPay = 0.0;
                        for(std::vector<string>::size_type i = 1; i != tokens.size(); i+=2)
                        {
                            float weight = atof(tokens[i].c_str());
                            totalWeight += weight;
                            exceededWeight += weight > 20 ? weight - 20 : 0.0;
                        }
                        toPay = exceededWeight * 2.5;
                        Send(socket,ToString(CHECK_LUGGAGE)+Config.TrameSeparator+ToString(totalWeight)+Config.TrameSeparator+ToString(exceededWeight)+Config.TrameSeparator+ToString(toPay)+Config.EndTrame);
                    }
                    break;
                case PAYMENT_DONE : 
                    {
                        if(state != CHECKING) break;
                        Log("Successfully done payment",SUCCESS_TYPE);
                        int n =1;
                        for(std::vector<string>::size_type i = 1; i != tempoTokens.size(); i+=2)
                        {
                            SaveLuggage(n,ticketNumber,tempoTokens[i+1]);
                            n++;
                        }
                        state = AUTHENTICATED;
                        tempoTokens.clear();
                    }
                    break;
                case PAYMENT_CANCELED :
                    if(state != CHECKING) break;
                    Log("Payement canceled",ERROR_TYPE);
                    state = AUTHENTICATED;
                    break;
                default : 
                    Log("Error Request type doesn't exist : "+tokens[0],ERROR_TYPE);
                    break;
            }
        }while(message!="Stop" && message.length()!=0);
        pthread_mutex_lock(&currentIndexMutex);
		connectedSocket[treatedClient] = -1;
        pthread_mutex_unlock(&currentIndexMutex);
        
        Log("Closing socket ",INFO_TYPE);
        Close(socket);
    }
    return 0;
}