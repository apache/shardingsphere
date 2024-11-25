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

package org.apache.shardingsphere.infra.yaml.schema.swapper;

import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereColumn;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereConstraint;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereIndex;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereTable;
import org.apache.shardingsphere.infra.util.yaml.swapper.YamlConfigurationSwapper;
import org.apache.shardingsphere.infra.yaml.schema.pojo.YamlShardingSphereColumn;
import org.apache.shardingsphere.infra.yaml.schema.pojo.YamlShardingSphereConstraint;
import org.apache.shardingsphere.infra.yaml.schema.pojo.YamlShardingSphereIndex;
import org.apache.shardingsphere.infra.yaml.schema.pojo.YamlShardingSphereTable;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * YAML table swapper.
 */
public final class YamlTableSwapper implements YamlConfigurationSwapper<YamlShardingSphereTable, ShardingSphereTable> {
    
    private final YamlColumnSwapper columnSwapper = new YamlColumnSwapper();
    
    private final YamlIndexSwapper indexSwapper = new YamlIndexSwapper();
    
    private final YamlConstraintSwapper constraintSwapper = new YamlConstraintSwapper();
    
    @Override
    public YamlShardingSphereTable swapToYamlConfiguration(final ShardingSphereTable table) {
        YamlShardingSphereTable result = new YamlShardingSphereTable();
        result.setName(table.getName());
        result.setColumns(swapToYamlColumns(table.getAllColumns()));
        result.setIndexes(swapToYamlIndexes(table.getAllIndexes()));
        result.setConstraints(swapToYamlConstraints(table.getAllConstraints()));
        result.setType(table.getType());
        return result;
    }
    
    private Map<String, YamlShardingSphereColumn> swapToYamlColumns(final Collection<ShardingSphereColumn> columns) {
        return columns.stream().collect(Collectors.toMap(key -> key.getName().toLowerCase(), columnSwapper::swapToYamlConfiguration, (oldValue, currentValue) -> oldValue, LinkedHashMap::new));
    }
    
    private Map<String, YamlShardingSphereIndex> swapToYamlIndexes(final Collection<ShardingSphereIndex> indexes) {
        return indexes.stream().collect(Collectors.toMap(key -> key.getName().toLowerCase(), indexSwapper::swapToYamlConfiguration, (oldValue, currentValue) -> oldValue, LinkedHashMap::new));
    }
    
    private Map<String, YamlShardingSphereConstraint> swapToYamlConstraints(final Collection<ShardingSphereConstraint> constrains) {
        return constrains.stream().collect(Collectors.toMap(key -> key.getName().toLowerCase(), constraintSwapper::swapToYamlConfiguration, (oldValue, currentValue) -> oldValue, LinkedHashMap::new));
    }
    
    @Override
    public ShardingSphereTable swapToObject(final YamlShardingSphereTable yamlConfig) {
        return new ShardingSphereTable(yamlConfig.getName(),
                swapToColumns(yamlConfig.getColumns()), swapToIndexes(yamlConfig.getIndexes()), swapToConstraints(yamlConfig.getConstraints()), yamlConfig.getType());
    }
    
    private Collection<ShardingSphereColumn> swapToColumns(final Map<String, YamlShardingSphereColumn> columns) {
        return null == columns ? Collections.emptyList() : columns.values().stream().map(columnSwapper::swapToObject).collect(Collectors.toList());
    }
    
    private Collection<ShardingSphereIndex> swapToIndexes(final Map<String, YamlShardingSphereIndex> indexes) {
        return null == indexes ? Collections.emptyList() : indexes.values().stream().map(indexSwapper::swapToObject).collect(Collectors.toList());
    }
    
    private Collection<ShardingSphereConstraint> swapToConstraints(final Map<String, YamlShardingSphereConstraint> constraints) {
        return null == constraints ? Collections.emptyList() : constraints.values().stream().map(constraintSwapper::swapToObject).collect(Collectors.toList());
    }
}
