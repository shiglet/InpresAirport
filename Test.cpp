#include <iostream>     // cout, endl
#include <fstream>      // fstream
#include <vector>
#include <string>
#include <algorithm>    // copy
#include <iterator>     // ostream_operator
 
#include <boost/tokenizer.hpp>
#include <boost/algorithm/string/classification.hpp> // Include boost::for is_any_of
#include <boost/algorithm/string/split.hpp> // Include for boost::split
using namespace std;

int main()
{
    int i=0;
    do
    {
        cout<<i<<endl;
        if(i==10)
            continue;
        i++;
    }while(i!=10);
    return 0;
}