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

package org.apache.shardingsphere.infra.metadata.database.schema.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import org.apache.shardingsphere.database.connector.core.metadata.data.model.ColumnMetaData;

/**
 * ShardingSphere column.
 */
@RequiredArgsConstructor
@Getter
@ToString
public final class ShardingSphereColumn {
    
    private final String name;
    
    private final int dataType;
    
    private final boolean primaryKey;
    
    private final boolean generated;

    private final String typeName; // 新增字段，用于 PG UDT、JSONB 等
    
    private final boolean caseSensitive;
    
    private final boolean visible;
    
    private final boolean unsigned;
    
    private final boolean nullable;
    
    public ShardingSphereColumn(final ColumnMetaData columnMetaData) {
        this(columnMetaData.getName(), columnMetaData.getDataType(), columnMetaData.isPrimaryKey(), columnMetaData.isGenerated(),columnMetaData.getTypeName(),
                columnMetaData.isCaseSensitive(), columnMetaData.isVisible(), columnMetaData.isUnsigned(), columnMetaData.isNullable());
    }
}
