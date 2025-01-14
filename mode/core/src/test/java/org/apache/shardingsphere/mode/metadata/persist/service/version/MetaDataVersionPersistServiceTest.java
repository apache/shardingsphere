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

package org.apache.shardingsphere.mode.metadata.persist.service.version;

import org.apache.shardingsphere.infra.metadata.version.MetaDataVersion;
import org.apache.shardingsphere.mode.spi.repository.PersistRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
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
    void assertSwitchActiveVersion() {
        when(repository.getChildrenKeys("foo_db/versions")).thenReturn(Arrays.asList("1", "0"));
        persistService.switchActiveVersion(Arrays.asList(new MetaDataVersion("foo_db", "0", "1"), new MetaDataVersion("bar_db", "2", "2")));
        verify(repository).persist("foo_db/active_version", "1");
        verify(repository).delete("foo_db/versions/0");
    }
    
    @Test
    void assertGetVersionPathByActiveVersion() {
        when(repository.query("foo_db/versions/1")).thenReturn("foo_path");
        assertThat(persistService.getVersionPathByActiveVersion("foo_db/active_version", "1"), is("foo_path"));
    }
    
    @Test
    void assertGetVersions() {
        when(repository.getChildrenKeys("foo_db/versions")).thenReturn(Arrays.asList("1", "0", "2", "10"));
        assertThat(persistService.getVersions("foo_db/versions"), is(Arrays.asList("10", "2", "1", "0")));
    }
}
