.SILENT:

OBJ = $(B)Utilities.o $(B)SocketUtilities.o
B = bin/
EXEC = Client Server Test
U = Utils/
NET = Network/
C = g++ -g -std=c++11
all : $(EXEC)

Client: Client.cpp $(OBJ)
		echo Creation de Client
		$(C) -o $(B)Client Client.cpp $(OBJ) -Wall -lnsl -lpthread
Server: Server.cpp $(OBJ)
		echo Creation de Server
		$(C) -o $(B)Server Server.cpp $(OBJ) -Wall -lnsl -lpthread
Test:	Test.cpp $(OBJ)
		echo Creation de Test
		$(C) -o $(B)Test Test.cpp $(OBJ) -Wall -lnsl -lpthread
$(B)SocketUtilities.o:	$(NET)SocketUtilities.cpp $(NET)SocketUtilities.h
		echo Creation de SocketUtilities.o
		$(C) -o $(B)SocketUtilities.o -c $(NET)SocketUtilities.cpp
$(B)Utilities.o:	$(U)Utilities.cpp $(U)Utilities.h
		echo Creation de Utilities.o
		$(C) -o $(B)Utilities.o -c $(U)Utilities.cpp
clean:
	rm -rf $(B)*.o

clobber: clean
	rm -rf $(B)$(EXEC)
