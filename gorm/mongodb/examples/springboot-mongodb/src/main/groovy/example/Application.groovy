package example

import grails.gorm.transactions.Transactional
import org.springframework.boot.CommandLineRunner
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.testcontainers.containers.MongoDBContainer
import org.testcontainers.utility.DockerImageName

@SpringBootApplication
class Application implements CommandLineRunner {

    static void main(String[] args) {
        try (
                MongoDBContainer dbContainer = new MongoDBContainer(DockerImageName.parse("mongo:${System.getProperty("mongodbContainerVersion", "7.0.16")}"))
        ) {

            dbContainer.start()

            System.setProperty('spring.data.mongodb.uri', dbContainer.getReplicaSetUrl('myDb'))
            SpringApplication.run(Application.class, args)
        }
    }

    @Override
    @Transactional
    void run(String... args) throws Exception {
        new Book(title: "The Stand").save()
        new Book(title: "The Shining").save()
        new Book(title: "It").save()
    }
}