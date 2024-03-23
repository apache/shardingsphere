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
import org.apache.shardingsphere.infra.util.yaml.YamlEngine;
import org.apache.shardingsphere.metadata.persist.node.GlobalNode;
import org.apache.shardingsphere.mode.spi.PersistRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collection;
import java.util.Collections;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class PropertiesPersistServiceTest {
    
    private PropertiesPersistService propertiesPersistService;
    
    @Mock
    private PersistRepository repository;
    
    private static final String DEFAULT_VERSION = "0";
    
    @BeforeEach
    void setUp() throws ReflectiveOperationException {
        propertiesPersistService = new PropertiesPersistService(repository);
    }
    
    @Test
    void testPersistWithNoActiveVersion() {
        // Arrange
        Properties props = new Properties();
        when(repository.getChildrenKeys(GlobalNode.getPropsVersionsNode())).thenReturn(Collections.emptyList());
        
        // Act
        propertiesPersistService.persist(props);
        
        // Assert
        verify(repository).persist(GlobalNode.getPropsActiveVersionNode(), DEFAULT_VERSION);
        verify(repository).persist(GlobalNode.getPropsVersionNode(DEFAULT_VERSION), YamlEngine.marshal(props));
    }
    
    @Test
    void testPersistWithExistingActiveVersion() {
        // Arrange
        Properties props = new Properties();
        when(repository.getChildrenKeys(GlobalNode.getPropsVersionsNode())).thenReturn(Collections.singletonList("1"));
        when(repository.getDirectly(GlobalNode.getPropsActiveVersionNode())).thenReturn("2");
        
        // Act
        propertiesPersistService.persist(props);
        
        // Assert
        verify(repository, never()).persist(GlobalNode.getPropsActiveVersionNode(), DEFAULT_VERSION);
        verify(repository).persist(GlobalNode.getPropsVersionNode("2"), YamlEngine.marshal(props));
    }
    
    @Test
    void testPersistConfig() {
        // Arrange
        Properties props = new Properties();
        when(repository.getChildrenKeys(GlobalNode.getPropsVersionsNode())).thenReturn(Collections.singletonList("1"));
        
        // Act
        Collection<MetaDataVersion> result = propertiesPersistService.persistConfig(props);
        
        // Assert
        assertEquals(1, result.size());
        MetaDataVersion metaDataVersion = result.iterator().next();
        assertEquals(GlobalNode.getPropsRootNode(), metaDataVersion.getKey());
        assertEquals("2", metaDataVersion.getNextActiveVersion());
    }
    
    @Test
    void testLoadWithExistingContent() {
        // Arrange
        String activeVersion = "1"; // Assume an active version exists
        String yamlContent = "firstName: \"John\"\nlastName: \"Doe\"\nage: 20";
        when(repository.getDirectly(GlobalNode.getPropsActiveVersionNode())).thenReturn(activeVersion);
        when(repository.getDirectly(GlobalNode.getPropsVersionNode(activeVersion))).thenReturn(yamlContent);
        
        // Act
        Properties loadedProps = propertiesPersistService.load();
        
        // Assert
        assertEquals("John", loadedProps.getProperty("firstName"));
        assertEquals("Doe", loadedProps.getProperty("lastName"));
        assertEquals(20, loadedProps.get("age"));
    }
    
}
