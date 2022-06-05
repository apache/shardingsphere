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

import org.apache.shardingsphere.infra.metadata.database.schema.decorator.model.ShardingSphereColumn;
import org.apache.shardingsphere.infra.metadata.database.schema.decorator.model.ShardingSphereConstraint;
import org.apache.shardingsphere.infra.metadata.database.schema.decorator.model.ShardingSphereIndex;
import org.apache.shardingsphere.infra.metadata.database.schema.decorator.model.ShardingSphereTable;
import org.apache.shardingsphere.infra.yaml.config.swapper.YamlConfigurationSwapper;
import org.apache.shardingsphere.infra.yaml.schema.pojo.YamlColumnMetaData;
import org.apache.shardingsphere.infra.yaml.schema.pojo.YamlConstraintMetaData;
import org.apache.shardingsphere.infra.yaml.schema.pojo.YamlIndexMetaData;
import org.apache.shardingsphere.infra.yaml.schema.pojo.YamlTableMetaData;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Table meta data YAML swapper.
 */
public final class TableMetaDataYamlSwapper implements YamlConfigurationSwapper<YamlTableMetaData, ShardingSphereTable> {
    
    @Override
    public YamlTableMetaData swapToYamlConfiguration(final ShardingSphereTable table) {
        YamlTableMetaData result = new YamlTableMetaData();
        result.setColumns(swapYamlColumns(table.getColumns()));
        result.setIndexes(swapYamlIndexes(table.getIndexes()));
        result.setConstraints(swapYamlConstraints(table.getConstrains()));
        result.setName(table.getName());
        return result;
    }
    
    @Override
    public ShardingSphereTable swapToObject(final YamlTableMetaData yamlConfig) {
        return new ShardingSphereTable(yamlConfig.getName(), swapColumns(yamlConfig.getColumns()), swapIndexes(yamlConfig.getIndexes()), swapConstraints(yamlConfig.getConstraints()));
    }
    
    private Collection<ShardingSphereConstraint> swapConstraints(final Map<String, YamlConstraintMetaData> constraints) {
        return null == constraints ? Collections.emptyList() : constraints.values().stream().map(this::swapConstraint).collect(Collectors.toList());
    }
    
    private ShardingSphereConstraint swapConstraint(final YamlConstraintMetaData constraint) {
        return new ShardingSphereConstraint(constraint.getName(), constraint.getReferencedTableName());
    }
    
    private Collection<ShardingSphereIndex> swapIndexes(final Map<String, YamlIndexMetaData> indexes) {
        return null == indexes ? Collections.emptyList() : indexes.values().stream().map(this::swapIndex).collect(Collectors.toList());
    }
    
    private ShardingSphereIndex swapIndex(final YamlIndexMetaData index) {
        return new ShardingSphereIndex(index.getName());
    }
    
    private Collection<ShardingSphereColumn> swapColumns(final Map<String, YamlColumnMetaData> indexes) {
        return null == indexes ? Collections.emptyList() : indexes.values().stream().map(this::swapColumn).collect(Collectors.toList());
    }
    
    private ShardingSphereColumn swapColumn(final YamlColumnMetaData column) {
        return new ShardingSphereColumn(column.getName(), column.getDataType(), column.isPrimaryKey(), column.isGenerated(), column.isCaseSensitive());
    }
    
    private Map<String, YamlConstraintMetaData> swapYamlConstraints(final Map<String, ShardingSphereConstraint> constrains) {
        return constrains.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, entry -> swapYamlConstraint(entry.getValue()), (oldValue, currentValue) -> oldValue, LinkedHashMap::new));
    }
    
    private YamlConstraintMetaData swapYamlConstraint(final ShardingSphereConstraint constraint) {
        YamlConstraintMetaData result = new YamlConstraintMetaData();
        result.setName(constraint.getName());
        result.setReferencedTableName(constraint.getReferencedTableName());
        return result;
    }
    
    private Map<String, YamlIndexMetaData> swapYamlIndexes(final Map<String, ShardingSphereIndex> indexes) {
        return indexes.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, entry -> swapYamlIndex(entry.getValue()), (oldValue, currentValue) -> oldValue, LinkedHashMap::new));
    }
    
    private YamlIndexMetaData swapYamlIndex(final ShardingSphereIndex index) {
        YamlIndexMetaData result = new YamlIndexMetaData();
        result.setName(index.getName());
        return result;
    }
    
    private Map<String, YamlColumnMetaData> swapYamlColumns(final Map<String, ShardingSphereColumn> columns) {
        return columns.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, entry -> swapYamlColumn(entry.getValue()), (oldValue, currentValue) -> oldValue, LinkedHashMap::new));
    }
    
    private YamlColumnMetaData swapYamlColumn(final ShardingSphereColumn column) {
        YamlColumnMetaData result = new YamlColumnMetaData();
        result.setName(column.getName());
        result.setCaseSensitive(column.isCaseSensitive());
        result.setGenerated(column.isGenerated());
        result.setPrimaryKey(column.isPrimaryKey());
        result.setDataType(column.getDataType());
        return result;
    }
}
