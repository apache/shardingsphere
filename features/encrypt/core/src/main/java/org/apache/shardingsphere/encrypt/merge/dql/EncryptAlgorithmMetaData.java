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

package org.apache.shardingsphere.encrypt.merge.dql;

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.encrypt.context.EncryptContextBuilder;
import org.apache.shardingsphere.encrypt.rule.EncryptRule;
import org.apache.shardingsphere.encrypt.spi.EncryptAlgorithm;
import org.apache.shardingsphere.encrypt.spi.context.EncryptContext;
import org.apache.shardingsphere.infra.binder.segment.select.projection.Projection;
import org.apache.shardingsphere.infra.binder.segment.select.projection.impl.ColumnProjection;
import org.apache.shardingsphere.infra.binder.segment.table.TablesContext;
import org.apache.shardingsphere.infra.binder.statement.dml.SelectStatementContext;
import org.apache.shardingsphere.infra.database.type.DatabaseTypeEngine;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Encrypt algorithm meta data.
 */
@RequiredArgsConstructor
public final class EncryptAlgorithmMetaData {
    
    private final ShardingSphereDatabase database;
    
    private final EncryptRule encryptRule;
    
    private final SelectStatementContext selectStatementContext;
    
    /**
     * Find encryptor.
     * 
     * @param tableName table name
     * @param columnName column name
     * @return encryptor
     */
    public Optional<EncryptAlgorithm> findEncryptor(final String tableName, final String columnName) {
        return encryptRule.findEncryptor(tableName, columnName);
    }
    
    /**
     * Judge whether column is support QueryWithCipherColumn or not.
     *
     * @param tableName table name
     * @param columnName column name
     * @return whether column is support QueryWithCipherColumn or not
     */
    public boolean isQueryWithCipherColumn(final String tableName, final String columnName) {
        return encryptRule.isQueryWithCipherColumn(tableName, columnName);
    }
    
    /**
     * Find encrypt context.
     * 
     * @param columnIndex column index
     * @return encrypt context
     */
    public Optional<EncryptContext> findEncryptContext(final int columnIndex) {
        Optional<ColumnProjection> columnProjection = findColumnProjection(columnIndex);
        if (!columnProjection.isPresent()) {
            return Optional.empty();
        }
        TablesContext tablesContext = selectStatementContext.getTablesContext();
        String schemaName = tablesContext.getSchemaName().orElseGet(() -> DatabaseTypeEngine.getDefaultSchemaName(selectStatementContext.getDatabaseType(), database.getName()));
        Map<String, String> expressionTableNames = tablesContext.findTableNamesByColumnProjection(
                Collections.singletonList(columnProjection.get()), database.getSchema(schemaName));
        Optional<String> tableName = findTableName(columnProjection.get(), expressionTableNames);
        return tableName.map(optional -> EncryptContextBuilder.build(database.getName(), schemaName, optional, columnProjection.get().getName()));
    }
    
    private Optional<ColumnProjection> findColumnProjection(final int columnIndex) {
        List<Projection> expandProjections = selectStatementContext.getProjectionsContext().getExpandProjections();
        if (expandProjections.size() < columnIndex) {
            return Optional.empty();
        }
        Projection projection = expandProjections.get(columnIndex - 1);
        return projection instanceof ColumnProjection ? Optional.of((ColumnProjection) projection) : Optional.empty();
    }
    
    private Optional<String> findTableName(final ColumnProjection columnProjection, final Map<String, String> columnTableNames) {
        String tableName = columnTableNames.get(columnProjection.getExpression());
        if (null != tableName) {
            return Optional.of(tableName);
        }
        for (String each : selectStatementContext.getTablesContext().getTableNames()) {
            if (encryptRule.findEncryptor(each, columnProjection.getName()).isPresent()) {
                return Optional.of(each);
            }
        }
        return Optional.empty();
    }
}
