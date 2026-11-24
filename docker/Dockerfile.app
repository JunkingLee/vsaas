# syntax=docker/dockerfile:1
# docker build -f docker/Dockerfile.app --build-arg MODULE=vsaas-auth -t vsaas-auth:1.0 .
ARG MODULE=vsaas-auth

FROM maven:3.9.9-eclipse-temurin-21 AS build
ARG MODULE
WORKDIR /src
COPY pom.xml .
COPY vsaas-common/pom.xml vsaas-common/
COPY vsaas-api/pom.xml vsaas-api/
COPY vsaas-auth/pom.xml vsaas-auth/
COPY vsaas-tenant/pom.xml vsaas-tenant/
COPY vsaas-user/pom.xml vsaas-user/
COPY vsaas-device/pom.xml vsaas-device/
COPY vsaas-media/pom.xml vsaas-media/
COPY vsaas-record/pom.xml vsaas-record/
COPY vsaas-alarm/pom.xml vsaas-alarm/
COPY vsaas-billing/pom.xml vsaas-billing/
RUN mvn -B -q -DskipTests dependency:go-offline || true
COPY . .
RUN mvn -B -pl ${MODULE} -am -DskipTests package \
 && JAR=$(ls ${MODULE}/target/*.jar | grep -v original | head -1) \
 && cp "$JAR" /src/app.jar

FROM eclipse-temurin:21-jre-jammy
WORKDIR /app
ENV JAVA_OPTS="-Xms256m -Xmx512m -XX:+UseG1GC"
COPY --from=build /src/app.jar /app/app.jar
EXPOSE 8080
ENTRYPOINT ["sh","-c","java $JAVA_OPTS -jar /app/app.jar"]
