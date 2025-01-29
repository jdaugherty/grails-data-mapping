package example

import grails.boot.GrailsApp
import grails.boot.config.GrailsAutoConfiguration
import groovy.transform.CompileStatic
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.testcontainers.containers.MongoDBContainer
import org.testcontainers.utility.DockerImageName

@CompileStatic
@SpringBootApplication
class Application extends GrailsAutoConfiguration {
    static void main(String[] args) {
        try (
                MongoDBContainer dbContainer = new MongoDBContainer(DockerImageName.parse("mongo:${System.getProperty("mongodbContainerVersion", "7.0.16")}"))
        ) {
            dbContainer.start()

            System.setProperty('grails.mongodb.url', dbContainer.getReplicaSetUrl('myDb'))
            GrailsApp.run(Application, args)
        }
    }
}