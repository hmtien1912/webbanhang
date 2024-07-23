FROM openjdk:17-jdk-alpine

RUN apk update && apk add --no-cache maven

WORKDIR /app

COPY pom.xml .

RUN mvn dependency:go-offline

COPY src ./src

RUN mvn package -DskipTests

COPY target/*.jar app.jar

EXPOSE 25565

CMD ["java", "-jar", "app.jar"]