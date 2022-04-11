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

package org.apache.shardingsphere.mode.metadata.persist;

import lombok.SneakyThrows;
import org.apache.shardingsphere.infra.config.RuleConfiguration;
import org.apache.shardingsphere.infra.config.props.ConfigurationPropertyKey;
import org.apache.shardingsphere.infra.config.schema.SchemaConfiguration;
import org.apache.shardingsphere.infra.config.schema.impl.DataSourceProvidedSchemaConfiguration;
import org.apache.shardingsphere.infra.datasource.props.DataSourceProperties;
import org.apache.shardingsphere.infra.datasource.props.DataSourcePropertiesCreator;
import org.apache.shardingsphere.infra.yaml.config.swapper.YamlRuleConfigurationSwapperEngine;
import org.apache.shardingsphere.infra.yaml.engine.YamlEngine;
import org.apache.shardingsphere.mode.metadata.persist.service.ComputeNodePersistService;
import org.apache.shardingsphere.mode.metadata.persist.service.impl.DataSourcePersistService;
import org.apache.shardingsphere.mode.metadata.persist.service.impl.GlobalRulePersistService;
import org.apache.shardingsphere.mode.metadata.persist.service.impl.PropertiesPersistService;
import org.apache.shardingsphere.mode.metadata.persist.service.impl.SchemaRulePersistService;
import org.apache.shardingsphere.mode.persist.PersistRepository;
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
import java.util.Map.Entry;
import java.util.Properties;
import java.util.stream.Collectors;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public final class MetaDataPersistServiceTest {
    
    private static final String SCHEMA_RULE_YAML = "yaml/persist/data-schema-rule.yaml";
    
    @Mock
    private DataSourcePersistService dataSourceService;
    
    @Mock
    private SchemaRulePersistService schemaRuleService;
    
    @Mock
    private GlobalRulePersistService globalRuleService;
    
    @Mock
    private PropertiesPersistService propsService;
    
    @Mock
    private ComputeNodePersistService computeNodePersistService;
    
    private MetaDataPersistService metaDataPersistService;
    
    @Before
    public void setUp() throws ReflectiveOperationException {
        metaDataPersistService = new MetaDataPersistService(mock(PersistRepository.class));
        setField("dataSourceService", dataSourceService);
        setField("schemaRuleService", schemaRuleService);
        setField("globalRuleService", globalRuleService);
        setField("propsService", propsService);
        setField("computeNodePersistService", computeNodePersistService);
    }
    
    private void setField(final String name, final Object value) throws ReflectiveOperationException {
        Field field = metaDataPersistService.getClass().getDeclaredField(name);
        field.setAccessible(true);
        field.set(metaDataPersistService, value);
    }
    
    @Test
    public void assertPersistConfigurations() {
        Map<String, DataSource> dataSourceMap = createDataSourceMap();
        Collection<RuleConfiguration> ruleConfigs = createRuleConfigurations();
        Collection<RuleConfiguration> globalRuleConfigs = createGlobalRuleConfigurations();
        Properties props = createProperties();
        metaDataPersistService.persistConfigurations(
                Collections.singletonMap("foo_db", new DataSourceProvidedSchemaConfiguration(dataSourceMap, ruleConfigs)), globalRuleConfigs, props, false);
        verify(dataSourceService).persist("foo_db", createDataSourcePropertiesMap(dataSourceMap), false);
        verify(schemaRuleService).persist("foo_db", ruleConfigs, false);
        verify(globalRuleService).persist(globalRuleConfigs, false);
        verify(propsService).persist(props, false);
    }
    
    private Map<String, DataSourceProperties> createDataSourcePropertiesMap(final Map<String, DataSource> dataSourceMap) {
        return dataSourceMap.entrySet().stream().collect(
            Collectors.toMap(Entry::getKey, entry -> DataSourcePropertiesCreator.create(entry.getValue()), (oldValue, currentValue) -> oldValue, LinkedHashMap::new));
    }
    
    @Test
    public void assertPersistInstanceLabels() {
        metaDataPersistService.persistInstanceLabels("127.0.0.1@3307", Collections.singletonList("foo_label"), false);
        verify(computeNodePersistService).persistInstanceLabels("127.0.0.1@3307", Collections.singletonList("foo_label"), false);
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
    
    @Test
    public void assertGetEffectiveDataSources() {
        Map<String, DataSource> dataSourceMap = createDataSourceMap();
        Collection<RuleConfiguration> ruleConfigs = createRuleConfigurations();
        Map<String, SchemaConfiguration> schemaConfigs = Collections.singletonMap("foo_db", new DataSourceProvidedSchemaConfiguration(dataSourceMap, ruleConfigs));
        Map<String, DataSource> resultEffectiveDataSources = metaDataPersistService.getEffectiveDataSources("foo_db", schemaConfigs);
        assertTrue(resultEffectiveDataSources.isEmpty());
    }
}
