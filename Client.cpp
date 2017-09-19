#include "Network/SocketUtilities.h"
#include "Utils/Utilities.h"
int sHandler;
int main()
{
    Log("Création d'une socket",INFO_TYPE);
    sHandler = CreateSocket();
}