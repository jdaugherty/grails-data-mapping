package mongofunctional.tests

import grails.plugin.geb.ContainerGebSpec
import grails.testing.mixin.integration.Integration

@Integration
class BookControllerSpec extends ContainerGebSpec {

    void "Test list books"() {
        when:"The home page is visited"
        browser.go '/book/index'

        then:"The title is correct"
        browser.title == "Book List"
    }

    void "Test save book"() {
        when:
        browser.go "/book/create"
        browser.$('form').title = "The Stand"
        browser.$('input.save').click()

        then:"The book is correct"
        browser.title == "Show Book"
        browser.$('li.fieldcontain div').text() == 'The Stand'
    }
}
