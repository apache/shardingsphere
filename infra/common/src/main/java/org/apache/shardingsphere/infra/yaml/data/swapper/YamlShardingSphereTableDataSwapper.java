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

import org.apache.shardingsphere.infra.metadata.data.ShardingSphereRowData;
import org.apache.shardingsphere.infra.metadata.data.ShardingSphereTableData;
import org.apache.shardingsphere.infra.metadata.database.schema.decorator.model.ShardingSphereColumn;
import org.apache.shardingsphere.infra.util.yaml.swapper.YamlConfigurationSwapper;
import org.apache.shardingsphere.infra.yaml.data.pojo.YamlShardingSphereRowData;
import org.apache.shardingsphere.infra.yaml.data.pojo.YamlShardingSphereTableData;
import org.apache.shardingsphere.infra.yaml.schema.pojo.YamlShardingSphereColumn;

import java.math.BigDecimal;
import java.sql.Types;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * YAML ShardingSphere data swapper.
 */
public final class YamlShardingSphereTableDataSwapper implements YamlConfigurationSwapper<YamlShardingSphereTableData, ShardingSphereTableData> {
    
    @Override
    public YamlShardingSphereTableData swapToYamlConfiguration(final ShardingSphereTableData data) {
        YamlShardingSphereTableData result = new YamlShardingSphereTableData();
        result.setName(data.getName());
        List<YamlShardingSphereRowData> rowData = new LinkedList<>();
        data.getRows().forEach(each -> rowData.add(swapYamlRow(each, data.getColumns())));
        result.setRows(rowData);
        List<YamlShardingSphereColumn> columns = new LinkedList<>();
        data.getColumns().forEach(each -> columns.add(swapYamlColumn(each)));
        result.setColumns(columns);
        return result;
    }
    
    private YamlShardingSphereRowData swapYamlRow(final ShardingSphereRowData row, final List<ShardingSphereColumn> columns) {
        YamlShardingSphereRowData result = new YamlShardingSphereRowData();
        List<Object> rowData = null == row.getRows() ? Collections.emptyList() : row.getRows();
        List<Object> yamlRowData = new LinkedList<>();
        int count = 0;
        for (Object each : rowData) {
            yamlRowData.add(convertDataType(each, columns.get(count++).getDataType()));
        }
        result.setRows(yamlRowData);
        return result;
    }
    
    private Object convertDataType(final Object data, final int dataType) {
        if (Types.DECIMAL == dataType) {
            return data.toString();
        }
        // TODO use general type convertor
        return data;
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
        if (null != yamlConfig.getRows()) {
            yamlConfig.getRows().forEach(each -> result.getRows().add(swapRow(each, yamlConfig.getColumns())));
        }
        return result;
    }
    
    private ShardingSphereRowData swapRow(final YamlShardingSphereRowData yamlRowData, final List<YamlShardingSphereColumn> columns) {
        List<Object> yamlRow = null == yamlRowData.getRows() ? Collections.emptyList() : yamlRowData.getRows();
        List<Object> rowData = new LinkedList<>();
        int count = 0;
        for (Object each : yamlRow) {
            YamlShardingSphereColumn yamlColumn = columns.get(count++);
            rowData.add(convertByDataType(each, yamlColumn.getDataType()));
        }
        return new ShardingSphereRowData(rowData);
    }
    
    private Object convertByDataType(final Object data, final int dataType) {
        if (Types.DECIMAL == dataType) {
            return new BigDecimal(data.toString());
        }
        // TODO use general type convertor
        return data;
    }
    
    private ShardingSphereColumn swapColumn(final YamlShardingSphereColumn column) {
        return new ShardingSphereColumn(column.getName(), column.getDataType(), column.isPrimaryKey(), column.isGenerated(), column.isCaseSensitive(), column.isVisible());
    }
}
