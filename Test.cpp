#include "Network/SocketUtilities.h"
Configuration Config;
int main()
{
    Config = ReadConfigFile();
    Log(Config.Host);
    return 0;
}