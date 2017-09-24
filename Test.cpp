#include <iostream>     // cout, endl
#include <fstream>      // fstream
#include <vector>
#include <string>
#include <algorithm>    // copy
#include <iterator>     // ostream_operator
 
#include <boost/tokenizer.hpp>
#include <boost/algorithm/string/classification.hpp> // Include boost::for is_any_of
#include <boost/algorithm/string/split.hpp> // Include for boost::split
#include "Utils/Utilities.h"
using namespace std;
int DisplayMenu();
int main()
{
    ReadConfigFile();
    string poids,valise,msg;
    int count;
    msg+=ToString(10);
    cin>>count;
    for(int i=0;i<count;i++)
    {
        cout<<"Poids du baggages n°"+ToString(i+1)+" : ";
        cin>>poids;
        cout<<"Valise ? : ";
        cin>>valise;
        msg+= Config.TrameSeparator+poids+Config.TrameSeparator+valise;
    }
    msg+= Config.EndTrame;
    cout<<msg;
    vector<string> tokens = Tokenize(msg);
    tokens.pop_back();
    for(std::vector<string>::size_type i = 1; i != tokens.size(); i+=2)
    {
        cout<<endl<<tokens[i];
        cout<<tokens[i+1];
    }
        
    return 0;
}
