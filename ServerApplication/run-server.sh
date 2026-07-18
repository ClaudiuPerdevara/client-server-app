#!/usr/bin/env bash

if [ -z "$1" ]; then
	echo "Error: Please add the PORT number!"
	exit 1
fi

PORT=$1

if ! [[ $PORT =~ ^[0-9]+$ ]]; then
	echo "Error: PORT should be a valid number"
	exit 1
fi

echo "Server is starting on PORT ${PORT}..."
java -cp out org.example.Server $PORT






