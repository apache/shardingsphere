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

import org.apache.shardingsphere.infra.metadata.version.MetaDataVersion;
import org.apache.shardingsphere.mode.spi.PersistRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collection;
import java.util.Properties;

import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
public class PropertiesPersistServiceTest {
    
    private PropertiesPersistService propertiesPersistService;
    
    @BeforeEach
    void setUp() throws ReflectiveOperationException {
        propertiesPersistService = new PropertiesPersistService(mock(PersistRepository.class));
    }
    
    @Test
    void testPersist() {
        propertiesPersistService.persist(new Properties());
    }
    
    @Test
    void testPersistConfig() {
        Collection<MetaDataVersion> collection = propertiesPersistService.persistConfig(new Properties());
        Assertions.assertEquals(collection.size(), 1);
    }
    
    @Test
    void testLoad() {
        Properties properties = propertiesPersistService.load();
        Assertions.assertNotNull(properties);
    }
    
}
