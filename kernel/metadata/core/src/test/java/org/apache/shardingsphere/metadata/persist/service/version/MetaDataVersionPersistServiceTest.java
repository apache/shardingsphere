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

package org.apache.shardingsphere.metadata.persist.service.version;

import org.apache.shardingsphere.infra.metadata.version.MetaDataVersion;
import org.apache.shardingsphere.mode.spi.PersistRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class MetaDataVersionPersistServiceTest {
    
    @Mock
    private PersistRepository repository;
    
    private MetaDataVersionPersistService metaDataVersionPersistService;
    
    @BeforeEach
    void setUp() throws ReflectiveOperationException {
        metaDataVersionPersistService = new MetaDataVersionPersistService(repository);
    }
    
    @Test
    void testSwitchActiveVersion() {
        Collection<MetaDataVersion> expect = createMetaData();
        metaDataVersionPersistService.switchActiveVersion(expect);
        
        verify(repository, times(1)).persist(anyString(), anyString());
        
        verify(repository, times(1)).delete(anyString());
    }
    
    @Test
    void testGetActiveVersionByFullPath() {
        when(repository.getDirectly(anyString())).thenReturn("0");
        
        String actual = metaDataVersionPersistService.getActiveVersionByFullPath("123");
        assertEquals(actual, "0");
    }
    
    @Test
    void testGetVersionPathByActiveVersion() {
        when(repository.getDirectly(anyString())).thenReturn("0");
        
        String actual = metaDataVersionPersistService.getVersionPathByActiveVersion("123", "234");
        
        assertEquals(actual, "0");
    }
    
    private static List<MetaDataVersion> createMetaData() {
        MetaDataVersion metaDataVersion = new MetaDataVersion("123", "0", "1");
        return Collections.singletonList(metaDataVersion);
    }
    
}
