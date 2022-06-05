# Memory Game Service

The backend server of the online multiplayer [Memory Game](https://play.google.com/store/apps/details?id=com.AngeloBracaglia.MemoryGame&hl=en_US&gl=US).

## Description

The Memory Game Service provides social and online multiplayer features to the Memory Game Android app. <br>
The player can manage a list of private friends to play with or can join a random opponent in the queue. <br>
By using JSR 356, Java API for WebSocket, the user get notified in realtime about new friend requests and about private game invitations. <br>
The game session is managed by an authority server and it is player session agnostic. <br>
The service can provide an AI game client to avoid long matchmacking queue. <br> 


## Getting Started

### Dependencies

* Java 8 required.
* Maven

### Executing program

* The Memory Game Service comes with an embedded h2 database configured in the local profile.
* The application can be started using maven:

```
mvn spring-boot:run -Dspring-boot.run.profiles=local
```
* The h2-console is available at: 

```
localhost:8080/h2 
```

## Authors

Angelo Bracaglia [Get in touch](mailto:angelo.bracaglia.ing@gmail.com?subject=[GitHub]-Memory-game-service) 


