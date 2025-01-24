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
import org.apache.shardingsphere.infra.metadata.statistics.RowStatistics;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereColumn;
import org.apache.shardingsphere.infra.util.yaml.swapper.YamlConfigurationSwapper;
import org.apache.shardingsphere.infra.yaml.data.pojo.YamlRowStatistics;

import java.math.BigDecimal;
import java.sql.Types;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * YAML row statistics swapper.
 */
@RequiredArgsConstructor
public final class YamlRowStatisticsSwapper implements YamlConfigurationSwapper<YamlRowStatistics, RowStatistics> {
    
    private final List<ShardingSphereColumn> columns;
    
    @Override
    public YamlRowStatistics swapToYamlConfiguration(final RowStatistics data) {
        YamlRowStatistics result = new YamlRowStatistics();
        Collection<Object> rowData = null == data.getRows() ? Collections.emptyList() : data.getRows();
        List<Object> yamlRowData = new LinkedList<>();
        int count = 0;
        for (Object each : rowData) {
            yamlRowData.add(convertDataType(each, columns.get(count++).getDataType()));
        }
        result.setRows(yamlRowData);
        result.setUniqueKey(data.getUniqueKey());
        return result;
    }
    
    private Object convertDataType(final Object data, final int dataType) {
        if (Types.DECIMAL == dataType || Types.BIGINT == dataType) {
            return null == data ? null : data.toString();
        }
        // TODO use general type convertor
        return data;
    }
    
    @Override
    public RowStatistics swapToObject(final YamlRowStatistics yamlConfig) {
        Collection<Object> yamlRow = null == yamlConfig.getRows() ? Collections.emptyList() : yamlConfig.getRows();
        List<Object> rowData = new LinkedList<>();
        int count = 0;
        for (Object each : yamlRow) {
            ShardingSphereColumn column = columns.get(count++);
            rowData.add(convertByDataType(each, column.getDataType()));
        }
        return new RowStatistics(yamlConfig.getUniqueKey(), rowData);
    }
    
    private Object convertByDataType(final Object data, final int dataType) {
        if (null == data) {
            return null;
        }
        if (Types.DECIMAL == dataType) {
            return new BigDecimal(data.toString());
        }
        if (Types.BIGINT == dataType) {
            return Long.valueOf(data.toString());
        }
        if (Types.REAL == dataType || Types.FLOAT == dataType) {
            return Float.parseFloat(data.toString());
        }
        // TODO use general type convertor
        return data;
    }
}
