#include "Network/SocketUtilities.h"
#include "Protocol/CIMP.h"
int cliSocket;
char buffer[BUFFER_SIZE]={0};
string message;
void SendLogin(string l, string p);
bool ThreatLoginResponse(string msg);
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
    // while(1)
    // {
    //     getline(cin,msg);
    //     Log("Sending \""+msg+"\" message",INFO_TYPE);
    //     Send(socket,msg.c_str(),msg.length(),0);
    // }
    do
    {
        Log("Authentification : ");
        cout<<"Login : ";
        cin>>login;
        cout<<"Mot de passe : ";
        cin>>pass;
        SendLogin(login,pass);
        message = Receive(cliSocket);
        if((authenticated = (message == ToString(LOGIN_SUCCESS)+Config.EndTrame)))
            break;
        Log("La combinaison de login/password est incorrecte !",ERROR_TYPE);
    }while(1);
    Log("Authentification réussie.",SUCCESS_TYPE);
    Close(cliSocket);
}
void SendLogin(string l, string p)
{
    string s = ToString(LOGIN_REQUEST)+Config.TrameSeparator+l+Config.TrameSeparator+p+Config.EndTrame;
    Log("Envoie de "+s);
    Send(cliSocket,&s[0],s.length());
}