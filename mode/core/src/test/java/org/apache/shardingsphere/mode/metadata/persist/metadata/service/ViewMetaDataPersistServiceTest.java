/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.shardingsphere.mode.metadata.persist.metadata.service;

import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereView;
import org.apache.shardingsphere.mode.metadata.persist.version.VersionPersistService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.Collections;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertFalse;

class ViewMetaDataPersistServiceTest {
    
    private ViewMetaDataPersistService persistService;
    
    private FixturePersistRepository repository;
    
    @BeforeEach
    void setUp() {
        repository = new FixturePersistRepository();
        persistService = new ViewMetaDataPersistService(repository, new VersionPersistService(repository));
    }
    
    @Test
    void assertLoad() {
        repository.persist("/metadata/foo_db/schemas/foo_schema/views/foo_view/active_version", "0");
        repository.persist("/metadata/foo_db/schemas/foo_schema/views/foo_view/versions/0", "{name: foo_view}");
        Collection<ShardingSphereView> actual = persistService.load("foo_db", "foo_schema");
        assertThat(actual.size(), is(1));
        assertThat(actual.iterator().next().getName(), is("foo_view"));
    }
    
    @Test
    void assertLoadWhenActiveVersionIsEmpty() {
        repository.persist("/metadata/foo_db/schemas/foo_schema/views/foo_view/active_version", "");
        assertFalse(persistService.load("foo_db", "foo_schema", "foo_view").isPresent());
    }
    
    @Test
    void assertLoadWhenViewContentIsEmpty() {
        repository.persist("/metadata/foo_db/schemas/foo_schema/views/foo_view/active_version", "0");
        repository.persist("/metadata/foo_db/schemas/foo_schema/views/foo_view/versions/0", "");
        assertFalse(persistService.load("foo_db", "foo_schema", "foo_view").isPresent());
    }
    
    @Test
    void assertLoadWithRawNamePath() {
        repository.persist("/metadata/foo_db/schemas/foo_schema/views/Foo_View/active_version", "0");
        repository.persist("/metadata/foo_db/schemas/foo_schema/views/Foo_View/versions/0", "{name: Foo_View, viewDefinition: select 1}");
        assertThat(persistService.load("foo_db", "foo_schema", "Foo_View").map(ShardingSphereView::getName).orElse(""), is("Foo_View"));
    }
    
    @Test
    void assertLoadWithRawNamePathForCollection() {
        repository.persist("/metadata/foo_db/schemas/foo_schema/views/Foo_View/active_version", "1");
        repository.persist("/metadata/foo_db/schemas/foo_schema/views/Foo_View/versions/1", "{name: Foo_View, viewDefinition: select 2}");
        Collection<ShardingSphereView> actual = persistService.load("foo_db", "foo_schema");
        assertThat(actual.size(), is(1));
        assertThat(actual.iterator().next().getViewDefinition(), is("select 2"));
    }
    
    @Test
    void assertPersistWithoutVersion() {
        persistService.persist("foo_db", "foo_schema", Collections.singleton(new ShardingSphereView("Foo_View", "select 1")));
        assertThat(repository.query("/metadata/foo_db/schemas/foo_schema/views/Foo_View/active_version"), is("0"));
        assertThat(repository.query("/metadata/foo_db/schemas/foo_schema/views/Foo_View/versions/0"), containsString("name: Foo_View"));
    }
    
    @Test
    void assertPersistWithVersion() {
        repository.persist("/metadata/foo_db/schemas/foo_schema/views/Foo_View/versions/10", "old");
        persistService.persist("foo_db", "foo_schema", Collections.singleton(new ShardingSphereView("Foo_View", "select 1")));
        assertThat(repository.query("/metadata/foo_db/schemas/foo_schema/views/Foo_View/active_version"), is("11"));
        assertThat(repository.query("/metadata/foo_db/schemas/foo_schema/views/Foo_View/versions/11"), containsString("name: Foo_View"));
    }
    
    @Test
    void assertDrop() {
        repository.persist("/metadata/foo_db/schemas/foo_schema/views/Foo_View/active_version", "0");
        persistService.drop("foo_db", "foo_schema", "Foo_View");
        assertFalse(repository.isExisted("/metadata/foo_db/schemas/foo_schema/views/Foo_View"));
    }
}
