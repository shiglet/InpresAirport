#ifndef TRANSFERT_H
#define TRANSFERT_H
#include "SocketClient.h"
using namespace std;
class Transfert
{
	private :
		Socket socket;
	public :
		Transfert(Socket&);
		Transfert();
		~Transfert();
		Transfert(int hSocket);
		int read(char *);
		char * read()const;
		int write(const char *);
		Socket getSocket()const;
		void setSocket(const Socket &);
		
};

#endif //Transfert_H
