#ifndef INCLUDE_H
#define INCLUDE_H

#include <stdio.h>
#include <stdlib.h> /* pour exit */
#include <string.h> /* pour memcpy */
#include <unistd.h>
#include <sys/types.h>
#include <sys/socket.h> /* pour les types de socket */
#include <netdb.h> /* pour la structure hostent */
#include <errno.h>
#include <netinet/in.h> /* pour la conversion adresse reseau format dot ainsi que le conversion format local/format reseau */
#include <netinet/tcp.h> /* pour la conversion adresse reseau format dot */
#include <arpa/inet.h> /* pour la conversion adresse reseau format dot */
#include <time.h> /* pour select et timeval */
#include <pthread.h>
#include <signal.h>
#include <iostream>
#include <sstream>
using namespace std;

#endif
