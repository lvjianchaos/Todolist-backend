FROM eclipse-temurin:17-jdk

WORKDIR /app

# Copy the pre-built Spring Boot executable jar
COPY target/smart-todo-gateway-1.0-SNAPSHOT.jar /app/app.jar

# Fail fast during image build if the jar is not executable (missing Spring Boot manifest entries)
RUN jar xf /app/app.jar META-INF/MANIFEST.MF \
 && (grep -q "Start-Class" META-INF/MANIFEST.MF || (echo "ERROR: app.jar is not a Spring Boot executable jar (missing Start-Class). Rebuild with spring-boot-maven-plugin." && exit 1))

EXPOSE 8080

ENV JAVA_OPTS=""

ENTRYPOINT ["sh","-c","java $JAVA_OPTS -jar /app/app.jar"]
