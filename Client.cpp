#include "Network/SocketUtilities.h"
#include "Protocol/CIMP.h"
int cliSocket;
char buffer[BUFFER_SIZE]={0};
string message;
void SendLogin(string l, string p);
void TreatLogout(string msg);
int DisplayMenu();
int main()
{
    ReadConfigFile();
    Log(Config.Host,ERROR_TYPE);
    struct sockaddr_in socketAddr;
    Log("Server Checkin InpresAirport",INFO_TYPE);

    Log("Creating socket :",INFO_TYPE);    
    cliSocket = CreateSocket();

    Log("Getting address informations",INFO_TYPE);
    socketAddr = GetAddr(Config.Host,Config.CheckPort);

    Log("Connecting to the server",INFO_TYPE);
    Connect(socketAddr,cliSocket);

    string msg,login,pass;
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
            SendLogin(login,pass);
            message = Receive(cliSocket);
            if((authenticated = (message == ToString(LOGIN_SUCCESS)+Config.EndTrame)))
            {
                Log("Authentification réussie.",SUCCESS_TYPE);
                break;
            }
            Log("La combinaison de login/password est incorrecte !",ERROR_TYPE);
        }
        choix = DisplayMenu();
        switch(choix)
        {
            case 0 :
            //Disconnect 
                continue;
                break;
            case 1 :
            //Check ticket
            {
                string ticketNumber, count;

                cout<<"Numéro de billet ?";
                cin>>ticketNumber;
                cout<<"Nombre d'accompagnants ?";
                cin>>count;
                Send(cliSocket,ToString(CHECK_TICKET)+Config.TrameSeparator+ticketNumber+Config.TrameSeparator+count+Config.EndTrame);
                message = Receive(cliSocket);
                break;
            }
            case 2 :
            //Exit
                Send(cliSocket,ToString(LOGOUT_REQUEST)+Config.EndTrame);
                TreatLogout(Receive(cliSocket));
                exit(0);
                break;
        }
    }while(choix!=0);

    Send(cliSocket,ToString(LOGOUT_REQUEST)+Config.EndTrame);
    TreatLogout(Receive(cliSocket));
}

void SendLogin(string l, string p)
{
    string s = ToString(LOGIN_OFFICER)+Config.TrameSeparator+l+Config.TrameSeparator+p+Config.EndTrame;
    Log("Envoie de "+s);
    Send(cliSocket,&s[0],s.length());
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