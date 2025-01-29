package mongofunctional.tests

import grails.boot.GrailsApp
import grails.boot.config.GrailsAutoConfiguration
import groovy.transform.CompileStatic
import org.springframework.boot.SpringBootConfiguration
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.testcontainers.containers.MongoDBContainer
import org.testcontainers.utility.DockerImageName

@CompileStatic
@SpringBootApplication
class Application extends GrailsAutoConfiguration {
    static void main(String[] args) {
        String mongoVersion = "7.0.15-ubuntu2204" //System.getProperty("mongodbContainerVersion", "7.0.16")
        try (
                MongoDBContainer dbContainer = new MongoDBContainer(DockerImageName.parse("mongodb/mongodb-community-server:${mongoVersion}"))
        ) {
            dbContainer.start()
            System.setProperty('grails.mongodb.url', dbContainer.getReplicaSetUrl('test'))
            GrailsApp.run(Application, args)

        }
    }
}