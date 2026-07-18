FROM ubuntu

RUN apt-get update \
    && apt-get install -y python3 gcc g++ openjdk-17-jdk

#fac un dir in container pt a lucra in el si sa nu amestec cu altele
WORKDIR /app

#copiez codul sursa in src
COPY src ./src

RUN mkdir -p out && javac -d out src/main/java/org/example/*.java

ENV SERVER_PORT=12345

CMD ["sh", "-c", "java -cp out org.example.Server $SERVER_PORT"]

