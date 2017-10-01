#include "Utilities.h"
Configuration Config;

void Log(string log, int type)
{
    if(!Config.Log) return;
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
template<typename T>
string ToString(T n)
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
    pt::read_json("../Config/ConfigFile.json", root);
    Config.Host = root.get<string>("host");
    Config.CheckPort = root.get<int>("checkport");
    Config.TrameSeparator = root.get<char>("trameseparator");
    Config.EndTrame = root.get<char>("endtrame");
    Config.CSVSeparator = root.get<char>("csvseparator");
    Config.LoginFile = root.get<string>("loginfile");
    Config.TicketFile = root.get<string>("ticketfile");
    Config.Fly = root.get<string>("fly");
    Config.FlyNumber = root.get<string>("flynumber");
    Config.ExceededPrice = root.get<float>("exceededprice");
    Config.Log = root.get<bool>("log");
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
        vector<std::string> tokens = Tokenize(line,string() + Config.CSVSeparator);
        if(tokens.size()<2) continue;
        if(tokens.at(0) == login && tokens.at(1)==password)
            return true;
    }
    return false;
}


//CheckTicket
bool CheckTicket(string ticketNumber, string count)
{
    using namespace boost;
    ifstream in(Config.TicketFile.c_str());
    if (!in.is_open())
    {
        Log("Unable to read Ticket file !",ERROR_TYPE);
        exit(-1);
    }
    string line;
    while (getline(in,line))
    {
        vector<std::string> tokens = Tokenize(line,string() + Config.CSVSeparator);
        if(tokens.size()<3) continue;
        cout<<tokens.size()<<endl;
        if(tokens[0] == ticketNumber && tokens[1]==count && tokens[2] == "non_checked")
            return true;
    }
    return false;
}

vector<string> Tokenize(string message, string key)
{
    using namespace boost;
    vector<std::string> tokens;
    split(tokens, message, is_any_of(key),token_compress_on);
    return tokens;
}

void SaveLuggage(int n ,string ticketNumber,string valise)
{

    string fileName = Config.FlyNumber;
    boost::replace_all(fileName, "-", "_");
    fileName += "lug.csv";
    ofstream file(fileName.c_str(),ios::out | ios::app);
    file<<ticketNumber<<"-"<<setfill('0')<<setw(3)<<n<<Config.CSVSeparator;
    valise == "O" ? file<<"VALISE"<<endl : file<<"PASVALISE"<<endl;
    file.close();
}

template string ToString<int>(int);
template string ToString<float>(float);