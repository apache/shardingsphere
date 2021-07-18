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

package org.apache.shardingsphere.infra.config.persist;

import lombok.SneakyThrows;
import org.apache.shardingsphere.infra.config.RuleConfiguration;
import org.apache.shardingsphere.infra.config.datasource.DataSourceConfiguration;
import org.apache.shardingsphere.infra.config.persist.repository.ConfigCenterRepository;
import org.apache.shardingsphere.infra.config.persist.service.impl.DataSourcePersistService;
import org.apache.shardingsphere.infra.config.persist.service.impl.GlobalRulePersistService;
import org.apache.shardingsphere.infra.config.persist.service.impl.PropertiesPersistService;
import org.apache.shardingsphere.infra.config.persist.service.impl.SchemaRulePersistService;
import org.apache.shardingsphere.infra.config.properties.ConfigurationPropertyKey;
import org.apache.shardingsphere.infra.yaml.engine.YamlEngine;
import org.apache.shardingsphere.infra.yaml.config.swapper.YamlRuleConfigurationSwapperEngine;
import org.apache.shardingsphere.test.mock.MockedDataSource;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import javax.sql.DataSource;
import java.io.IOException;
import java.lang.reflect.Field;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public final class ConfigCenterTest {
    
    private static final String SCHEMA_RULE_YAML = "yaml/configcenter/data-schema-rule.yaml";
    
    @Mock
    private DataSourcePersistService dataSourceService;
    
    @Mock
    private SchemaRulePersistService schemaRuleService;
    
    @Mock
    private GlobalRulePersistService globalRuleService;
    
    @Mock
    private PropertiesPersistService propsService;
    
    private ConfigCenter configCenter;
    
    @Before
    public void setUp() throws ReflectiveOperationException {
        configCenter = new ConfigCenter(mock(ConfigCenterRepository.class));
        setField("dataSourceService", dataSourceService);
        setField("schemaRuleService", schemaRuleService);
        setField("globalRuleService", globalRuleService);
        setField("propsService", propsService);
    }
    
    private void setField(final String name, final Object value) throws ReflectiveOperationException {
        Field field = configCenter.getClass().getDeclaredField(name);
        field.setAccessible(true);
        field.set(configCenter, value);
    }
    
    @Test
    public void assertPersistConfigurations() {
        Map<String, DataSourceConfiguration> dataSourceConfigs = createDataSourceConfigurations();
        Collection<RuleConfiguration> schemaRuleConfigs = createRuleConfigurations();
        Collection<RuleConfiguration> globalRuleConfigs = createGlobalRuleConfigurations();
        Properties props = createProperties();
        configCenter.persistConfigurations(
                Collections.singletonMap("foo_db", dataSourceConfigs), Collections.singletonMap("foo_db", schemaRuleConfigs), globalRuleConfigs, props, false);
        verify(dataSourceService).persist("foo_db", dataSourceConfigs, false);
        verify(schemaRuleService).persist("foo_db", schemaRuleConfigs, false);
        verify(globalRuleService).persist(globalRuleConfigs, false);
        verify(propsService).persist(props, false);
    }
    
    private Map<String, DataSourceConfiguration> createDataSourceConfigurations() {
        return createDataSourceMap().entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, entry -> 
                DataSourceConfiguration.getDataSourceConfiguration(entry.getValue()), (oldValue, currentValue) -> oldValue, LinkedHashMap::new));
    }
    
    private Map<String, DataSource> createDataSourceMap() {
        Map<String, DataSource> result = new LinkedHashMap<>(2, 1);
        result.put("ds_0", createDataSource("ds_0"));
        result.put("ds_1", createDataSource("ds_1"));
        return result;
    }
    
    private DataSource createDataSource(final String name) {
        MockedDataSource result = new MockedDataSource();
        result.setDriverClassName("com.mysql.jdbc.Driver");
        result.setUrl("jdbc:mysql://localhost:3306/" + name);
        result.setUsername("root");
        result.setPassword("root");
        return result;
    }
    
    @SuppressWarnings("unchecked")
    private Collection<RuleConfiguration> createRuleConfigurations() {
        return new YamlRuleConfigurationSwapperEngine().swapToRuleConfigurations(YamlEngine.unmarshal(readYAML(SCHEMA_RULE_YAML), Collection.class));
    }
    
    private Collection<RuleConfiguration> createGlobalRuleConfigurations() {
        return Collections.emptyList();
    }
    
    private Properties createProperties() {
        Properties result = new Properties();
        result.put(ConfigurationPropertyKey.SQL_SHOW.getKey(), Boolean.FALSE);
        return result;
    }
    
    @SneakyThrows({IOException.class, URISyntaxException.class})
    private String readYAML(final String yamlFile) {
        return Files.readAllLines(Paths.get(ClassLoader.getSystemResource(yamlFile).toURI())).stream().map(each -> each + System.lineSeparator()).collect(Collectors.joining());
    }
}
