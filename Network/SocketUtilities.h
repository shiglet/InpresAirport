#ifndef SOCKETUTILITIES_H
#define SOCKETUTILITIES_H
#include "../Utils/Utilities.h"
//Client
void Connect(struct sockaddr_in , int );

//Client && Server
void Close(int );
int CreateSocket();
struct sockaddr_in GetAddr(string ,int);
int Send(int ,const void *,int, int);
int Receive(int , void* , int ,int);

//Server
void Bind(struct sockaddr_in ,int );
void Listen(int , int );
int Accept(struct sockaddr_in , int );

#endif
