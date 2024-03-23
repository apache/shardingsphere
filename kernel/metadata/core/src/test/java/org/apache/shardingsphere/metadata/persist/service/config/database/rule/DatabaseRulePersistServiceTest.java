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

package org.apache.shardingsphere.metadata.persist.service.config.database.rule;

import org.apache.shardingsphere.infra.config.rule.RuleConfiguration;
import org.apache.shardingsphere.infra.metadata.version.MetaDataVersion;
import org.apache.shardingsphere.metadata.persist.fixture.YamlDataNodeRuleConfigurationFixture;
import org.apache.shardingsphere.mode.spi.PersistRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collection;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class DatabaseRulePersistServiceTest {
    
    @Mock
    private PersistRepository repository;
    
    private DatabaseRulePersistService databaseRuleService;
    
    @BeforeEach
    void setUp() throws ReflectiveOperationException {
        databaseRuleService = new DatabaseRulePersistService(repository);
    }
    
    @Test
    void testPersistVersionExists() {
        // Arrange
        String databaseName = "testDatabase";
        Collection<RuleConfiguration> expectRuleConfigs = buildRuleConfigs();
        when(repository.getDirectly(anyString())).thenReturn("0");
        
        // Act
        databaseRuleService.persist(databaseName, expectRuleConfigs);
        
        // Assert
        verify(repository, times(1)).persist(anyString(), anyString());
    }
    
    @Test
    void testPersistVersionNotExists() {
        // Arrange
        String databaseName = "testDatabase";
        Collection<RuleConfiguration> expectRuleConfigs = buildRuleConfigs();
        
        // Act
        databaseRuleService.persist(databaseName, expectRuleConfigs);
        
        // Assert
        verify(repository, times(2)).persist(anyString(), anyString());
    }
    
    @Test
    void testLoad() {
        String databaseName = "testDatabase";
        when(repository.getChildrenKeys(anyString()))
                .thenReturn(Collections.singletonList("active_version"))
                .thenReturn(Collections.emptyList());
        when(repository.getDirectly(anyString())).thenReturn("0");
        
        Collection<RuleConfiguration> actual = databaseRuleService.load(databaseName);
        
        assertEquals(1, actual.size());
    }
    
    @Test
    void testDelete() {
        String databaseName = "testDatabase";
        databaseRuleService.delete(databaseName, "foo");
        
        verify(repository, times(1)).delete(anyString());
    }
    
    @Test
    void testDeleteConfig() {
        // Arrange
        String databaseName = "testDatabase";
        Collection<RuleConfiguration> expectRuleConfigs = buildRuleConfigs();
        
        Collection<MetaDataVersion> actual = databaseRuleService.deleteConfig(databaseName, expectRuleConfigs);
        
        verify(repository, times(expectRuleConfigs.size())).delete(anyString());
        
        assertEquals(expectRuleConfigs.size(), actual.size());
    }
    
    @Test
    void testPersistConfig() {
        // Arrange
        String databaseName = "testDatabase";
        Collection<RuleConfiguration> expectRuleConfigs = buildRuleConfigs();
        when(repository.getDirectly(anyString())).thenReturn("0");
        
        
        Collection<MetaDataVersion> actual = databaseRuleService.persistConfig(databaseName, expectRuleConfigs);
        
        verify(repository, times(expectRuleConfigs.size())).persist(anyString(), anyString());
        
        assertEquals(expectRuleConfigs.size(), actual.size());
    }
    
    private Collection<RuleConfiguration> buildRuleConfigs() {
        YamlDataNodeRuleConfigurationFixture yamlDataNodeRuleConfigurationFixture = new YamlDataNodeRuleConfigurationFixture();
        yamlDataNodeRuleConfigurationFixture.setKey("foo");
        yamlDataNodeRuleConfigurationFixture.setValue("foo_value");
        return Collections.singletonList(yamlDataNodeRuleConfigurationFixture);
    }
    
}
