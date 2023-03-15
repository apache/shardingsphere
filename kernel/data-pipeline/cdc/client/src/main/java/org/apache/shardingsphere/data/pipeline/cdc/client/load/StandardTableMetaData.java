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

package org.apache.shardingsphere.data.pipeline.cdc.client.load;

import lombok.Getter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Table meta data.
 */
@Slf4j
@ToString
public final class StandardTableMetaData {
    
    private final String name;
    
    private final Map<String, StandardColumnMetaData> columnMetaDataMap;
    
    @Getter
    private final List<String> columnNames;
    
    @Getter
    private final List<String> primaryKeyColumns;
    
    @Getter
    private final List<String> uniqueKeyColumns;
    
    public StandardTableMetaData(final String name, final Map<String, StandardColumnMetaData> columnMetaDataMap) {
        this.name = name;
        this.columnMetaDataMap = columnMetaDataMap;
        List<StandardColumnMetaData> columnMetaDataList = new ArrayList<>(columnMetaDataMap.values());
        Collections.sort(columnMetaDataList);
        columnNames = Collections.unmodifiableList(columnMetaDataList.stream().map(StandardColumnMetaData::getName).collect(Collectors.toList()));
        primaryKeyColumns = Collections.unmodifiableList(columnMetaDataList.stream().filter(StandardColumnMetaData::isPrimaryKey).map(StandardColumnMetaData::getName).collect(Collectors.toList()));
        uniqueKeyColumns = Collections.unmodifiableList(columnMetaDataList.stream().filter(StandardColumnMetaData::isUniqueKey).map(StandardColumnMetaData::getName).collect(Collectors.toList()));
    }
    
    /**
     * Get column meta data.
     *
     * @param columnName column name
     * @return column meta data
     */
    public StandardColumnMetaData getColumnMetaData(final String columnName) {
        StandardColumnMetaData result = columnMetaDataMap.get(columnName);
        if (null == result) {
            log.warn("Can not get column meta data for column name '{}', columnNames={}", columnName, columnNames);
        }
        return result;
    }
    
    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (null == o || getClass() != o.getClass()) {
            return false;
        }
        return name.equals(((StandardTableMetaData) o).name);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(name);
    }
}
