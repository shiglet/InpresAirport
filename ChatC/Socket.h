#ifndef SOCKET_H
#define SOCKET_H
#include <stdio.h>
#include <errno.h>
#include <stdlib.h>
#include <sys/socket.h>
#include <iostream>
#include <sys/types.h>
#include <netinet/in.h>
#include <netdb.h>
#include <string.h>
#include <unistd.h>
#include <arpa/inet.h>
using namespace std;
class Socket
{
	protected :
		int handler;
		int port;
		struct sockaddr_in adSocket;
	public :
		Socket();
		Socket(string,int);
		~Socket();
		Socket(const Socket &);
		int getHandler()const;
		void setHandler(const int &);
		int getPort()const;
		void setPort(const int);
		virtual void closes();
};

#endif //SOCKET_H
