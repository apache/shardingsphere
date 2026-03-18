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

package org.apache.shardingsphere.database.connector.core.metadata.data.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import org.apache.shardingsphere.database.connector.core.metadata.database.enums.TableType;

import java.util.Collection;

/**
 * Table meta data.
 */
@RequiredArgsConstructor
@Getter
@ToString
public final class TableMetaData {
    
    private final String name;
    
    private final Collection<ColumnMetaData> columns;
    
    private final Collection<IndexMetaData> indexes;
    
    private final Collection<ConstraintMetaData> constraints;
    
    private final TableType type;
    
    public TableMetaData(final String name, final Collection<ColumnMetaData> columns, final Collection<IndexMetaData> indexes, final Collection<ConstraintMetaData> constraints) {
        this.name = name;
        this.columns = columns;
        this.indexes = indexes;
        this.constraints = constraints;
        type = TableType.TABLE;
    }
}
