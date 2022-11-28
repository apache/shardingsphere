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

package org.apache.shardingsphere.infra.yaml.data.swapper;

import com.google.common.collect.Lists;
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.infra.metadata.data.ShardingSphereRowData;
import org.apache.shardingsphere.infra.metadata.data.ShardingSphereTableData;
import org.apache.shardingsphere.infra.metadata.database.schema.decorator.model.ShardingSphereColumn;
import org.apache.shardingsphere.infra.util.yaml.swapper.YamlConfigurationSwapper;
import org.apache.shardingsphere.infra.yaml.data.pojo.YamlShardingSpherePartitionRowData;
import org.apache.shardingsphere.infra.yaml.data.pojo.YamlShardingSphereRowData;
import org.apache.shardingsphere.infra.yaml.data.pojo.YamlShardingSphereTableData;
import org.apache.shardingsphere.infra.yaml.schema.pojo.YamlShardingSphereColumn;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * YAML ShardingSphere data swapper.
 */
@RequiredArgsConstructor
public final class YamlShardingSphereTableDataSwapper implements YamlConfigurationSwapper<YamlShardingSphereTableData, ShardingSphereTableData> {
    
    private final int rowsPartitionSize;
    
    public YamlShardingSphereTableDataSwapper() {
        this(100);
    }
    
    @Override
    public YamlShardingSphereTableData swapToYamlConfiguration(final ShardingSphereTableData data) {
        YamlShardingSphereTableData result = new YamlShardingSphereTableData();
        result.setName(data.getName());
        Map<Integer, YamlShardingSpherePartitionRowData> yamlPartitionRows = new LinkedHashMap<>();
        int i = 0;
        for (List<ShardingSphereRowData> each : Lists.partition(data.getRows(), rowsPartitionSize)) {
            Collection<YamlShardingSphereRowData> yamlShardingSphereRowData = new LinkedList<>();
            each.forEach(rowData -> yamlShardingSphereRowData.add(new YamlShardingSphereRowDataSwapper(data.getColumns()).swapToYamlConfiguration(rowData)));
            YamlShardingSpherePartitionRowData partitionRowsData = new YamlShardingSpherePartitionRowData();
            partitionRowsData.setPartitionRows(yamlShardingSphereRowData);
            yamlPartitionRows.put(i++, partitionRowsData);
        }
        result.setPartitionRows(yamlPartitionRows);
        List<YamlShardingSphereColumn> columns = new LinkedList<>();
        data.getColumns().forEach(each -> columns.add(swapYamlColumn(each)));
        result.setColumns(columns);
        return result;
    }
    
    private YamlShardingSphereColumn swapYamlColumn(final ShardingSphereColumn column) {
        YamlShardingSphereColumn result = new YamlShardingSphereColumn();
        result.setName(column.getName());
        result.setCaseSensitive(column.isCaseSensitive());
        result.setGenerated(column.isGenerated());
        result.setPrimaryKey(column.isPrimaryKey());
        result.setDataType(column.getDataType());
        result.setVisible(column.isVisible());
        return result;
    }
    
    @Override
    public ShardingSphereTableData swapToObject(final YamlShardingSphereTableData yamlConfig) {
        List<ShardingSphereColumn> columns = new LinkedList<>();
        if (null != yamlConfig.getColumns()) {
            yamlConfig.getColumns().forEach(each -> columns.add(swapColumn(each)));
        }
        ShardingSphereTableData result = new ShardingSphereTableData(yamlConfig.getName(), columns);
        if (null != yamlConfig.getPartitionRows()) {
            for (YamlShardingSpherePartitionRowData each : yamlConfig.getPartitionRows().values()) {
                each.getPartitionRows().forEach(yamlRowData -> result.getRows().add(new YamlShardingSphereRowDataSwapper(columns).swapToObject(yamlRowData)));
            }
        }
        return result;
    }
    
    private ShardingSphereColumn swapColumn(final YamlShardingSphereColumn column) {
        return new ShardingSphereColumn(column.getName(), column.getDataType(), column.isPrimaryKey(), column.isGenerated(), column.isCaseSensitive(), column.isVisible(), column.isUnsigned());
    }
}
