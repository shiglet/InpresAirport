#ifndef UTILITIES_H
#define UTILITIES_H
#include "Include.h"

//Struct
typedef struct
{
    string Host;
    int CheckPort;
    char TrameSeparator;
    char EndTrame;
    char CSVSeparator;
    string LoginFile;
    string TicketFile;
} Configuration;
extern Configuration Config;
//Log
#define DEFAULT_TYPE 0
#define ERROR_TYPE 1
#define SUCCESS_TYPE 2
#define INFO_TYPE 3
void Log(string log, int type = DEFAULT_TYPE);


//Conf
void ReadConfigFile();
void ReadConfigFileClient();

//Others
template<typename T>
string ToString(T);
vector<string> Tokenize(string message, string key = string()+Config.TrameSeparator+Config.EndTrame);

//Ticket
bool CheckTicket(string,string);
//Login
bool CheckLogin(string,string);

#endif

