FROM eclipse-temurin:17-jdk-jammy

WORKDIR /app

COPY src ./src

RUN mkdir -p out && javac -d out src/main/java/org/example/*.java

COPY test.zip /app/test.zip

ENV SERVER_HOST="172.17.0.2"
ENV SERVER_PORT="12345"
ENV LOCATION="Iasi"
ENV ZIP_PATH="/app/test.zip"

CMD ["sh", "-c", " \
    OPTION=$(shuf -i 1-4 -n 1); \
    echo \"Client starting with option: $OPTION\"; \
    if [ \"$OPTION\" -eq 3 ]; then \
        java -cp out org.example.Client $SERVER_HOST $SERVER_PORT $OPTION \"$LOCATION\"; \
    elif [ \"$OPTION\" -eq 4 ]; then \
        java -cp out org.example.Client $SERVER_HOST $SERVER_PORT $OPTION \"$ZIP_PATH\"; \
    else \
        java -cp out org.example.Client $SERVER_HOST $SERVER_PORT $OPTION; \
    fi \
"]
