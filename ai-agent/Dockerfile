FROM eclipse-temurin:17-jdk

WORKDIR /app

COPY target/smart-todo-ai-agent-1.0-SNAPSHOT.jar /app/app.jar

RUN jar xf /app/app.jar META-INF/MANIFEST.MF \
 && (grep -q "Start-Class" META-INF/MANIFEST.MF || (echo "ERROR: app.jar is not a Spring Boot executable jar (missing Start-Class). Rebuild with spring-boot-maven-plugin." && exit 1))

EXPOSE 8004

ENV JAVA_OPTS=""

ENTRYPOINT ["sh","-c","java $JAVA_OPTS -jar /app/app.jar"]

