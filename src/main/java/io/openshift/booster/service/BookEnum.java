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

import java.time.LocalDate;

public enum BookEnum {
    STAR_WARS(1, "Star Wars: From the Adventures of Luke Skywalker", "George Lucas", "Star wars. Force. Jedi. Joda.", LocalDate.of(1976, 11, 12)),
    LOTR(2, "The Lord of the Rings", "J. R. R. Tolkien", "Hobbit. Ring. Sauron.", LocalDate.of(1954, 7, 29)),
    HP1(3, "Harry Potter and the Philosopher's Stone", "J. K. Rowling", "Wizard. Stone. Magic.", LocalDate.of(1997, 6, 26)),
    GF1(4, "The Godfather", "Mario Puzo", "Mafia. Vito Corleone.", LocalDate.of(1969, 3, 10)),
    FIRM(5, "The Firm", "John Grisham", "Lawyers. Corruption. Law.", LocalDate.of(1991, 2, 1)),
    JURASSIC_PARK(6, "Jurassic park", "Michael Crichton", "Dinosaurs. Genetic engineering. Amusement park.", LocalDate.of(1990, 11, 20)),
    SSR(7, "The Shawshank redemption", "Mark Kermode", "Prison. Murder.", LocalDate.of(2003, 7, 1)),
    CCF(8, "Charlie and the Chocolate Factory", "Roald Dahl", "Adventures of young Charlie Bucket inside the chocolate factory of eccentric chocolatier Willy Wonka.", LocalDate.of(1964, 1, 17));

    private int id;
    private String title;
    private String author;
    private String content;
    private LocalDate releaseDate;

    BookEnum(int id, String title, String author, String content, LocalDate releaseDate) {
        this.id = id;
        this.title = title;
        this.author = author;
        this.content = content;
        this.releaseDate = releaseDate;
    }

    public Book toBook() {
        return new Book(id, title, author, content, releaseDate);
    }
}
