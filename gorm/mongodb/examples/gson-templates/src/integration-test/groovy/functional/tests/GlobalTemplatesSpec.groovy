package functional.tests

import grails.testing.mixin.integration.Integration
import grails.testing.spock.RunOnce
import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpResponse
import io.micronaut.http.HttpStatus
import org.junit.jupiter.api.BeforeEach
import spock.lang.AutoCleanup
import spock.lang.Shared
import spock.lang.Specification
import io.micronaut.http.client.HttpClient

/**
 * Created by graemerocher on 19/05/16.
 */
@Integration(applicationClass = Application)
class GlobalTemplatesSpec extends Specification {

    @Shared
    @AutoCleanup
    static HttpClient client

    @Shared
    static String baseUrl

    @RunOnce
    @BeforeEach
    void init() {
        baseUrl = "http://localhost:$serverPort"
        client = HttpClient.create(new URL(baseUrl))
    }

    void "Test errors view rendering"() {
        when:
        HttpRequest request = HttpRequest.GET('/place/show')
        HttpResponse<String> rsp = client.toBlocking().exchange(request, String)

        then:"The REST resource is created and the correct JSON is returned"
        rsp.status() == HttpStatus.OK
        rsp.body() == '{"location":{"type":"Point","coordinates":[10.0,10.0]},"name":"London"}'
    }
}
