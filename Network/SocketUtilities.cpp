#include "SocketUtilities.h"
#include "../Utils/Utilities.h"
//Client
void Connect(struct sockaddr_in addr , int sHandler)
{
    int tailleSockaddr_in = sizeof(struct sockaddr_in);
	int ret = connect(sHandler, (struct sockaddr *) &addr, tailleSockaddr_in);

	if (ret == -1)
	{
		printf("[Error] on socket connect %d\n", errno);

		switch(errno)
		{
			case EBADF : printf("[Error] EBADF - hsocket doesn't exitst\n");
					break;

			default : printf("[Error] Unknown error ?\n");
		}

		Close(sHandler);
		exit(-1);
	}

	else
		printf("Successfull connect\n");
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
	Log("Successfully created socket\n",SUCCESS_TYPE);
	return hSocket;
}

struct sockaddr_in GetAddr(char * host ,int port)
{
	struct hostent * infosHost;
	struct in_addr ipAddress; /* Adresse Internet au format reseau */
	struct sockaddr_in socketAddress;
	/* 2-3. Acquisition des informations sur l'ordinateur local */
	if ((infosHost = gethostbyname(host)) == 0)
	{
		Log("Error while getting host by name. Errno = "+ToString(errno), ERROR_TYPE);
		exit(-1);
	}
	Log("Successfully get host Info\n",SUCCESS_TYPE);

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
	Log("Bind adresse et port socket OK\n",SUCCESS_TYPE);
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
		exit(1);
	}
	Log("Successfully accepted a client connection",SUCCESS_TYPE);

	return serviceSocket;
}
/*
//Client && Server
int Send(int ,void *,int, int);
int Receive(int , void* , int ,int);
*/
