.SILENT:

OBJ = Utilities.o SocketUtilities.o
EXEC = Client Server
U = Utils/
NET = Network/
all : $(EXEC)

Client: Client.cpp $(OBJ)
		echo Creation de Client
		g++ -g -o Client Client.cpp $(OBJ) -Wall
Server: Server.cpp $(OBJ)
		echo Creation de Server
		g++ -g -o Server Server.cpp $(OBJ) -Wall
SocketUtilities.o:	$(NET)SocketUtilities.cpp $(NET)SocketUtilities.h
		echo Creation de SocketUtilities.o
		g++ -g -c $(NET)SocketUtilities.cpp
Utilities.o:	$(U)Utilities.cpp $(U)Utilities.h
		echo Creation de Utilities.o
		g++ -g -c $(U)Utilities.cpp
clean:
	rm -rf *.o

clobber: clean
	rm -rf $(EXEC)
