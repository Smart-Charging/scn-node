FROM openjdk:8-alpine

COPY build /scn-node
COPY src/main/resources/* /scn-node/
WORKDIR /scn-node

CMD ["java", "-jar", "./libs/scn-node-1.0.0.jar"]
