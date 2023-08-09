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
    
    @Override
    public YamlShardingSphereTable swapToYamlConfiguration(final ShardingSphereTable table) {
        YamlShardingSphereTable result = new YamlShardingSphereTable();
        result.setColumns(swapYamlColumns(table.getColumnValues()));
        result.setIndexes(swapYamlIndexes(table.getIndexValues()));
        result.setConstraints(swapYamlConstraints(table.getConstraintValues()));
        result.setName(table.getName());
        return result;
    }
    
    @Override
    public ShardingSphereTable swapToObject(final YamlShardingSphereTable yamlConfig) {
        return new ShardingSphereTable(yamlConfig.getName(), swapColumns(yamlConfig.getColumns()), swapIndexes(yamlConfig.getIndexes()), swapConstraints(yamlConfig.getConstraints()));
    }
    
    private Collection<ShardingSphereConstraint> swapConstraints(final Map<String, YamlShardingSphereConstraint> constraints) {
        return null == constraints ? Collections.emptyList() : constraints.values().stream().map(this::swapConstraint).collect(Collectors.toList());
    }
    
    private ShardingSphereConstraint swapConstraint(final YamlShardingSphereConstraint constraint) {
        return new ShardingSphereConstraint(constraint.getName(), constraint.getReferencedTableName());
    }
    
    private Collection<ShardingSphereIndex> swapIndexes(final Map<String, YamlShardingSphereIndex> indexes) {
        return null == indexes ? Collections.emptyList() : indexes.values().stream().map(this::swapIndex).collect(Collectors.toList());
    }
    
    private ShardingSphereIndex swapIndex(final YamlShardingSphereIndex index) {
        ShardingSphereIndex result = new ShardingSphereIndex(index.getName());
        result.getColumns().addAll(index.getColumns());
        result.setUnique(index.isUnique());
        return result;
    }
    
    private Collection<ShardingSphereColumn> swapColumns(final Map<String, YamlShardingSphereColumn> indexes) {
        return null == indexes ? Collections.emptyList() : indexes.values().stream().map(this::swapColumn).collect(Collectors.toList());
    }
    
    private ShardingSphereColumn swapColumn(final YamlShardingSphereColumn column) {
        return new ShardingSphereColumn(column.getName(), column.getDataType(), column.isPrimaryKey(), column.isGenerated(), column.isCaseSensitive(), column.isVisible(), column.isUnsigned(),
                column.isNullable());
    }
    
    private Map<String, YamlShardingSphereConstraint> swapYamlConstraints(final Collection<ShardingSphereConstraint> constrains) {
        return constrains.stream().collect(Collectors.toMap(key -> key.getName().toLowerCase(), this::swapYamlConstraint, (oldValue, currentValue) -> oldValue, LinkedHashMap::new));
    }
    
    private YamlShardingSphereConstraint swapYamlConstraint(final ShardingSphereConstraint constraint) {
        YamlShardingSphereConstraint result = new YamlShardingSphereConstraint();
        result.setName(constraint.getName());
        result.setReferencedTableName(constraint.getReferencedTableName());
        return result;
    }
    
    private Map<String, YamlShardingSphereIndex> swapYamlIndexes(final Collection<ShardingSphereIndex> indexes) {
        return indexes.stream().collect(Collectors.toMap(key -> key.getName().toLowerCase(), this::swapYamlIndex, (oldValue, currentValue) -> oldValue, LinkedHashMap::new));
    }
    
    private YamlShardingSphereIndex swapYamlIndex(final ShardingSphereIndex index) {
        YamlShardingSphereIndex result = new YamlShardingSphereIndex();
        result.setName(index.getName());
        result.getColumns().addAll(index.getColumns());
        result.setUnique(index.isUnique());
        return result;
    }
    
    private Map<String, YamlShardingSphereColumn> swapYamlColumns(final Collection<ShardingSphereColumn> columns) {
        return columns.stream().collect(Collectors.toMap(key -> key.getName().toLowerCase(), this::swapYamlColumn, (oldValue, currentValue) -> oldValue, LinkedHashMap::new));
    }
    
    private YamlShardingSphereColumn swapYamlColumn(final ShardingSphereColumn column) {
        YamlShardingSphereColumn result = new YamlShardingSphereColumn();
        result.setName(column.getName());
        result.setCaseSensitive(column.isCaseSensitive());
        result.setGenerated(column.isGenerated());
        result.setPrimaryKey(column.isPrimaryKey());
        result.setDataType(column.getDataType());
        result.setVisible(column.isVisible());
        result.setUnsigned(column.isUnsigned());
        result.setNullable(column.isNullable());
        return result;
    }
}
