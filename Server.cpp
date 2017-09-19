#include "Network/SocketUtilities.h"
#include "Utils/Utilities.h"
int main()
{
    int listenningSocket, serviceSocket = 0;
    char buffer[500]={};
    struct sockaddr_in socketAddr;
    Log("Server Checkin InpresAirport",INFO_TYPE);

    Log("Creating socket :",INFO_TYPE);    
    listenningSocket = CreateSocket();

    Log("Getting address informations",INFO_TYPE);
    socketAddr = GetAddr("192.168.40.128",5000);

    Log("Binding socket",INFO_TYPE);
    Bind(socketAddr,listenningSocket);

    Log("Start listenning for client",INFO_TYPE);
    Listen(listenningSocket,5);

    Log("Waiting for client connection",INFO_TYPE);
    serviceSocket = Accept(socketAddr, listenningSocket);
    
    while(1)
    {
        Log("Receiving a message",INFO_TYPE);
        Receive(serviceSocket,&buffer,sizeof(buffer),0);
        string m = string(buffer);
        memset(&buffer, 0, sizeof(buffer));
        Log("Message received : "+m+" length : "+ToString(m.length()),INFO_TYPE);
    }
}