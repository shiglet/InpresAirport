#include "Network/SocketUtilities.h"
int sHandler;

int main()
{
    ReadConfigFile();
    Log(Config.Host,ERROR_TYPE);
    int socket = 0;
    struct sockaddr_in socketAddr;
    Log("Server Checkin InpresAirport",INFO_TYPE);

    Log("Creating socket :",INFO_TYPE);    
    socket = CreateSocket();

    Log("Getting address informations",INFO_TYPE);
    socketAddr = GetAddr(Config.Host,Config.CheckPort);

    Log("Connecting to the server",INFO_TYPE);
    Connect(socketAddr,socket);

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
        if((authenticated = CheckLogin(login,pass)))
            break;
        Log("La combinaison de login/password est incorrecte !",ERROR_TYPE);
    }while(1);
    
}