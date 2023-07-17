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
import org.apache.shardingsphere.data.pipeline.common.sqlbuilder.PipelineSQLSegmentBuilder;
import org.apache.shardingsphere.data.pipeline.spi.sqlbuilder.DialectPipelineSQLBuilder;

import java.util.Optional;
import java.util.stream.Collectors;

/**
 * PostgreSQL pipeline SQL builder.
 */
public final class PostgreSQLPipelineSQLBuilder implements DialectPipelineSQLBuilder {
    
    @Override
    public Optional<String> buildCreateSchemaSQL(final String schemaName) {
        return Optional.of(String.format("CREATE SCHEMA IF NOT EXISTS %s", schemaName));
    }
    
    @Override
    public Optional<String> buildInsertOnDuplicateClause(final DataRecord dataRecord) {
        // TODO without unique key, job has been interrupted, which may lead to data duplication
        if (dataRecord.getUniqueKeyValue().isEmpty()) {
            return Optional.empty();
        }
        StringBuilder result = new StringBuilder("ON CONFLICT (");
        PipelineSQLSegmentBuilder sqlSegmentBuilder = new PipelineSQLSegmentBuilder(getType());
        result.append(dataRecord.getColumns().stream().filter(Column::isUniqueKey).map(each -> sqlSegmentBuilder.getEscapedIdentifier(each.getName())).collect(Collectors.joining(",")));
        result.append(") DO UPDATE SET ");
        result.append(dataRecord.getColumns().stream()
                .filter(each -> !each.isUniqueKey()).map(each -> sqlSegmentBuilder.getEscapedIdentifier(each.getName()) + "=EXCLUDED." + sqlSegmentBuilder.getEscapedIdentifier(each.getName()))
                .collect(Collectors.joining(",")));
        return Optional.of(result.toString());
    }
    
    @Override
    public String buildCheckEmptySQL(final String qualifiedTableName) {
        return String.format("SELECT * FROM %s LIMIT 1", qualifiedTableName);
    }
    
    @Override
    public Optional<String> buildEstimatedCountSQL(final String qualifiedTableName) {
        return Optional.of(String.format("SELECT reltuples::integer FROM pg_class WHERE oid='%s'::regclass::oid;", qualifiedTableName));
    }
    
    @Override
    public String getDatabaseType() {
        return "PostgreSQL";
    }
}
