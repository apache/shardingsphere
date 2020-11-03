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
import org.apache.shardingsphere.encrypt.rule.EncryptRule;
import org.apache.shardingsphere.encrypt.spi.EncryptAlgorithm;
import org.apache.shardingsphere.infra.metadata.model.schema.physical.model.schema.PhysicalSchemaMetaData;
import org.apache.shardingsphere.infra.binder.segment.select.projection.Projection;
import org.apache.shardingsphere.infra.binder.segment.select.projection.impl.ColumnProjection;
import org.apache.shardingsphere.infra.binder.statement.dml.SelectStatementContext;

import java.util.List;
import java.util.Optional;

/**
 * Encrypt algorithm meta data.
 */
@RequiredArgsConstructor
public final class EncryptAlgorithmMetaData {
    
    private final PhysicalSchemaMetaData schemaMetaData;
    
    private final EncryptRule encryptRule;
    
    private final SelectStatementContext selectStatementContext;
    
    /**
     * Find encryptor.
     *
     * @param columnIndex column index
     * @return encryptor
     */
    public Optional<EncryptAlgorithm> findEncryptor(final int columnIndex) {
        List<Projection> expandProjections = selectStatementContext.getProjectionsContext().getExpandProjections();
        if (expandProjections.isEmpty()) {
            return Optional.empty();
        }
        return findEncryptor(columnIndex, expandProjections);
    }
    
    private Optional<EncryptAlgorithm> findEncryptor(final int columnIndex, final List<Projection> expandProjections) {
        Projection projection = expandProjections.get(columnIndex - 1);
        if (projection instanceof ColumnProjection) {
            String columnName = ((ColumnProjection) projection).getName();
            Optional<String> tableName = selectStatementContext.getTablesContext().findTableName((ColumnProjection) projection, schemaMetaData);
            return tableName.isPresent() ? findEncryptor(tableName.get(), columnName) : findEncryptor(columnName);
        }
        return Optional.empty();
    }
    
    private Optional<EncryptAlgorithm> findEncryptor(final String tableName, final String columnName) {
        return encryptRule.findEncryptor(tableName, columnName);
    }
    
    private Optional<EncryptAlgorithm> findEncryptor(final String columnName) {
        for (String each : selectStatementContext.getTablesContext().getTableNames()) {
            Optional<EncryptAlgorithm> result = encryptRule.findEncryptor(each, columnName);
            if (result.isPresent()) {
                return result;
            }
        }
        return Optional.empty();
    }
}
