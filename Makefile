setup:
	cd frontend && npm ci
	chmod +x gradlew
	./gradlew build installDist

run:
	./build/install/demo/bin/demo

docker-build:
	docker build -t java-project-99 .

docker-run:
	docker run --rm java-project-99
