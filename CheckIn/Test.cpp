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
#include "Protocol/CIMP.h"
using namespace std;
int DisplayMenu();
int main()
{
    ReadConfigFile();
    vector<string> msg;
    cout<<CreateMessage(LOGIN_OFFICER,msg);
    return 0;
}
