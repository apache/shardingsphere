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

package org.apache.shardingsphere.mode.metadata.persist.version;

import org.apache.shardingsphere.mode.node.path.type.version.VersionNodePath;
import org.apache.shardingsphere.mode.spi.repository.PersistRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MetaDataVersionPersistServiceTest {
    
    private MetaDataVersionPersistService persistService;
    
    @Mock
    private PersistRepository repository;
    
    @BeforeEach
    void setUp() {
        persistService = new MetaDataVersionPersistService(repository);
    }
    
    @Test
    void assertPersistWithNewMetaData() {
        assertThat(persistService.persist(new VersionNodePath("foo_db"), "foo_metadata"), is(0));
        verify(repository).persist("foo_db/versions/0", "foo_metadata");
        verify(repository).persist("foo_db/active_version", "0");
        verify(repository, times(0)).delete(any());
    }
    
    @Test
    void assertPersistWithExistedMetaData() {
        when(repository.getChildrenKeys("foo_db/versions")).thenReturn(Arrays.asList("2", "1", "0"));
        assertThat(persistService.persist(new VersionNodePath("foo_db"), "foo_metadata"), is(3));
        verify(repository).persist("foo_db/versions/3", "foo_metadata");
        verify(repository).persist("foo_db/active_version", "3");
        verify(repository).delete("foo_db/versions/0");
        verify(repository).delete("foo_db/versions/1");
        verify(repository).delete("foo_db/versions/2");
    }
    
    @Test
    void assertLoadContent() {
        when(repository.query("foo_db/versions/1")).thenReturn("foo_path");
        assertThat(persistService.loadContent("foo_db/active_version", 1), is("foo_path"));
    }
}
