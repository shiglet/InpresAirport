#ifndef SOCKETCLIENT_H
#define SOCKETCLIENT_H
#include "Socket.h"
using namespace std;
class SocketClient : public Socket
{
	private :
		string nomServeur;
	public :
		SocketClient();
		~SocketClient();
		SocketClient(string , int );
		string getNomServeur()const;
		void setNomServeur(const string);
		
};

#endif //SOCKETCLIENT_H
