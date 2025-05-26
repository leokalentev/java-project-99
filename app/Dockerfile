FROM gradle:8.5.0-jdk21

WORKDIR /app

COPY /app .

RUN chmod +x gradlew

RUN gradle installDist

CMD ./build/install/demo/bin/demo
