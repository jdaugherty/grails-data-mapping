package org.grails.datastore.gorm

import grails.gorm.annotation.AutoTimestamp
import static grails.gorm.annotation.AutoTimestamp.EventType.*;
import grails.gorm.tests.GormDatastoreSpec
import grails.persistence.Entity

class CustomAutoTimestampSpec extends GormDatastoreSpec {

    void "Test when the auto timestamp properties are customized, they are correctly set"() {
        when:"An entity is persisted"
            def r = new RecordCustom(name: "Test")
            r.save(flush:true, failOnError:true)
            session.clear()
            r = RecordCustom.get(r.id)

        then:"the custom lastUpdated and dateCreated are set"
            r.modified != null && r.modified < new Date()
            r.created != null && r.created < new Date()

        when:"An entity is modified"
            Date previousCreated = r.created
            Date previousModified = r.modified
            r.name = "Test 2"
            r.save(flush:true)
            session.clear()
            r = RecordCustom.get(r.id)

            then:"the custom lastUpdated property is updated and dateCreated is not"
            r.modified != null && previousModified < r.modified
            previousCreated == r.created
    }
    @Override
    List getDomainClasses() {
        [RecordCustom]
    }
}

@Entity
class RecordCustom {
    Long id
    String name
    @AutoTimestamp(SAVE) Date created
    @AutoTimestamp Date modified
}
