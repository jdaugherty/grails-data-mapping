package org.grails.datastore.gorm.mongo

import grails.gorm.annotation.Entity
import grails.mongodb.MongoEntity
import org.bson.types.ObjectId
import org.grails.datastore.mapping.mongo.MongoDatastore
import org.grails.datastore.mapping.mongo.config.MongoSettings
import org.testcontainers.containers.MongoDBContainer
import org.testcontainers.utility.DockerImageName
import spock.lang.Specification

/**
 * Created by graemerocher on 30/06/16.
 */
class MultipleConnectionsSpec extends Specification {

    MongoDBContainer mongoDBContainer = new MongoDBContainer(DockerImageName.parse("mongo:${System.getProperty("mongodbContainerVersion", "7.0.16")}"))
    MongoDatastore datastore

    void setup() {
        mongoDBContainer.start()
        System.setProperty(MongoSettings.SETTING_HOST, mongoDBContainer.getHost())
        System.setProperty(MongoSettings.SETTING_PORT, mongoDBContainer.getMappedPort(27017).toString())
        Map config = [
            (MongoSettings.SETTING_URL)        : "mongodb://localhost/defaultDb",
            (MongoSettings.SETTING_CONNECTIONS): [
                    test1: [
                            url: "mongodb://localhost/test1Db"
                    ],
                    test2: [
                            url: "mongodb://localhost/test2Db"
                    ]
            ]
        ]
        this.datastore = new MongoDatastore(config, getDomainClasses() as Class[])
    }

    void cleanup() {
        datastore.close()
        mongoDBContainer.stop()
    }

    void "Test multiple datasources state"() {
        expect:
        CompanyA.DB.name == 'test1Db'
        CompanyA.test2.DB.name == 'test2Db'
    }

    void "Test query multiple data sources"() {
        when:"An entity is saved"
        new CompanyA(name:"One").save(flush:true)

        then:"The results are correct"
        CompanyA.count() == 1
        CompanyA.withConnection("test2") { count() } == 0

        when:"An entity is saved to another connection"
        new CompanyA(name:"Two").save(flush:true)
        CompanyA.withConnection("test2") {
            save(new CompanyA(name: "Three"), [flush:true])
        }

        then:"The results are correct"
        CompanyA.count() == 2
        CompanyA.first()
        CompanyA.withConnection("test2") { count() == 1 }
    }

    List getDomainClasses() {
        [CompanyA]
    }
}

/**
 * Created by graemerocher on 30/06/16.
 */
@Entity
class CompanyA implements MongoEntity<CompanyA> {
    ObjectId id
    String name
    static mapping = {
        connections "test1", "test2"
    }
}

