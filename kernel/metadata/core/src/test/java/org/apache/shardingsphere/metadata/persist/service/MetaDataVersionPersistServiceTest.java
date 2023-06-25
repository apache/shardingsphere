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

package org.apache.shardingsphere.metadata.persist.service;

import org.apache.shardingsphere.infra.metadata.version.MetaDataVersion;
import org.apache.shardingsphere.metadata.persist.service.version.MetaDataVersionPersistService;
import org.apache.shardingsphere.mode.spi.PersistRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collections;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class MetaDataVersionPersistServiceTest {
    
    private PersistRepository repository;
    
    private MetaDataVersionPersistService metaDataVersionPersistService;
    
    @BeforeEach
    void setUp() {
        repository = mock(PersistRepository.class);
        metaDataVersionPersistService = new MetaDataVersionPersistService(repository);
    }
    
    @Test
    void assertSwitchActiveVersion() {
        metaDataVersionPersistService.switchActiveVersion(Collections.singletonList(new MetaDataVersion("foo_key", "0", "1")));
        verify(repository).persist("foo_key/active_version", "1");
    }
}
