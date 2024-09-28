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

package org.apache.shardingsphere.metadata.persist.service.config.global;

import org.apache.shardingsphere.metadata.persist.service.version.MetaDataVersionPersistService;
import org.apache.shardingsphere.mode.spi.PersistRepository;
import org.apache.shardingsphere.test.util.PropertiesBuilder;
import org.apache.shardingsphere.test.util.PropertiesBuilder.Property;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PropertiesPersistServiceTest {
    
    private PropertiesPersistService persistService;
    
    @Mock
    private PersistRepository repository;
    
    @Mock
    private MetaDataVersionPersistService metaDataVersionPersistService;
    
    @BeforeEach
    void setUp() {
        persistService = new PropertiesPersistService(repository, metaDataVersionPersistService);
        when(repository.query("/props/active_version")).thenReturn("0");
    }
    
    @Test
    void assertLoadWithEmptyContent() {
        assertTrue(persistService.load().isEmpty());
    }
    
    @Test
    void assertLoad() {
        when(repository.query("/props/versions/0")).thenReturn("{\"k\":\"v\"}");
        Properties props = persistService.load();
        assertThat(props.size(), is(1));
        assertThat(props.getProperty("k"), is("v"));
    }
    
    @Test
    void assertPersistWithEmptyActiveVersion() {
        when(repository.query("/props/active_version")).thenReturn("");
        persistService.persist(PropertiesBuilder.build(new Property("k", "v")));
        verify(repository).persist("/props/versions/0", "k: v" + System.lineSeparator());
        verify(repository).persist("/props/active_version", "0");
        verify(metaDataVersionPersistService).switchActiveVersion(any());
    }
    
    @Test
    void assertPersistWithActiveVersion() {
        when(repository.getChildrenKeys("/props/versions")).thenReturn(Collections.singletonList("10"));
        persistService.persist(PropertiesBuilder.build(new Property("k", "v")));
        verify(repository).persist("/props/versions/11", "k: v" + System.lineSeparator());
        verify(metaDataVersionPersistService).switchActiveVersion(any());
    }
}
