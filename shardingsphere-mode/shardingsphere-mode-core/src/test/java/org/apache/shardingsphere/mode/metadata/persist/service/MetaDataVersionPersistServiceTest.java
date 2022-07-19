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

package org.apache.shardingsphere.mode.metadata.persist.service;

import org.apache.shardingsphere.mode.metadata.persist.node.DatabaseMetaDataNode;
import org.apache.shardingsphere.mode.persist.PersistRepository;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public final class MetaDataVersionPersistServiceTest {

    private PersistRepository repository;

    private MetaDataVersionPersistService metaDataVersionPersistService;

    @Before
    public void setUp() {
        repository = mock(PersistRepository.class);
        when(repository.get(contains("foo_db"))).thenReturn("1");
        metaDataVersionPersistService = new MetaDataVersionPersistService(repository);
    }

    @Test
    public void assertGetActiveVersion() {
        assertTrue(metaDataVersionPersistService.getActiveVersion("foo_db").isPresent());
        verify(repository).get(DatabaseMetaDataNode.getActiveVersionPath("foo_db"));
    }

    @Test
    public void assertIsActiveVersion() {
        assertTrue(metaDataVersionPersistService.isActiveVersion("foo_db", "1"));
    }

    @Test
    public void assertCreateNewVersion() {
        assertFalse(metaDataVersionPersistService.createNewVersion("new_db").isPresent());
        assertThat(metaDataVersionPersistService.createNewVersion("foo_db").get(), equalTo("2"));
        verify(repository, times(2)).persist(contains("foo_db"), anyString());
    }

    @Test
    public void assertPersistActiveVersion() {
        metaDataVersionPersistService.persistActiveVersion("foo_db", "2");
        verify(repository).persist(DatabaseMetaDataNode.getActiveVersionPath("foo_db"), "2");
    }

    @Test
    public void assertDeleteVersion() {
        metaDataVersionPersistService.deleteVersion("foo_db", "1");
        verify(repository).delete(DatabaseMetaDataNode.getDatabaseVersionPath("foo_db", "1"));
    }
}
