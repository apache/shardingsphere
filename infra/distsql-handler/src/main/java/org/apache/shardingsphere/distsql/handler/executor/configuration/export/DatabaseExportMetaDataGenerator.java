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

package org.apache.shardingsphere.distsql.handler.executor.configuration.export;

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.infra.config.rule.RuleConfiguration;
import org.apache.shardingsphere.infra.config.rule.checker.DatabaseRuleConfigurationEmptyChecker;
import org.apache.shardingsphere.infra.config.rule.scope.DatabaseRuleConfiguration;
import org.apache.shardingsphere.infra.datasource.pool.props.domain.DataSourcePoolProperties;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.resource.unit.StorageUnit;
import org.apache.shardingsphere.infra.spi.type.ordered.OrderedSPILoader;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.infra.util.yaml.YamlEngine;
import org.apache.shardingsphere.infra.util.yaml.representer.ShardingSphereYamlRepresenter;
import org.apache.shardingsphere.infra.yaml.config.swapper.rule.YamlRuleConfigurationSwapper;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.nodes.MappingNode;
import org.yaml.snakeyaml.nodes.Node;
import org.yaml.snakeyaml.nodes.NodeTuple;
import org.yaml.snakeyaml.nodes.ScalarNode;
import org.yaml.snakeyaml.nodes.Tag;

import java.io.StringWriter;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Database export metadata generator.
 */
@RequiredArgsConstructor
public final class DatabaseExportMetaDataGenerator {
    
    private final ShardingSphereDatabase database;
    
    /**
     * Generate YAML format.
     *
     * @return database configuration data of YAML format
     */
    public String generateYAMLFormat() {
        StringBuilder result = new StringBuilder();
        appendDatabaseName(database.getName(), result);
        appendDataSourceConfigurations(database, result);
        appendRuleConfigurations(database.getRuleMetaData().getConfigurations(), result);
        return result.toString();
    }
    
    private void appendDatabaseName(final String databaseName, final StringBuilder stringBuilder) {
        stringBuilder.append(YamlEngine.marshal(Collections.singletonMap("databaseName", databaseName)));
    }
    
    private void appendDataSourceConfigurations(final ShardingSphereDatabase database, final StringBuilder stringBuilder) {
        if (database.getResourceMetaData().getStorageUnits().isEmpty()) {
            return;
        }
        Map<String, Map<String, Object>> dataSources = new LinkedHashMap<>(database.getResourceMetaData().getStorageUnits().size(), 1F);
        for (Entry<String, StorageUnit> entry : database.getResourceMetaData().getStorageUnits().entrySet()) {
            Map<String, Object> props = createDataSourceConfiguration(entry.getValue().getDataSourcePoolProperties());
            dataSources.put("null".equals(entry.getKey()) ? new String("null") : entry.getKey(), props.isEmpty() ? null : props);
        }
        stringBuilder.append(marshalDataSources(dataSources));
    }
    
    private Map<String, Object> createDataSourceConfiguration(final DataSourcePoolProperties props) {
        Map<String, Object> result = new LinkedHashMap<>(props.getConnectionPropertySynonyms().getStandardProperties().size() + props.getPoolPropertySynonyms().getStandardProperties().size(), 1F);
        for (Entry<String, Object> entry : props.getConnectionPropertySynonyms().getStandardProperties().entrySet()) {
            if (null != entry.getValue()) {
                result.put(entry.getKey(), "null".equals(entry.getValue()) ? new String("null") : entry.getValue());
            }
        }
        for (Entry<String, Object> entry : props.getPoolPropertySynonyms().getStandardProperties().entrySet()) {
            if (null != entry.getValue()) {
                result.put(entry.getKey(), "null".equals(entry.getValue()) ? new String("null") : entry.getValue());
            }
        }
        return result;
    }
    
    private String marshalDataSources(final Map<String, Map<String, Object>> dataSources) {
        DumperOptions dumperOptions = new DumperOptions();
        dumperOptions.setLineBreak(DumperOptions.LineBreak.getPlatformLineBreak());
        dumperOptions.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        ShardingSphereYamlRepresenter representer = new ShardingSphereYamlRepresenter(dumperOptions);
        MappingNode root = (MappingNode) representer.represent(Collections.singletonMap("dataSources", dataSources));
        String emptyScalarToken = "null";
        replaceEmptyValues(root, emptyScalarToken);
        StringWriter result = new StringWriter() {
            
            @Override
            public void write(final String value, final int offset, final int length) {
                if (emptyScalarToken != value) {
                    super.write(value, offset, length);
                }
            }
        };
        new Yaml(representer, dumperOptions).serialize(root, result);
        return result.toString();
    }
    
    private void replaceEmptyValues(final MappingNode node, final String emptyScalarToken) {
        List<NodeTuple> values = node.getValue();
        for (int i = 0; i < values.size(); i++) {
            NodeTuple tuple = values.get(i);
            Node value = tuple.getValueNode();
            if (value instanceof MappingNode) {
                replaceEmptyValues((MappingNode) value, emptyScalarToken);
            } else if (value instanceof ScalarNode && Tag.STR.equals(value.getTag()) && ((ScalarNode) value).getValue().isEmpty()) {
                values.set(i, new NodeTuple(tuple.getKeyNode(), new ScalarNode(Tag.NULL, emptyScalarToken, null, null, DumperOptions.ScalarStyle.PLAIN)));
            }
        }
    }
    
    @SuppressWarnings({"rawtypes", "unchecked"})
    private void appendRuleConfigurations(final Collection<RuleConfiguration> ruleConfigs, final StringBuilder stringBuilder) {
        if (ruleConfigs.isEmpty()) {
            return;
        }
        boolean hasAppendedRule = false;
        for (Entry<RuleConfiguration, YamlRuleConfigurationSwapper> entry : OrderedSPILoader.getServices(YamlRuleConfigurationSwapper.class, ruleConfigs).entrySet()) {
            if (TypedSPILoader.getService(DatabaseRuleConfigurationEmptyChecker.class, entry.getKey().getClass()).isEmpty((DatabaseRuleConfiguration) entry.getKey())) {
                continue;
            }
            if (!hasAppendedRule) {
                stringBuilder.append("rules:").append(System.lineSeparator());
                hasAppendedRule = true;
            }
            stringBuilder.append(YamlEngine.marshal(Collections.singletonList(entry.getValue().swapToYamlConfiguration(entry.getKey()))));
        }
    }
}
