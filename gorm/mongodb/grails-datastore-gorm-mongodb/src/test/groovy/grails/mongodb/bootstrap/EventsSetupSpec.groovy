package grails.mongodb.bootstrap

import grails.gorm.annotation.Entity
import grails.gorm.tests.Plant
import grails.mongodb.MongoEntity
import org.grails.datastore.mapping.config.Settings
import org.grails.datastore.mapping.mongo.MongoDatastore
import org.testcontainers.containers.MongoDBContainer
import org.testcontainers.utility.DockerImageName
import spock.lang.AutoCleanup
import spock.lang.Shared
import spock.lang.Specification

/**
 * Created by graemerocher on 05/10/2016.
 */
class EventsSetupSpec extends Specification {

    @Shared MongoDBContainer mongoDBContainer = new MongoDBContainer(DockerImageName.parse("mongo:${System.getProperty("mongodbContainerVersion", "7.0.16")}"))
    @Shared @AutoCleanup MongoDatastore datastore

    void setupSpec() {
        mongoDBContainer.start()
        System.setProperty('grails.mongodb.url', mongoDBContainer.getReplicaSetUrl('myDb'))
        datastore = new MongoDatastore(MyEventSender)
    }

    void cleanupSpec() {
        mongoDBContainer.stop()
    }

    void 'test events get triggered'() {
        setup:
        MyEventSender.DB.drop()
        when:
        new MyEventSender(name: "fred").save(flush:true)

        then:
        MyEventSender.first().name == 'FRED'
    }
}

@Entity
class MyEventSender implements MongoEntity<MyEventSender> {
    String name

    def beforeInsert() {
        name = name.toUpperCase()
    }
}
