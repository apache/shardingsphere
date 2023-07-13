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

import org.apache.shardingsphere.data.pipeline.api.ingest.record.Column;
import org.apache.shardingsphere.data.pipeline.api.ingest.record.DataRecord;
import org.apache.shardingsphere.data.pipeline.common.sqlbuilder.PipelineSQLBuilderEngine;
import org.apache.shardingsphere.data.pipeline.spi.sqlbuilder.DialectPipelineSQLBuilder;
import org.apache.shardingsphere.infra.database.type.DatabaseTypeEngine;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Pipeline SQL builder of openGauss.
 */
public final class OpenGaussPipelineSQLBuilder implements DialectPipelineSQLBuilder {
    
    @Override
    public Optional<String> buildCreateSchemaSQL(final String schemaName) {
        return Optional.of(String.format("CREATE SCHEMA %s", DatabaseTypeEngine.escapeIdentifierIfNecessary(getType(), schemaName)));
    }
    
    @Override
    public Optional<String> buildInsertSQLOnDuplicateClause(final String schemaName, final DataRecord dataRecord) {
        StringBuilder result = new StringBuilder(" ON DUPLICATE KEY UPDATE ");
        for (int i = 0; i < dataRecord.getColumnCount(); i++) {
            Column column = dataRecord.getColumn(i);
            if (column.isUniqueKey()) {
                continue;
            }
            result.append(DatabaseTypeEngine.escapeIdentifierIfNecessary(getType(), column.getName()))
                    .append("=EXCLUDED.").append(DatabaseTypeEngine.escapeIdentifierIfNecessary(getType(), column.getName())).append(',');
        }
        result.setLength(result.length() - 1);
        return Optional.of(result.toString());
    }
    
    @Override
    public List<Column> extractUpdatedColumns(final DataRecord dataRecord) {
        return dataRecord.getColumns().stream().filter(each -> !(each.isUniqueKey())).collect(Collectors.toList());
    }
    
    @Override
    public String buildCheckEmptySQL(final String schemaName, final String tableName) {
        return String.format("SELECT * FROM %s LIMIT 1", new PipelineSQLBuilderEngine(getType()).getQualifiedTableName(schemaName, tableName));
    }
    
    @Override
    public Optional<String> buildEstimatedCountSQL(final String schemaName, final String tableName) {
        return Optional.of(String.format("SELECT reltuples::integer FROM pg_class WHERE oid='%s'::regclass::oid;",
                new PipelineSQLBuilderEngine(getType()).getQualifiedTableName(schemaName, tableName)));
    }
    
    @Override
    public String getDatabaseType() {
        return "openGauss";
    }
}
