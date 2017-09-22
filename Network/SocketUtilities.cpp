#include "SocketUtilities.h"
//Client
void Connect(struct sockaddr_in addr , int sHandler)
{
	int size = sizeof(struct sockaddr_in);
	
	if (connect(sHandler, (struct sockaddr *) &addr, size) == -1)
	{
		string error = "Error while closing socket : ";

		switch(errno)
		{
			case EBADF :
				error += "EBADF - hsocket doesn't exitst";
				break;
			default : 
				error+= "Unknown error. Errno = " + ToString(errno);
		}
		Log(error,ERROR_TYPE);
		Close(sHandler);
		exit(-1);
	}
	Log("Successfull connect to the server",SUCCESS_TYPE);
}

struct sockaddr_in GetAddr(string host ,int port)
{
	struct hostent * infosHost;
	struct in_addr ipAddress; /* Adresse Internet au format reseau */
	struct sockaddr_in socketAddress;
	/* 2-3. Acquisition des informations sur l'ordinateur local */
	if ((infosHost = gethostbyname(host.c_str())) == 0)
	{
		Log("Error while getting host by name. Errno = "+ToString(errno), ERROR_TYPE);
		exit(-1);
	}
	Log("Successfully get host Info",SUCCESS_TYPE);

	memcpy(&ipAddress, infosHost->h_addr, infosHost->h_length);
	string sAddress = inet_ntoa(ipAddress);
	Log("IP address = " + sAddress ,INFO_TYPE);

	/* 3-4. Préparation de la structure sockaddr_in */
	memset(&socketAddress, 0, sizeof(struct sockaddr_in));
	socketAddress.sin_family = AF_INET; /* Domaine */
	socketAddress.sin_port = htons(port);
	memcpy(&socketAddress.sin_addr, infosHost->h_addr,infosHost->h_length);

	return socketAddress;
}


void Bind(struct sockaddr_in socketAddress ,int hSocket )
{
	/* 5. Le syst�me prend connaissance de l'adresse et du port de la socket */
	if (bind(hSocket, (struct sockaddr *) &socketAddress, sizeof(struct sockaddr_in)) == -1)
	{
		Log("Error while binding socket. Errno = "+ToString(errno),ERROR_TYPE);
		Close(hSocket);
		exit(-1);
	}
	Log("Successfully binded socket to address",SUCCESS_TYPE);
}

void Listen(int hSocket, int flag)
{
	if (listen(hSocket, flag) == -1)
	{
		Log("Error while listening for client. Errno = "+ToString(errno),ERROR_TYPE);
		Close(hSocket);
		exit(-1);
	}
	Log("A client is connected after listenning",SUCCESS_TYPE);
}
int Accept(struct sockaddr_in socketAddress, int listenningSocket )
{
	int sizeSockAddr = sizeof(struct sockaddr_in);
	int serviceSocket = accept(listenningSocket, (struct sockaddr *) &socketAddress, (socklen_t*) &sizeSockAddr) ;
	if (serviceSocket == -1)
	{
		Log("Error on accept of the socket. Errno = "+ToString(errno),ERROR_TYPE);
		Close(listenningSocket);
		exit(-1);
	}
	Log("Successfully accepted a client connection",SUCCESS_TYPE);

	return serviceSocket;
}

int Send(int hSocket ,const void * data,int size, int flag)
{
	int ret = send(hSocket, data, size, flag);
	if (ret == -1)
	{
		Log("Error while trying to send data" + ToString(errno),ERROR_TYPE);
		Close(hSocket);
		exit(-1);
	}

	return ret;
}

int Send(int hSocket ,string data, int flag)
{
	int ret = send(hSocket, data.c_str(), data.length(), flag);
	if (ret == -1)
	{
		Log("Error while trying to send data" + ToString(errno),ERROR_TYPE);
		Close(hSocket);
		exit(-1);
	}

	return ret;
}

int Receive(int hSocket, void* data, int size ,int flag)
{
	int n,ret = 0;
	do
	{
		ret = recv(hSocket,(char*) data + n, size-n, flag);
		if (n == -1)
		{
			Log("Error while trying to receive data. Errno = "+ ToString(errno),ERROR_TYPE);
			Close(hSocket);
			exit(-1);
		}
	}while(n<size && ret !=0);
	
	return n;
}

string Receive(int hSocket,int flag)
{	
	string msg="";
	char byte;
	int n;
	do
	{
		n= recv(hSocket, &byte , 1 , flag);
		if (n == -1)
		{
			Log("Error while trying to receive data. Errno = "+ ToString(errno),ERROR_TYPE);
			Close(hSocket);
			exit(-1);
		}
		else if(n>0)
			msg+= byte;
	}while(byte != Config.EndTrame && n!=0);
	Log("Message received :"+msg,SUCCESS_TYPE);
	return msg;
}

void Close(int sHandler)
{
    if(close(sHandler) == -1)
    {
        string error = "Error while closing socket : ";
        switch(errno)
        {
            case EBADF:
                Log(error+"fd isn't a valid open file descriptor.",ERROR_TYPE);
                break;
            case EINTR:
                Log(error+"The close() call was interrupted by a signal; see signal(7).",ERROR_TYPE);
                break;
            
            case EIO : 
                Log(error+"An I/O error occurred.",ERROR_TYPE);
                break;
        }
        exit(-1);
    }
    Log("Successfully closed socket",SUCCESS_TYPE);
}

int CreateSocket()
{
    int hSocket;
	if ((hSocket = socket(AF_INET, SOCK_STREAM, 0)) == -1)
	{
		Log("Error while creating socket. Errno = "+ToString(errno), ERROR_TYPE);
		exit(-1);
	}
	Log("Successfully created socket",SUCCESS_TYPE);
	return hSocket;
}

