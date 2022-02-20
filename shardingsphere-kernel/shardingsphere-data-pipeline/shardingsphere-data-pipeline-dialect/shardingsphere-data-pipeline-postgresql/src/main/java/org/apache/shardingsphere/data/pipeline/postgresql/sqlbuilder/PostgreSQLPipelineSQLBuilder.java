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

package org.apache.shardingsphere.data.pipeline.postgresql.sqlbuilder;

import org.apache.shardingsphere.data.pipeline.api.ingest.record.Column;
import org.apache.shardingsphere.data.pipeline.api.ingest.record.DataRecord;
import org.apache.shardingsphere.data.pipeline.core.record.RecordUtil;
import org.apache.shardingsphere.data.pipeline.core.sqlbuilder.AbstractPipelineSQLBuilder;

import java.util.Map;
import java.util.Set;

/**
 * PostgreSQL pipeline SQL builder.
 */
public final class PostgreSQLPipelineSQLBuilder extends AbstractPipelineSQLBuilder {
    
    public PostgreSQLPipelineSQLBuilder() {
    }
    
    public PostgreSQLPipelineSQLBuilder(final Map<String, Set<String>> shardingColumnsMap) {
        super(shardingColumnsMap);
    }
    
    @Override
    public String getLeftIdentifierQuoteString() {
        return "\"";
    }
    
    @Override
    public String getRightIdentifierQuoteString() {
        return "\"";
    }
    
    @Override
    public String buildInsertSQL(final DataRecord dataRecord) {
        return super.buildInsertSQL(dataRecord) + buildConflictSQL(dataRecord);
    }
    
    private String buildConflictSQL(final DataRecord dataRecord) {
        StringBuilder result = new StringBuilder(" ON CONFLICT (");
        for (Column each : RecordUtil.extractPrimaryColumns(dataRecord)) {
            result.append(each.getName()).append(",");
        }
        result.setLength(result.length() - 1);
        result.append(") DO NOTHING");
        return result.toString();
    }
    
    @Override
    public String getType() {
        return "PostgreSQL";
    }
}
