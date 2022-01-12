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

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Pipelien table meta data.
 */
@ToString
public final class PipelineTableMetaData {
    
    @NonNull
    private final String name;
    
    private final Map<String, PipelineColumnMetaData> columnMetaDataMap;
    
    @Getter
    private final Collection<String> columnNames;
    
    @Getter
    private final List<String> primaryKeys;
    
    public PipelineTableMetaData(final String name, final Map<String, PipelineColumnMetaData> columnMetaDataMap) {
        this.name = name;
        this.columnMetaDataMap = columnMetaDataMap;
        columnNames = Collections.unmodifiableCollection(columnMetaDataMap.keySet());
        primaryKeys = Collections.unmodifiableList(columnMetaDataMap.values().stream().filter(PipelineColumnMetaData::isPrimaryKey).map(PipelineColumnMetaData::getName).collect(Collectors.toList()));
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
