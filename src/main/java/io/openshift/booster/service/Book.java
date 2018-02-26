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
import java.time.format.DateTimeFormatter;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import org.hibernate.search.annotations.Analyze;
import org.hibernate.search.annotations.DocumentId;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.Indexed;
import org.hibernate.search.annotations.SortableField;
import org.hibernate.search.annotations.Store;
import org.springframework.data.annotation.Id;

@Indexed
public class Book {

    @Id
    @DocumentId
    private Integer id;

    @Field
    private String title;

    @Field(analyze = Analyze.NO)
    @SortableField
    private String author;

    @Field(store = Store.NO)
    private String content;

    @Field(analyze = Analyze.NO)
    @SortableField
    @JsonSerialize(using = IsoLocalDateSerializer.class)
    private LocalDate releaseDate;

    public Book() {
    }

    public Book(Integer id, String title, String author, String content, LocalDate releaseDate) {
        this.id = id;
        this.title = title;
        this.author = author;
        this.content = content;
        this.releaseDate = releaseDate;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public LocalDate getReleaseDate() {
        return releaseDate;
    }

    public void setReleaseDate(LocalDate releaseDate) {
        this.releaseDate = releaseDate;
    }

    @Override
    public int hashCode() {
        return (getId() != null ? getId() : 0);
    }

    @Override
    public boolean equals(Object obj) {
        return (obj instanceof Book) && (Book.class.cast(obj).getId().equals(getId()));
    }

    public static class IsoLocalDateSerializer extends LocalDateSerializer {
        protected IsoLocalDateSerializer() {
            super(DateTimeFormatter.ISO_DATE);
        }
    }
}
