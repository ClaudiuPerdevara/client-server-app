SERVER_PORT ?= 12345

LOCATION ?= "Bucharest"
ZIP_PATH ?= "/home/claudiu/Desktop/intro/client-server-app/test.zip"

SERVER_BASH_PATH := ServerApplication/run-server.sh
CLIENT_BASH_PATH := ClientApplication/run-clients.sh

.PHONY: all server client run-server run-clients clean

all: server client

server:
	@echo "Compiling server..."
	javac -d out ServerApplication/src/main/java/org/example/*.java

client:
	@echo "Compiling client..."
	javac -d out ClientApplication/src/main/java/org/example/Client.java


run-server: server
	@echo "Starting server on PORT $(SERVER_PORT)..."
	chmod +x $(SERVER_BASH_PATH)
	./$(SERVER_BASH_PATH) $(SERVER_PORT)

run-clients: server client
	@echo "Starting 100 concurrent clients..."
	chmod +x $(CLIENT_BASH_PATH)
	./$(CLIENT_BASH_PATH) $(SERVER_PORT) $(LOCATION) $(ZIP_PATH)


clean:
	rm -rf out/


