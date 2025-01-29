package org.grails.datastore.gorm.mongodb.boot.autoconfigure

import grails.gorm.annotation.Entity
import org.springframework.boot.autoconfigure.AutoConfigurationPackages
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.boot.autoconfigure.context.PropertyPlaceholderAutoConfiguration
import org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration
import org.springframework.context.annotation.AnnotationConfigApplicationContext
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import
import org.testcontainers.containers.MongoDBContainer
import org.testcontainers.utility.DockerImageName
import spock.lang.PendingFeature
import spock.lang.Shared
import spock.lang.Specification

/**
 * Tests for MongoDB autoconfigure
 */
class MongoDbGormAutoConfigurationSpec extends Specification {

    @Shared MongoDBContainer mongoDBContainer = new MongoDBContainer(DockerImageName.parse("mongo:${System.getProperty("mongodbContainerVersion", "7.0.16")}"))
    protected AnnotationConfigApplicationContext context

    void setupSpec() {
        mongoDBContainer.start()
        System.setProperty('spring.data.mongodb.uri', mongoDBContainer.getReplicaSetUrl('myDb'))
    }

    void cleanup() {
        context.close()
    }

    void cleanupSpec() {
        mongoDBContainer.stop()
    }

    void setup() {
        context = new AnnotationConfigApplicationContext()
        AutoConfigurationPackages.register(context, "org.grails.datastore.gorm.mongodb.boot.autoconfigure")
        this.context.register(
                TestConfiguration,
                MongoAutoConfiguration,
                PropertyPlaceholderAutoConfiguration
        )
    }

    @PendingFeature
    void 'Test that GORM is correctly configured'() {
        when: "The context is refreshed"
        context.refresh()

        then: "GORM queries work"
        Person.count() != null
    }

    @Configuration
    @EnableAutoConfiguration
    @ComponentScan("org.grails.datastore.gorm.mongodb.boot.autoconfigure")
    @Import(MongoDbGormAutoConfiguration)
    static class TestConfiguration {
    }
}

@Entity
class Person {
    String firstName
    String lastName
    Integer age = 18
}
