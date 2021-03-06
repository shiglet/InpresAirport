#include "Network/SocketUtilities.h"
#include "Protocol/CIMP.h"
int cliSocket;
char buffer[BUFFER_SIZE]={0};
string ticketNumber, passager,message;
void SendLogin(string l, string p);
void TreatLogout(string msg);
void TreatLuggages(int);
void TreatWeight(string message);
pthread_mutex_t messageReceived;
int DisplayMenu();
pthread_t threadReception;
void * ThreadFunc(int * p);
pthread_cond_t received;
int main()
{
    ReadConfigFile();
    struct sockaddr_in socketAddr;
    int x=0;
    Log("Server Checkin InpresAirport",INFO_TYPE);
    string msg,login,pass;
    vector<string> vMessage;
    
	pthread_cond_init(&received, NULL); 
    pthread_mutex_init(&messageReceived, NULL); 
    pthread_create(&threadReception,NULL,(void*(*)(void*))ThreadFunc,&x);
    bool authenticated = false;
    int choix;
    do
    {
        while(!authenticated)
        {
            Log("Authentification : ");
            cout<<"Login : ";
            cin>>login;
            cout<<"Mot de passe : ";
            cin>>pass;
            if(!authenticated)
            {
                Log("Creating socket :",INFO_TYPE);    
                cliSocket = CreateSocket();
            
                Log("Getting address informations",INFO_TYPE);
                socketAddr = GetAddr(Config.Host,Config.CheckPort);
            
                Log("Connecting to the server",INFO_TYPE);
                Connect(socketAddr,cliSocket);
            }
            cout<<"Send login"<<endl;
            SendLogin(login,pass);
            cout<<"Waiting server response"<<endl;
            message = Receive(cliSocket);           
            cout<<"Server response received"<<endl;
            if((authenticated = (message == ToString(LOGIN_SUCCESS)+Config.EndTrame)))
            {
                Log("Authentification réussie.",SUCCESS_TYPE);

                pthread_create(&threadReception,NULL,(void*(*)(void*))ThreadFunc,&x);
                break;
            }
            Log("La combinaison de login/password est incorrecte !",ERROR_TYPE);
            Log("Closing socket",INFO_TYPE);
            Close(cliSocket);
        }
        choix = DisplayMenu();
        switch(choix)
        {
            case 0 :
            //Disconnect 
                authenticated = false;
                Send(cliSocket,CreateMessage(LOGOUT_REQUEST));
                pthread_cond_wait(&received, &messageReceived);                            
                TreatLogout(message);
                break;
            case 1 :
            //Check ticket
            {
                cout<<"Numéro de billet ?";
                cin>>ticketNumber;
                ticketNumber = ticketNumber;
                cout<<"Nombre d'accompagnants ?";
                cin>>passager;
                vMessage = {ticketNumber,passager};
                Send(cliSocket,CreateMessage(CHECK_TICKET,vMessage));
                pthread_cond_wait(&received, &messageReceived);
                if(message == ToString(CHECK_SUCCESS)+Config.EndTrame)
                {
                    cout<<"Le billet est correcte, encodage des/du baggage(s) ..."<<endl;

                    TreatLuggages(atoi(passager.c_str()));
                    pthread_cond_wait(&received, &messageReceived);                                
                    TreatWeight(message);
                }
                else
                {
                    cout<<"Billet incorrece ou déjà checké"<<endl;
                }
                break;
            }
        }
    }while(choix!=2);
    Send(cliSocket,CreateMessage(LOGOUT_REQUEST));
    pthread_cond_wait(&received, &messageReceived);                
    TreatLogout(message);    
}

void SendLogin(string l, string p)
{
    vector<string> vMessage{l,p};
    Send(cliSocket,CreateMessage(LOGIN_OFFICER,vMessage));
}

void TreatLogout(string msg)
{
    vector<string> tokens = Tokenize(msg);
    if(ToString(LOGOUT_SUCCESS) == tokens[0])
    {
        Log("Déconnexion réussie avec succès",SUCCESS_TYPE);
    }
    else
    {
        Log("Problème lors de la déconnexion : "+tokens[1]);
    }
    Log("Closing socket",INFO_TYPE);
    Close(cliSocket);
}

int DisplayMenu()
{
    int choix;
    cout<<"-----------------------------------------------"<<endl;
    cout<<"\(1)Check ticket"<<endl;
    cout<<"\(2)Exit"<<endl;

    cout<<endl<<"(0)Logout"<<endl;
    cout<<"-----------------------------------------------"<<endl;

    do
    {
        cout<<"Choix : ";
        cin>>choix;
    }while(choix<0 || choix>2);
    return choix;
}
void TreatLuggages(int passager)
{
    vector<string> vMessage;
    string poids,valise;
    for(int i=0;i<passager;i++)
    {
        cout<<"Poids du baggages n°"+ToString(i+1)+" : ";
        cin>>poids;
        vMessage.push_back(poids);
        cout<<"Valise ? : ";
        cin>>valise;
        vMessage.push_back(valise);
    }
    Send(cliSocket, CreateMessage(CHECK_LUGGAGE,vMessage));
}
void TreatWeight(string message)
{
    vector<string> tokens = Tokenize(message);
    string pay;
    tokens.pop_back();
    cout<<"Numéro de billet : "+ticketNumber<<endl;
    cout<<"Nombre d'accompagnants : "+passager<<endl;
    cout<<"Poids total bagages : "+tokens[1]<<endl;
    cout<<"Excédent poids : "+tokens[2]<<"kg"<<endl;
    cout<<"Supplément à payer : "+tokens[3]+" EUR"<<endl;
    cout<<"Paiement effectué ? ";cin>>pay;
    if(pay=="Y")
    {
        Log("Payement effectué avec succés",SUCCESS_TYPE);
        Send(cliSocket,CreateMessage(PAYMENT_DONE));
    }
    else if(pay=="N")
    {
        Log("Payement annulé",ERROR_TYPE);
        Send(cliSocket,CreateMessage(PAYMENT_CANCELED));
    }
}
void * ThreadFunc(int * p)
{
    while(1)
    {
        if(cliSocket!=0)
        {
            message = Receive(cliSocket);
            if(message == ToString(1000)+Config.EndTrame)
            {
                cout<<"*** FIN DES OPERATIONS DE CHECK-IN ! ***"<<endl;
                Close(cliSocket);
                exit(0);
            }
            else
            {
                pthread_cond_signal(&received);
            }
        }
    }
}