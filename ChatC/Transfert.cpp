#include "Transfert.h"
Transfert::Transfert(Socket & s)
{
	setSocket(s);
}
Transfert::Transfert(int hSocket)
{
	socket.setHandler(hSocket);
}
Transfert::Transfert()
{

}
Transfert::~Transfert()
{
	//cout<<"Destructeur de Transfert"<<endl;
}
int Transfert::read(char * msg)
{
	int nbr=0;
	/*
	if((nbr =recv(socket.getHandler(),msg,100,0))<0)
	{
		cout<<"Erreur de recv "<<errno<<endl;
	}*/
	return nbr;
}
char * Transfert::read()const
{
	/*char * msg = (char*)malloc(100);
	int nbr=0;
	if((nbr =recv(socket.getHandler(),msg,100,0))<0)
	{
		cout<<"Erreur de recv "<<errno<<endl;
	}
	return msg;*/
	char msgLong[1024]={0};
	//char * msg = (char*)malloc(100);
	char b[2]={0};
	int nbr=0;
	int i=0;
	do
	{
		if((nbr =recv(socket.getHandler(),b,1,0))<0)
		{
			cout<<"Erreur de recv "<<errno<<endl;
		}
		else
		{
			strcat(msgLong,b);
			i++;
		}
	}while(strcmp(b,"|")!=0 && nbr>0);
	msgLong[i]='\0';
	char * msg = (char*)malloc(strlen(msgLong));
	strcpy(msg,msgLong);
	//cout<<"Message Lu ("<<i<<") : "<<msg<<endl;
	return msg;
}
int Transfert::write(const char * msg)
{
	int nbr=0;
	if((nbr=send(socket.getHandler(),msg,strlen(msg),0))<0)
	{
		cout<<"Erreur sur le send !!"<<errno<<endl;
		return -1;
	}
	return nbr;
}
Socket Transfert::getSocket()const
{
	return socket;
}
void Transfert::setSocket(const Socket & s)
{
	socket.setHandler(s.getHandler());
	socket.setPort(s.getPort());
}