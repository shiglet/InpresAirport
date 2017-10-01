#include "Network/SocketUtilities.h"
#include "Protocol/CIMP.h"
int cliSocket;
char buffer[BUFFER_SIZE]={0};
string ticketNumber, passager,message;
void SendLogin(string l, string p);
void TreatLogout(string msg);
void TreatLuggages(int);
void TreatWeight(string message);
int DisplayMenu();
int main()
{
    ReadConfigFile();
    struct sockaddr_in socketAddr;
    Log("Server Checkin InpresAirport",INFO_TYPE);
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
            if(!authenticated)
            {
                Log("Creating socket :",INFO_TYPE);    
                cliSocket = CreateSocket();
            
                Log("Getting address informations",INFO_TYPE);
                socketAddr = GetAddr(Config.Host,Config.CheckPort);
            
                Log("Connecting to the server",INFO_TYPE);
                Connect(socketAddr,cliSocket);
            }
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
                authenticated = false;
                Send(cliSocket,ToString(LOGOUT_REQUEST)+Config.EndTrame);
                TreatLogout(Receive(cliSocket));
                break;
            case 1 :
            //Check ticket
            {
                cout<<"Numéro de billet ?"+Config.FlyNumber;
                cin>>ticketNumber;
                ticketNumber = Config.FlyNumber + ticketNumber;
                cout<<"Nombre d'accompagnants ?";
                cin>>passager;
                Send(cliSocket,ToString(CHECK_TICKET)+Config.TrameSeparator+ticketNumber+Config.TrameSeparator+passager+Config.EndTrame);
                message = Receive(cliSocket);
                if(message == ToString(CHECK_SUCCESS)+Config.EndTrame)
                {
                    cout<<"Le billet est correcte, encodage des/du baggage(s) ..."<<endl;

                    TreatLuggages(atoi(passager.c_str()));
                    message = Receive(cliSocket);
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
    Send(cliSocket,ToString(LOGOUT_REQUEST)+Config.EndTrame);
    TreatLogout(Receive(cliSocket));    
}

void SendLogin(string l, string p)
{
    string s = ToString(LOGIN_OFFICER)+Config.TrameSeparator+l+Config.TrameSeparator+p+Config.EndTrame;
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
void TreatLuggages(int passager)
{
    string poids,valise,msg;
    msg+=ToString(CHECK_LUGGAGE);
    for(int i=0;i<passager;i++)
    {
        cout<<"Poids du baggages n°"+ToString(i+1)+" : ";
        cin>>poids;
        cout<<"Valise ? : ";
        cin>>valise;
        msg+= Config.TrameSeparator+poids+Config.TrameSeparator+valise;
    }
    msg+= Config.EndTrame;
    Send(cliSocket, msg);
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
        Send(cliSocket,ToString(PAYMENT_DONE)+Config.EndTrame);
    }
    else if(pay=="N")
    {
        Log("Payement annulé",ERROR_TYPE);
        Send(cliSocket,ToString(PAYMENT_CANCELED)+Config.EndTrame);
    }
}