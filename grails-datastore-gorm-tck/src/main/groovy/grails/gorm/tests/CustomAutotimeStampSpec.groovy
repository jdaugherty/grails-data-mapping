package grails.gorm.tests

import grails.persistence.Entity

class CustomAutotimeStampSpec extends GormDatastoreSpec{

    void "Test when the auto timestamp properties are customized, they are correctly set"() {
        when:"An entity is persisted"
            def r = new RecordCustom(name: "Test")
            r.save(flush:true)
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
            r.modified != null && previousModified > r.modified
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
    Date created
    Date modified

    static mapping = {
        dateCreated 'created'
        lastUpdated 'modified'
    }
}
