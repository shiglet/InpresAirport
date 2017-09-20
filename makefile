.SILENT:

OBJ = Utilities.o SocketUtilities.o
EXEC = Client Server Test
U = Utils/
NET = Network/
C = g++ -g
all : $(EXEC)

Client: Client.cpp $(OBJ)
		echo Creation de Client
		$(C) -o Client Client.cpp $(OBJ) -Wall -lnsl -lpthread
Server: Server.cpp $(OBJ)
		echo Creation de Server
		$(C) -o Server Server.cpp $(OBJ) -Wall -lnsl -lpthread
Test:	Test.cpp $(OBJ)
		echo Creation de Test
		$(C) -o Test Test.cpp $(OBJ) -Wall -lnsl -lpthread
SocketUtilities.o:	$(NET)SocketUtilities.cpp $(NET)SocketUtilities.h
		echo Creation de SocketUtilities.o
		$(C) -c $(NET)SocketUtilities.cpp
Utilities.o:	$(U)Utilities.cpp $(U)Utilities.h
		echo Creation de Utilities.o
		$(C) -c $(U)Utilities.cpp
clean:
	rm -rf *.o

clobber: clean
	rm -rf $(EXEC)
