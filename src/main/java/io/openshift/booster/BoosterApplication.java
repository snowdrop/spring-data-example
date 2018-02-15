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

import io.openshift.booster.service.Book;
import io.openshift.booster.service.BookEnum;
import me.snowdrop.data.core.repository.config.EnableSnowdropRepositories;
import org.infinispan.Cache;
import org.infinispan.configuration.cache.ConfigurationBuilder;
import org.infinispan.configuration.cache.Index;
import org.infinispan.configuration.global.GlobalConfigurationBuilder;
import org.infinispan.manager.DefaultCacheManager;
import org.infinispan.manager.EmbeddedCacheManager;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
@EnableSnowdropRepositories
public class BoosterApplication {

    public static void main(String[] args) {
        SpringApplication.run(BoosterApplication.class, args);
    }

    @Bean(destroyMethod = "stop")
    public EmbeddedCacheManager createCacheManager() {
        GlobalConfigurationBuilder globalCfg = new GlobalConfigurationBuilder();
        globalCfg.globalJmxStatistics().allowDuplicateDomains(true).disable(); // get rid of this?

        ConfigurationBuilder cacheCfg = new ConfigurationBuilder();
        cacheCfg.jmxStatistics().disable();
        cacheCfg.indexing()
            .index(Index.ALL)
            .addIndexedEntity(Book.class)
            .addProperty("default.directory_provider", "local-heap")
            .addProperty("lucene_version", "LUCENE_CURRENT");

        return new DefaultCacheManager(globalCfg.build(), cacheCfg.build());
    }

    @Bean
    public Cache<Integer, Book> getStore(EmbeddedCacheManager cacheManager) {
        return cacheManager.getCache();
    }

    @Bean
    public BuildDataStore buildDataStore(Cache<Integer, Book> store) {
        return new BuildDataStore(store);
    }

    public class BuildDataStore implements ApplicationListener<ApplicationReadyEvent> {
        private Cache<Integer, Book> store;

        public BuildDataStore(Cache<Integer, Book> store) {
            this.store = store;
        }

        @Override
        public void onApplicationEvent(final ApplicationReadyEvent event) {
            try {
                store.clear();
                for (BookEnum fe : BookEnum.values()) {
                    Book f = fe.toBook();
                    store.put(f.getId(), f);
                }
            } catch (Exception e) {
                System.out.println("An error occurred trying to build data store: " + e.toString());
            }
        }
    }

}
