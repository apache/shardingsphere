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

package org.apache.shardingsphere.data.pipeline.opengauss.sqlbuilder;

import com.google.common.collect.Collections2;
import org.apache.shardingsphere.data.pipeline.api.ingest.record.Column;
import org.apache.shardingsphere.data.pipeline.api.ingest.record.DataRecord;
import org.apache.shardingsphere.data.pipeline.core.sqlbuilder.AbstractPipelineSQLBuilder;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * OpenGauss pipeline SQL builder.
 */
public final class OpenGaussPipelineSQLBuilder extends AbstractPipelineSQLBuilder {

    public OpenGaussPipelineSQLBuilder(final Map<String, Set<String>> shardingColumnsMap) {
        super(shardingColumnsMap);
    }
    
    @Override
    public String getLeftIdentifierQuoteString() {
        return "";
    }
    
    @Override
    public String getRightIdentifierQuoteString() {
        return "";
    }
    
    @Override
    public String buildInsertSQL(final DataRecord dataRecord) {
        return super.buildInsertSQL(dataRecord) + buildConflictSQL();
    }
    
    @Override
    public List<Column> extractUpdatedColumns(final Collection<Column> columns, final DataRecord record) {
        return new ArrayList(Collections2.filter(columns, column -> !(column.isPrimaryKey()
                || isShardingColumn(getShardingColumnsMap(), record.getTableName(), column.getName()))));
    }
    
    private boolean isShardingColumn(final Map<String, Set<String>> shardingColumnsMap,
                                     final String tableName, final String columnName) {
        return shardingColumnsMap.containsKey(tableName)
                && shardingColumnsMap.get(tableName).contains(columnName);
    }
    
    private String buildConflictSQL() {
        // there need return ON DUPLICATE KEY UPDATE NOTHING after support this syntax.
        return "";
    }
}
