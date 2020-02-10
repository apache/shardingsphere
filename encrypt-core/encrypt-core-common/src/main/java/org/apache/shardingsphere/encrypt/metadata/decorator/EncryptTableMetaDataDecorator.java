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

package org.apache.shardingsphere.encrypt.metadata.decorator;

import org.apache.shardingsphere.encrypt.metadata.EncryptColumnMetaData;
import org.apache.shardingsphere.encrypt.rule.EncryptRule;
import org.apache.shardingsphere.underlying.common.metadata.column.ColumnMetaData;
import org.apache.shardingsphere.underlying.common.metadata.table.TableMetaData;
import org.apache.shardingsphere.underlying.common.metadata.table.TableMetas;
import org.apache.shardingsphere.underlying.common.metadata.table.init.decorator.TableMetaDataDecorator;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

/**
 * Table meta data decorator for encrypt.
 *
 * @author zhangliang
 */
public final class EncryptTableMetaDataDecorator implements TableMetaDataDecorator<EncryptRule> {
    
    @Override
    public TableMetas decorate(final TableMetas tableMetas, final EncryptRule encryptRule) {
        Map<String, TableMetaData> result = new HashMap<>(tableMetas.getAllTableNames().size(), 1);
        for (String each : tableMetas.getAllTableNames()) {
            result.put(each, decorate(tableMetas.get(each), each, encryptRule));
        }
        return new TableMetas(result);
    }
    
    @Override
    public TableMetaData decorate(final TableMetaData tableMetaData, final String tableName, final EncryptRule encryptRule) {
        return new TableMetaData(getEncryptColumnMetaDataList(tableName, tableMetaData.getColumns().values(), encryptRule), tableMetaData.getIndexes());
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
        String plainColumnName = encryptRule.findPlainColumn(tableName, logicColumnName).orNull();
        String assistedQueryColumnName = encryptRule.findAssistedQueryColumn(tableName, logicColumnName).orNull();
        return new EncryptColumnMetaData(
                logicColumnName, originalColumnMetaData.getDataType(), originalColumnMetaData.isPrimaryKey(), originalColumnMetaData.getName(), plainColumnName, assistedQueryColumnName);
    }
}
