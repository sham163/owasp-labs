# build
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /build
COPY pom.xml .
RUN mvn -q -DskipTests dependency:go-offline
COPY src ./src
RUN mvn -q -DskipTests package

# run
FROM eclipse-temurin:17-jre
WORKDIR /app
ENV UPLOAD_DIR=/app/uploads
RUN mkdir -p ${UPLOAD_DIR}
COPY --from=build /build/target/secure-university.jar /app/app.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","/app/app.jar"]
