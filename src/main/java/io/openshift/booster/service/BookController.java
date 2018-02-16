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

package io.openshift.booster.service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Spliterator;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import io.openshift.booster.exception.NotFoundException;
import io.openshift.booster.exception.UnprocessableEntityException;
import io.openshift.booster.exception.UnsupportedMediaTypeException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

@Controller
@RequestMapping(value = "/api/books")
public class BookController {

    private final BookRepository repository;

    @Autowired
    public BookController(BookRepository repository) {
        this.repository = repository;
    }

    private Book save(Book book) {
        return repository.save(book);
    }

    @ResponseBody
    @GetMapping(path = "/findAuthors", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<Book> findAuthors(@RequestParam(name = "author") String author) {
        return repository.findByAuthorLike(author);
    }

    @ResponseBody
    @GetMapping(path = "/findTitle", produces = MediaType.APPLICATION_JSON_VALUE)
    public Stream<Book> findByTitle(@RequestParam(name = "title") String title) {
        return repository.findByTitleLike(title);
    }

    @ResponseBody
    @GetMapping(path = "/findWord", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<Book> findByContent(@RequestParam(name = "word") String word) {
        return repository.findByContentContains(word, PageRequest.of(0, 100));
    }

    @ResponseBody
    @PostMapping(path = "/findForm", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public List<Book> findForm(@RequestBody BookQuery example) {
        // TODO -- better intersection logic / code
        Pageable pageable = PageRequest.of(0, example.getMaxResults(), Sort.Direction.fromString(example.getDirection()), example.getOrderBy());
        Collection<Book> books = new LinkedHashSet<>();
        if (example.getReleaseDate() != null) {
            List<Book> byReleaseDate = repository.findByReleaseDate(example.getReleaseDate(), pageable);
            books.addAll(byReleaseDate);
        }
        if (StringUtils.hasLength(example.getAuthor())) {
            List<Book> byAuthor = repository.findByAuthor(example.getAuthor(), pageable);
            if (example.getReleaseDate() != null) {
                byAuthor.retainAll(books);
            }
            books = byAuthor;
        }
        if (StringUtils.hasLength(example.getContent())) {
            List<Book> byContentContains = repository.findByContentContains(example.getContent(), pageable);
            if (example.getReleaseDate() != null && StringUtils.hasLength(example.getAuthor())) {
                byContentContains.retainAll(books);
            }
            books = byContentContains;
        }
        return new ArrayList<>(books);
    }

    @ResponseBody
    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public Book get(@PathVariable("id") Integer id) {
        verifyBookExists(id);

        return repository.findById(id).get();
    }

    @ResponseBody
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public List<Book> getAll() {
        Spliterator<Book> fruits = repository.findAll().spliterator();
        return StreamSupport.stream(fruits, false).collect(Collectors.toList());
    }

    @ResponseBody
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public Book post(@RequestBody(required = false) Book book) {
        Integer newId = verifyCorrectPayload(book);
        book.setId(newId);

        return save(book);
    }

    @ResponseBody
    @ResponseStatus(HttpStatus.OK)
    @PutMapping(value = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public Book put(@PathVariable("id") Integer id, @RequestBody(required = false) Book book) {
        verifyBookExists(id);
        verifyCorrectPayload(book);

        book.setId(id);
        return save(book);
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping("/{id}")
    public void delete(@PathVariable("id") Integer id) {
        verifyBookExists(id);

        repository.deleteById(id);
    }

    private void verifyBookExists(Integer id) {
        if (!repository.existsById(id)) {
            throw new NotFoundException(String.format("Book with id=%d was not found", id));
        }
    }

    private Integer verifyCorrectPayload(Book book) {
        if (Objects.isNull(book)) {
            throw new UnsupportedMediaTypeException("Book cannot be null");
        }

        if (!Objects.isNull(book.getId())) {
            throw new UnprocessableEntityException("Id field must be generated");
        }

        return BookUtils.generateNextId(getAll());
    }

}
