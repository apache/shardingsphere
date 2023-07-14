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

package org.apache.shardingsphere.data.pipeline.common.sqlbuilder.fixture;

import org.apache.shardingsphere.data.pipeline.api.ingest.record.Column;
import org.apache.shardingsphere.data.pipeline.api.ingest.record.DataRecord;
import org.apache.shardingsphere.data.pipeline.common.ingest.record.RecordUtils;
import org.apache.shardingsphere.data.pipeline.common.sqlbuilder.PipelineSQLSegmentBuilder;
import org.apache.shardingsphere.data.pipeline.spi.sqlbuilder.DialectPipelineSQLBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public final class H2PipelineSQLBuilder implements DialectPipelineSQLBuilder {
    
    @Override
    public List<Column> extractUpdatedColumns(final DataRecord dataRecord) {
        return new ArrayList<>(RecordUtils.extractUpdatedColumns(dataRecord));
    }
    
    @Override
    public String buildCheckEmptySQL(final String schemaName, final String tableName) {
        return String.format("SELECT * FROM %s LIMIT 1", new PipelineSQLSegmentBuilder(getType()).getQualifiedTableName(schemaName, tableName));
    }
    
    @Override
    public Optional<String> buildEstimatedCountSQL(final String schemaName, final String tableName) {
        return Optional.empty();
    }
    
    @Override
    public String getDatabaseType() {
        return "H2";
    }
}
