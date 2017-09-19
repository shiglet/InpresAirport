#include "Network/SocketUtilities.h"
#include "Utils/Utilities.h"
int sHandler;
int main()
{
    int socket = 0;
    struct sockaddr_in socketAddr;
    Log("Server Checkin InpresAirport",INFO_TYPE);

    Log("Creating socket :",INFO_TYPE);    
    socket = CreateSocket();

    Log("Getting address informations",INFO_TYPE);
    socketAddr = GetAddr("192.168.40.128",5000);

    Log("Connecting to the server",INFO_TYPE);
    Connect(socketAddr,socket);

    string msg;
    while(1)
    {
        getline(cin,msg);
        Log("Sending \""+msg+"\" message",INFO_TYPE);
        Send(socket,msg.c_str(),msg.length(),0);
    }
}