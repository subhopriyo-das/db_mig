# oracle to GCP Migration service

## About

https://github.com/DOCS/MoneyGramOracle2GCPMigration.md

## Setting up minimal development environment

*Required Tools*
   * Git 1.7.1+
   * Java 1.7+
   * Docker 1.12+
   * Docker Compose 1.12+

1. Clone this Git repository, best using SSH.
2. clead the project with `./gradlew clean`.
3. build the project with `./gradlew build -x test`.
4. Build and deploy the application with `./gradlew deploy`.

## Setting up Eclipse IDE development environment
1. Import projects into Eclipse using `File -> Import, Gradle -> Existing Gradle Project`,
   using your local repository clone as the _Project root directory_. Ensure
   _Gradle wrapper_ (default) is selected as _Gradle distribution_.

## Configuring the development environment 
1. Recreate the Liberty server, redeploy the application and start the server
   with:
   ```
   ./gradlew clean
   ./gradlew build -x test
   ./gradlew setupLiberty
   ./gradlew deploy
   ./gradlew libertyStop
   ./gradlew libertyStart
   ```
   
## Building docker image locally and deployed to UG machine
Docker image build is orchestrated with Gradle scripting.

1. Login to docker repository 
   ```
   docker login docker_repo_url
   ```
2. Run the following command to build the Docker image:
   ```
   ./gradlew buildDockerImage
   ```
3. Save docker image in tar file
   ```
   docker save -o <path for generated tar file> <image_id>
   ```
4. load docker image from tar file
   ```
   docker load -i <path to image tar file>
   ```
    

## Manual Deployment of service
1. Login to docker repo
   ```
   docker login doxker-repo-url
   ```
2. Pull the latest docker image
   ```
   docker pull dockr-url:latest
   ```
3. Get the IMAGE ID of the latest image pulled above
   ```
   docker images docker-url:latest
   REPOSITORY                                                                    TAG                 IMAGE ID            CREATED             SIZE
   docker-url/service   latest              4c2f27e50523        3 weeks ago         726.3 MB
   ```
4. Re-Tag the docker image to local
   ```
   docker tag <IMAGE ID> localhost:5000/service
   ```
## Thank You Very much !!!! !!
