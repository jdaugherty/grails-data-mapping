package grails.mongodb.bootstrap

import grails.gorm.tests.Plant
import org.grails.datastore.mapping.config.Settings
import org.grails.datastore.mapping.mongo.MongoDatastore
import org.testcontainers.containers.MongoDBContainer
import org.testcontainers.utility.DockerImageName
import spock.lang.AutoCleanup
import spock.lang.Shared
import spock.lang.Specification

/**
 * Created by graemerocher on 16/12/16.
 */
class FailOnErrorSetupSpec extends Specification {

    @Shared MongoDBContainer mongoDBContainer = new MongoDBContainer(DockerImageName.parse("mongo:${System.getProperty("mongodbContainerVersion", "7.0.16")}"))
    @Shared @AutoCleanup MongoDatastore datastore

    void setupSpec() {
        mongoDBContainer.start()
        System.setProperty('grails.mongodb.url', mongoDBContainer.getReplicaSetUrl('myDb'))
        datastore = new MongoDatastore([(Settings.SETTING_FAIL_ON_ERROR):true],Plant)
    }

    void cleanupSpec() {
        mongoDBContainer.stop()
    }

    void "test fail on error was configured correctly"() {

        when:
        def plant = new Plant()
        plant.save()

        then:
        plant.errors.hasErrors()
        thrown grails.validation.ValidationException
    }

}
