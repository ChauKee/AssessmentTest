###########################################################
########### Local startup for Development #################
###########################################################
1. Have Java 17 installed in your local machine. Type "java -version" to check your installed Java version
2. Have Install Maven v3.9.0 in your local machine. Type "mvn -v" to check your installed Maven version
3. Open a command line, cd into the project root directory
4. Input your datasource in application-dev.properties. Datasource properties are spring.datasource.url,
spring.datasource.username, spring.datasource.password, and spring.jpa.database-platform
5. Type "mvn spring-boot:run -Dspring-boot.run.profiles=dev" to start the application with
dev profile

###########################################################
############### Build jar for Deployment ##################
###########################################################
1. Input your datasource in application-{profile}.properties, for example application-uat.properties for UAT profile/environment.
Datasource properties are spring.datasource.url, spring.datasource.username, spring.datasource.password, and spring.jpa.database-platform
2. Open a command line, cd into the project root directory
3. Type "mvn clean install -Dmaven.test.skip=true" to build a jar package of the project. 
The generated jar is named "assessment-0.0.1-SNAPSHOT.jar" and is located in the ./target folder
4. Open a command line, cd into the ./target folder. Type "java -jar assessment-0.0.1-SNAPSHOT.jar --spring.profiles.active=uat"
to start the application with UAT profile

###########################################################
############### Running Integration Test ##################
###########################################################
1. To run integration test, input Test Data Source Config in application.properties file under
/test/resources folder. Datasource properties are spring.datasource.url,spring.datasource.username,
spring.datasource.password, and spring.jpa.database-platform

###########################################################
############### Test Result & Screenshots #################
###########################################################
1. Test results and screenshot are placed under /resources/screenshots_results folder