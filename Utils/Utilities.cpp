#include "Utilities.h"
void Log(string log, int type)
{
    switch(type)
    {
        case DEFAULT_TYPE : 
            cout << "\033[1;37m[Default] - ";
            break;
        case ERROR_TYPE :
            cout << "\033[1;31m[Error] - ";
            break;
        case SUCCESS_TYPE : 
            cout << "\033[1;34m[Success] - ";
            break;
        case INFO_TYPE : 
            cout << "\033[1;32m[Info] - ";
            break;
    }
    cout<<log<<"\033[0m\n";
}

string ToString(int n)
{
    std::ostringstream stm ;
    stm << n ;
    return stm.str() ;
}