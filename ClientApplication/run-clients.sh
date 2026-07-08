#!/usr/bin/env bash

if [ "$#" -lt 3 ]; then
	echo "Error: Please add a PORT number, location and path to ZIP!"
	exit 1
fi

location="$2"
path="$3"

PORT=$1

if ! [[ $PORT =~ ^[0-9]+$ ]]; then
	echo "Error: PORT number should be a valid number!"
	exit 1
fi

for i in {1..100}; do
	option=$(shuf -i 1-4 -n 1)
	(
		if [[ $option -eq 3 ]]; then
			output=$(java -cp out org.example.Client "$PORT" "$option" "${location}" 2>&1)
		elif [[ $option -eq 4 ]]; then
			output=$(java -cp out org.example.Client $PORT $option "${path}" 2>&1)
		else
			output=$(java -cp out org.example.Client $PORT $option 2>&1)
		fi

		printf "Client %s: option %s \n %s \n --------------------\n" "$i" "$option" "$output"
	) &
done


