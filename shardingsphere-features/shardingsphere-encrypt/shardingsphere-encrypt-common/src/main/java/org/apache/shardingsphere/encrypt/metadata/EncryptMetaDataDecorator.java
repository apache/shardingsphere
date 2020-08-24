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

package org.apache.shardingsphere.encrypt.metadata;

import org.apache.shardingsphere.encrypt.constant.EncryptOrder;
import org.apache.shardingsphere.encrypt.rule.EncryptRule;
import org.apache.shardingsphere.sql.parser.binder.metadata.column.ColumnMetaData;
import org.apache.shardingsphere.sql.parser.binder.metadata.table.TableMetaData;
import org.apache.shardingsphere.infra.metadata.schema.spi.RuleMetaDataDecorator;

import java.util.Collection;
import java.util.LinkedList;

/**
 * Table meta data decorator for encrypt.
 */
public final class EncryptMetaDataDecorator implements RuleMetaDataDecorator<EncryptRule> {
    
    @Override
    public TableMetaData decorate(final String tableName, final TableMetaData tableMetaData, final EncryptRule encryptRule) {
        return new TableMetaData(getEncryptColumnMetaDataList(tableName, tableMetaData.getColumns().values(), encryptRule), tableMetaData.getIndexes().values());
    }
    
    private Collection<ColumnMetaData> getEncryptColumnMetaDataList(final String tableName, final Collection<ColumnMetaData> originalColumnMetaDataList, final EncryptRule encryptRule) {
        Collection<ColumnMetaData> result = new LinkedList<>();
        Collection<String> derivedColumns = encryptRule.getAssistedQueryAndPlainColumns(tableName);
        for (ColumnMetaData each : originalColumnMetaDataList) {
            if (!derivedColumns.contains(each.getName())) {
                result.add(getEncryptColumnMetaData(tableName, each, encryptRule));
            }
        }
        return result;
    }
    
    private ColumnMetaData getEncryptColumnMetaData(final String tableName, final ColumnMetaData originalColumnMetaData, final EncryptRule encryptRule) {
        if (!encryptRule.isCipherColumn(tableName, originalColumnMetaData.getName())) {
            return originalColumnMetaData;
        }
        String logicColumnName = encryptRule.getLogicColumnOfCipher(tableName, originalColumnMetaData.getName());
        String plainColumnName = encryptRule.findPlainColumn(tableName, logicColumnName).orElse(null);
        String assistedQueryColumnName = encryptRule.findAssistedQueryColumn(tableName, logicColumnName).orElse(null);
        return new EncryptColumnMetaData(
                logicColumnName, originalColumnMetaData.getDataType(), originalColumnMetaData.getDataTypeName(), originalColumnMetaData.isPrimaryKey(), originalColumnMetaData.getName(),
                plainColumnName, assistedQueryColumnName);
    }
    
    @Override
    public int getOrder() {
        return EncryptOrder.ORDER;
    }
    
    @Override
    public Class<EncryptRule> getTypeClass() {
        return EncryptRule.class;
    }
}
