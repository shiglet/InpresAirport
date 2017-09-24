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
    SaveLuggage(1,Config.FlyNumber+ToString(100),"O");
    SaveLuggage(2,Config.FlyNumber+ToString(100),"O");
    SaveLuggage(3,Config.FlyNumber+ToString(100),"N");
    SaveLuggage(15,Config.FlyNumber+ToString(100),"O");
    return 0;
}
