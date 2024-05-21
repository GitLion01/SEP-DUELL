FROM openjdk:20
COPY target/demo-0.0.1-SNAPSHOT.jar application.jar
# Create a directory to store images
RUN mkdir -p /app/images

# Copy the image file into the container
COPY images/bild.jpg /app/images/

ENTRYPOINT ["java", "-jar", "application.jar"]
