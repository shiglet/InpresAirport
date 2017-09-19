#ifndef SOCKETUTILITIES_H
#define SOCKETUTILITIES_H

//Client
void Connect(struct sockaddr_in , int );

//Client && Server
void Close(int );
int CreateSocket();
struct sockaddr_in GetAddr(char * ,int);
/*
int Send(int ,void *,int, int);
int Receive(int , void* , int ,int);*/

//Server
void Bind(struct sockaddr_in ,int );
void Listen(int , int );
int Accept(struct sockaddr_in , int );

#endif
