/*
 * Copyright 2016-2017 Red Hat, Inc, and individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.openshift.booster;

import static com.jayway.restassured.RestAssured.given;
import static com.jayway.restassured.RestAssured.when;
import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.Matchers.isEmptyString;
import static org.junit.Assert.assertFalse;

import java.time.LocalDate;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.jayway.restassured.RestAssured;
import com.jayway.restassured.http.ContentType;
import io.openshift.booster.service.Book;
import io.openshift.booster.service.BookEnum;
import io.openshift.booster.service.BookRepository;
import io.openshift.booster.service.BookUtils;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class BoosterApplicationTest {

    @Value("${local.server.port}")
    private int port;

    @Autowired
    private BookRepository bookRepository;

    @Before
    public void beforeTest() {
        bookRepository.deleteAll();
        RestAssured.baseURI = String.format("http://localhost:%d/api/books", port);
    }

    private Book save(Book book) {
        if (book.getId() == null) {
            book.setId(BookUtils.generateNextId(bookRepository.findAll()));
        }
        bookRepository.save(book);
        return book;
    }

    @Test
    public void testFindByTitle() {
        Book gf = save(BookEnum.GF1.toBook());
        when().get("/findTitle?title=godf")
            .then()
            .statusCode(200)
            .body("id", hasItems(gf.getId()))
            .body("title", hasItems(gf.getTitle()));
    }

    @Test
    public void testFindByWord() {
        Book sw = save(BookEnum.STAR_WARS.toBook());
        Book hp1 = save(BookEnum.HP1.toBook());
        when().get("/findWord?word=force")
            .then()
            .statusCode(200)
            .body("id", hasItems(sw.getId()))
            .body("title", hasItems(sw.getTitle()));
        when().get("/findWord?word=magic")
            .then()
            .statusCode(200)
            .body("id", hasItems(hp1.getId()))
            .body("title", hasItems(hp1.getTitle()));
    }

    @Test
    public void testFindByExample() {
        Book ccf = save(BookEnum.CCF.toBook());
        Map<String, String> example = new HashMap<>();
        example.put("orderBy", "releaseDate");
        example.put("author", "Roald Dahl");
        given().contentType(ContentType.JSON)
            .body(example)
            .when()
            .post("/findForm")
            .then()
            .statusCode(200)
            .body("id", hasItems(ccf.getId()))
            .body("title", hasItems(ccf.getTitle()));
    }

    @Test
    public void testGetAll() {
        Book sw = save(BookEnum.STAR_WARS.toBook());
        Book lotr = save(BookEnum.LOTR.toBook());
        when().get()
            .then()
            .statusCode(200)
            .body("id", hasItems(sw.getId(), lotr.getId()))
            .body("title", hasItems(sw.getTitle(), lotr.getTitle()));
    }

    @Test
    public void testGetEmptyArray() {
        when().get()
            .then()
            .statusCode(200)
            .body(is("[]"));
    }

    @Test
    public void testGetOne() {
        Book sw = save(BookEnum.STAR_WARS.toBook());
        when().get(String.valueOf(sw.getId()))
            .then()
            .statusCode(200)
            .body("id", is(sw.getId()))
            .body("title", is(sw.getTitle()));
    }

    @Test
    public void testGetNotExisting() {
        when().get("0")
            .then()
            .statusCode(404);
    }

    @Test
    public void testPost() {
        Map<String, String> book = new HashMap<>();
        book.put("title", "Kubernetes in Action");
        book.put("author", "Marko Luksa");
        book.put("content", "K8s. OpenShift.");
        book.put("releaseDate", LocalDate.of(2017, 8, 1).toString());
        given().contentType(ContentType.JSON)
            .body(book)
            .when()
            .post()
            .then()
            .statusCode(201)
            .body("id", not(isEmptyString()))
            .body("title", is("Kubernetes in Action"));
    }

    @Test
    public void testPostWithWrongPayload() {
        given().contentType(ContentType.JSON)
            .body(Collections.singletonMap("id", 0))
            .when()
            .post()
            .then()
            .statusCode(422);
    }

    @Test
    public void testPostWithNonJsonPayload() {
        given().contentType(ContentType.XML)
            .when()
            .post()
            .then()
            .statusCode(415);
    }

    @Test
    public void testPostWithEmptyPayload() {
        given().contentType(ContentType.JSON)
            .when()
            .post()
            .then()
            .statusCode(415);
    }

    @Test
    public void testPut() {
        Book sw = save(BookEnum.STAR_WARS.toBook());

        Map<String, String> book = new HashMap<>();
        book.put("title", "Kubernetes in Action");
        book.put("author", "Marko Luksa");
        book.put("content", "K8s. OpenShift.");
        book.put("releaseDate", LocalDate.of(2017, 8, 1).toString());

        given().contentType(ContentType.JSON)
            .body(book)
            .when()
            .put(String.valueOf(sw.getId()))
            .then()
            .statusCode(200)
            .body("id", is(sw.getId()))
            .body("title", is("Kubernetes in Action"));
    }

    @Test
    public void testPutNotExisting() {
        Map<String, String> book = new HashMap<>();
        book.put("title", "Kubernetes in Action");
        book.put("author", "Marko Luksa");
        book.put("content", "K8s. OpenShift.");
        book.put("releaseDate", LocalDate.of(2017, 8, 1).toString());

        given().contentType(ContentType.JSON)
            .body(book)
            .when()
            .put("/0")
            .then()
            .statusCode(404);
    }

    @Test
    public void testPutWithWrongPayload() {
        Book sw = save(BookEnum.STAR_WARS.toBook());
        given().contentType(ContentType.JSON)
            .body(Collections.singletonMap("id", 0))
            .when()
            .put(String.valueOf(sw.getId()))
            .then()
            .statusCode(422);
    }

    @Test
    public void testPutWithNonJsonPayload() {
        Book sw = save(BookEnum.STAR_WARS.toBook());
        given().contentType(ContentType.XML)
            .when()
            .put(String.valueOf(sw.getId()))
            .then()
            .statusCode(415);
    }

    @Test
    public void testPutWithEmptyPayload() {
        Book sw = save(BookEnum.STAR_WARS.toBook());
        given().contentType(ContentType.JSON)
            .when()
            .put(String.valueOf(sw.getId()))
            .then()
            .statusCode(415);
    }

    @Test
    public void testDelete() {
        Book sw = save(BookEnum.STAR_WARS.toBook());
        when().delete(String.valueOf(sw.getId()))
            .then()
            .statusCode(204);
        assertFalse(bookRepository.existsById(sw.getId()));
    }

    @Test
    public void testDeleteNotExisting() {
        when().delete("/0")
            .then()
            .statusCode(404);
    }

}
