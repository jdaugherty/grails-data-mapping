package databasepertenant.tests

import grails.boot.GrailsApp
import grails.boot.config.GrailsAutoConfiguration
import groovy.transform.CompileStatic
import org.testcontainers.containers.MongoDBContainer
import org.testcontainers.utility.DockerImageName

@CompileStatic
class Application extends GrailsAutoConfiguration {
    static void main(String[] args) {
        try (
                MongoDBContainer dbContainer = new MongoDBContainer(DockerImageName.parse("mongo:${System.getProperty("mongodbContainerVersion", "7.0.16")}"))
        ) {
            dbContainer.start()
            System.setProperty('grails.mongodb.url', dbContainer.getReplicaSetUrl('mydb'))
            System.setProperty('grails.mongodb.connections.test1.url', dbContainer.getReplicaSetUrl('test1Db'))
            System.setProperty('grails.mongodb.connections.test2.url', dbContainer.getReplicaSetUrl('test2Db'))
            GrailsApp.run(Application, args)
        }
    }
}