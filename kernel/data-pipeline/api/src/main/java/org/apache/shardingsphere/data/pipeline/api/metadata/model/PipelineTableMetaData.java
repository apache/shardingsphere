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

package org.apache.shardingsphere.data.pipeline.api.metadata.model;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Pipeline table meta data.
 */
@Slf4j
@EqualsAndHashCode(of = "name")
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
     * Get column meta data.
     *
     * @param columnIndex the first column is 1, the second is 2, ...
     * @return column meta data
     */
    // TODO Remove it. Get column meta data by column name for incremental dumper, since columns ordering might be changed.
    public PipelineColumnMetaData getColumnMetaData(final int columnIndex) {
        return getColumnMetaData(columnNames.get(columnIndex - 1));
    }
    
    /**
     * Get column meta data.
     *
     * @param columnName column name
     * @return column meta data
     */
    public PipelineColumnMetaData getColumnMetaData(final String columnName) {
        PipelineColumnMetaData result = columnMetaDataMap.get(columnName);
        if (null == result) {
            log.warn("Can not get column meta data for column name '{}', columnNames={}", columnName, columnNames);
        }
        return result;
    }
}
