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

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.infra.metadata.statistics.TableStatistics;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereColumn;
import org.apache.shardingsphere.infra.util.yaml.swapper.YamlConfigurationSwapper;
import org.apache.shardingsphere.infra.yaml.data.pojo.YamlShardingSphereRowData;
import org.apache.shardingsphere.infra.yaml.data.pojo.YamlShardingSphereTableData;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

/**
 * YAML ShardingSphere statistics swapper.
 */
@RequiredArgsConstructor
public final class YamlShardingSphereTableStatisticsSwapper implements YamlConfigurationSwapper<YamlShardingSphereTableData, TableStatistics> {
    
    private final List<ShardingSphereColumn> columns;
    
    @Override
    public YamlShardingSphereTableData swapToYamlConfiguration(final TableStatistics data) {
        YamlShardingSphereTableData result = new YamlShardingSphereTableData();
        result.setName(data.getName());
        Collection<YamlShardingSphereRowData> yamlShardingSphereRowData = new LinkedList<>();
        data.getRows().forEach(rowData -> yamlShardingSphereRowData.add(new YamlShardingSphereRowStatisticsSwapper(columns).swapToYamlConfiguration(rowData)));
        result.setRowData(yamlShardingSphereRowData);
        return result;
    }
    
    @Override
    public TableStatistics swapToObject(final YamlShardingSphereTableData yamlConfig) {
        TableStatistics result = new TableStatistics(yamlConfig.getName());
        if (null != yamlConfig.getRowData()) {
            yamlConfig.getRowData().forEach(yamlRowData -> result.getRows().add(new YamlShardingSphereRowStatisticsSwapper(columns).swapToObject(yamlRowData)));
        }
        return result;
    }
}
