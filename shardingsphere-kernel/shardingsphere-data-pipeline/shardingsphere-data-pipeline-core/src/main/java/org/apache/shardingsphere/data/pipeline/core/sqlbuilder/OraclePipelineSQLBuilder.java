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

package org.apache.shardingsphere.data.pipeline.core.sqlbuilder;

import org.apache.shardingsphere.data.pipeline.api.ingest.record.DataRecord;
import org.apache.shardingsphere.data.pipeline.api.metadata.LogicTableName;

import java.util.Map;
import java.util.Set;

/**
 * Oracle pipeline SQL builder.
 */
public final class OraclePipelineSQLBuilder extends AbstractPipelineSQLBuilder {
    
    @Override
    public String buildCreateSchemaSQL(final String schemaName) {
        throw new UnsupportedOperationException("Not supported for now");
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
    public String buildInsertSQL(final String schemaName, final DataRecord dataRecord, final Map<LogicTableName, Set<String>> shardingColumnsMap) {
        return super.buildInsertSQL(schemaName, dataRecord, shardingColumnsMap);
        // TODO buildInsertSQL and buildConflictSQL, need 2 round parameters set
        // TODO refactor PipelineSQLBuilder to combine SQL building and parameters set
    }
    
    @Override
    public String getType() {
        return "Oracle";
    }
}
