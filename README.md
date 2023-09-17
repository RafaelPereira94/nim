# Nim Game
This Repo contains the backend implementation of nim game made with springboot, kotlin and redis.


- git clone https://github.com/RafaelPereira94/nim
- cd nim

# Setup redis
Use the following docker compose command to start redis database.

`docker-compose up -d`

## Build project:
`./gradlew build`

# Run tests
`./gradlew tests`

# Run Application
After docker image is running we can start our springboot application by using the following command:

`./gradlew run`

# Swagger 
The Api endpoints can be seen on swagger using the following url locally:
http://localhost:8080/swagger-ui/index.html/

# Note:
For this challenge only the optional goal `a)` was not archived.

# Future work 
- Add winning game strategy to computer move.
- More test cases could be done.
- Add authentication so each user has its own games.