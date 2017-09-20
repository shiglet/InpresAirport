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

Configuration ReadConfigFile()
{
    Configuration Config;
    // Short alias for this namespace
    namespace pt = boost::property_tree;

    // Create a root
    pt::ptree root;

    // Load the json file in this ptree
    pt::read_json("Config/ServerConfigFile.json", root);
    Config.Host = root.get<string>("host");
    Config.CheckPort = root.get<int>("checkport");
    Config.TrameSeparator = root.get<char>("trameseparator");
    Config.EndTrame = root.get<char>("endtrame");
    Config.CSVSeparator = root.get<char>("csvseparator");
    cout<<Config.Host<<" "<<Config.CheckPort<<" "<<Config.TrameSeparator<<" "<<Config.EndTrame<<" "<<Config.CSVSeparator<<" "<<endl;
    return Config;
}
void ReadConfigFileClient()
{

}