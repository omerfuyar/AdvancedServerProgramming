package com.dsu.hello_server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

// @SpringBootApplication = three things in one:
//  - @Configuration: this class may define beans
//  - @EnableAutoConfiguration: look at the classpath and configure what it finds
//    (web starter -> Tomcat + Jackson, data-jpa + MySQL driver -> DataSource + Hibernate)
//  - @ComponentScan: find @Service/@Repository/@RestController classes in THIS package
//    and its subpackages. A class outside com.dsu.hello_server is never found.
@SpringBootApplication
public class HelloServerApplication {

    public static void main(String[] args) {
        // Starts the Spring container: scans classes, creates every bean once (singleton),
        // injects them into each other's constructors, validates entities against the tables,
        // then starts embedded Tomcat on port 8080. main() returns, but the server keeps running.
        SpringApplication.run(HelloServerApplication.class, args);
    }
}
