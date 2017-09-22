#include "Utilities.h"
Configuration Config;

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

void ReadConfigFile()
{
    namespace pt = boost::property_tree;

    pt::ptree root;

    // Load the json file in this ptree
    pt::read_json("../Config/ServerConfigFile.json", root);
    Config.Host = root.get<string>("host");
    Config.CheckPort = root.get<int>("checkport");
    Config.TrameSeparator = root.get<char>("trameseparator");
    Config.EndTrame = root.get<char>("endtrame");
    Config.CSVSeparator = root.get<char>("csvseparator");
    Config.LoginFile = root.get<string>("loginfile");
}

//Login 
bool CheckLogin(string login, string password)
{
    using namespace boost;
    ifstream in(Config.LoginFile.c_str());
    if (!in.is_open())
    {
        Log("Unable to read Login file !",ERROR_TYPE);
        exit(-1);
    }
    string line;
    while (getline(in,line))
    {
        vector<std::string> tokens;
        split(tokens, line, is_any_of(";"),token_compress_on);
        if(tokens.size()<2) continue;
        if(tokens.at(0) == login && tokens.at(1)==password)
            return true;
    }
    return false;
}