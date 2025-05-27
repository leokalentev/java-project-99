FROM gradle:8.5.0-jdk21

WORKDIR /app

COPY . .

RUN chmod +x gradlew

RUN ./gradlew installDist --no-daemon

CMD ./build/install/demo/bin/demo
