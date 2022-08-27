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

package org.apache.shardingsphere.data.pipeline.core.metadata.model;

import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.data.pipeline.api.metadata.PipelineColumnMetaData;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Pipeline table meta data.
 */
@Slf4j
@ToString
public final class PipelineTableMetaData {
    
    @NonNull
    private final String name;
    
    private final Map<String, PipelineColumnMetaData> columnMetaDataMap;
    
    @Getter
    private final List<String> columnNames;
    
    @Getter
    private final List<String> primaryKeyColumns;
    
    @Getter
    private final Collection<PipelineIndexMetaData> uniqueIndexes;
    
    public PipelineTableMetaData(final String name, final Map<String, PipelineColumnMetaData> columnMetaDataMap, final Collection<PipelineIndexMetaData> uniqueIndexes) {
        this.name = name;
        this.columnMetaDataMap = columnMetaDataMap;
        List<PipelineColumnMetaData> columnMetaDataList = new ArrayList<>(columnMetaDataMap.values());
        Collections.sort(columnMetaDataList);
        columnNames = Collections.unmodifiableList(columnMetaDataList.stream().map(PipelineColumnMetaData::getName).collect(Collectors.toList()));
        primaryKeyColumns = Collections.unmodifiableList(columnMetaDataList.stream().filter(PipelineColumnMetaData::isPrimaryKey)
                .map(PipelineColumnMetaData::getName).collect(Collectors.toList()));
        this.uniqueIndexes = Collections.unmodifiableCollection(uniqueIndexes);
    }
    
    /**
     * Get column metadata.
     *
     * @param columnIndex column index
     * @return column metadata
     */
    public PipelineColumnMetaData getColumnMetaData(final int columnIndex) {
        return getColumnMetaData(columnNames.get(columnIndex));
    }
    
    /**
     * Get column metadata.
     *
     * @param columnName column name
     * @return column metadata
     */
    public PipelineColumnMetaData getColumnMetaData(final String columnName) {
        PipelineColumnMetaData result = columnMetaDataMap.get(columnName);
        if (null == result) {
            log.warn("getColumnMetaData, can not get column metadata for column name '{}', columnNames={}", columnName, columnNames);
        }
        return result;
    }
    
    /**
     * Judge whether column is unique key or not.
     *
     * @param columnIndex column index
     * @return true if the column is unique key, otherwise false
     */
    public boolean isUniqueKey(final int columnIndex) {
        if (columnIndex >= columnNames.size()) {
            return false;
        }
        String columnName = columnNames.get(columnIndex);
        return columnMetaDataMap.get(columnName).isPrimaryKey() || (columnName.equals(uniqueIndexes.iterator().next().getColumns().get(0).getName()));
    }
    
    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final PipelineTableMetaData that = (PipelineTableMetaData) o;
        return name.equals(that.name);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(name);
    }
}
