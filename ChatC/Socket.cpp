#include "Socket.h"

Socket::Socket()
{
	//Création de la socket
	handler = socket(AF_INET,SOCK_STREAM,0);
	if(handler==-1)
	{
		cout<<"Erreur de socket()"<<endl;
		exit(0);
	}
}
Socket::~Socket()
{
		//cout<<"Destructeur Socket"<<endl;
}
Socket::Socket(const Socket& s)
{
	setHandler(s.getHandler());
}
int Socket::getHandler()const
{
	return handler;
}
int Socket::getPort()const
{
	return port;
}
void Socket::setPort(const int p)
{
	port = p;
}
void Socket::closes()
{
	cout<<"Fermeture des sockets"<<endl;
	close(handler);
}
void Socket::setHandler(const int & h)
{
	handler= h;
}