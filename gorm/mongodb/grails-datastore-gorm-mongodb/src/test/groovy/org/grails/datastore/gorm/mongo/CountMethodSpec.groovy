package org.grails.datastore.gorm.mongo

import grails.gorm.annotation.Entity
import grails.mongodb.MongoEntity
import grails.mongodb.bootstrap.MongoDbDataStoreSpringInitializer
import org.grails.datastore.mapping.mongo.MongoDatastore
import org.testcontainers.containers.MongoDBContainer
import org.testcontainers.utility.DockerImageName
import spock.lang.AutoCleanup
import spock.lang.Shared
import spock.lang.Specification
import static com.mongodb.client.model.Filters.*
/**
 * Created by graemerocher on 29/11/2016.
 */
class CountMethodSpec extends Specification {

    @AutoCleanup @Shared MongoDatastore datastore

    @Shared MongoDBContainer mongoDBContainer = new MongoDBContainer(DockerImageName.parse("mongo:${System.getProperty("mongodbContainerVersion", "7.0.16")}"))

    void setupSpec() {
        mongoDBContainer.start()
        System.setProperty('grails.mongodb.url', mongoDBContainer.getReplicaSetUrl(MongoDbDataStoreSpringInitializer.DEFAULT_DATABASE_NAME))
        datastore = new MongoDatastore(CountTest)
    }

    void cleanupSpec() {
        mongoDBContainer.stop()
    }

    void "test count method"() {
        given:"some test data "
        CountTest.DB.drop()
        CountTest.withNewSession {
            new CountTest(name: "foo").save()
            new CountTest(name: "bar").save(flush:true)
        }

        expect:
        CountTest.find(eq("name", "foo"))
        CountTest.count(eq("name", "foo")) == 1
    }
}

@Entity
class CountTest implements MongoEntity<CountTest> {
    String name

}
