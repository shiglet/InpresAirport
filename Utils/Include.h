#ifndef INCLUDE_H
#define INCLUDE_H
//Include
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
#include <pthread.h>
#include <boost/property_tree/ptree.hpp>
#include <boost/property_tree/json_parser.hpp>
#include <boost/algorithm/string/classification.hpp> // Include boost::for is_any_of
#include <boost/algorithm/string/split.hpp> // Include for boost::split
#include <boost/algorithm/string/replace.hpp>
using namespace std;

//Define
#define BUFFER_SIZE 1024
#endif
