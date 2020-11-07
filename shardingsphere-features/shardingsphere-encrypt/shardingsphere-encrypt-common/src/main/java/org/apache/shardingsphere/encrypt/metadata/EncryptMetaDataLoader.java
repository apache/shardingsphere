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
import org.apache.shardingsphere.infra.config.properties.ConfigurationProperties;
import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.apache.shardingsphere.infra.datanode.DataNodes;
import org.apache.shardingsphere.infra.metadata.schema.loader.physical.PhysicalTableMetaDataLoader;
import org.apache.shardingsphere.infra.metadata.schema.loader.spi.ShardingSphereMetaDataLoader;
import org.apache.shardingsphere.infra.metadata.schema.model.physical.PhysicalColumnMetaData;
import org.apache.shardingsphere.infra.metadata.schema.model.physical.PhysicalTableMetaData;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Map;
import java.util.Optional;

/**
 * Meta data loader for encrypt.
 */
public final class EncryptMetaDataLoader implements ShardingSphereMetaDataLoader<EncryptRule> {
    
    @Override
    public Optional<PhysicalTableMetaData> load(final String tableName, final DatabaseType databaseType, final Map<String, DataSource> dataSourceMap, final DataNodes dataNodes,
                                                final EncryptRule encryptRule, final ConfigurationProperties props) throws SQLException {
        return encryptRule.findEncryptTable(tableName).isPresent() ? PhysicalTableMetaDataLoader.load(dataSourceMap.values().iterator().next(), tableName, databaseType) : Optional.empty();
    }
    
    @Override
    public PhysicalTableMetaData decorate(final String tableName, final PhysicalTableMetaData tableMetaData, final EncryptRule encryptRule) {
        return new PhysicalTableMetaData(getEncryptColumnMetaDataList(tableName, tableMetaData.getColumns().values(), encryptRule), tableMetaData.getIndexes().values());
    }
    
    private Collection<PhysicalColumnMetaData> getEncryptColumnMetaDataList(final String tableName,
                                                                            final Collection<PhysicalColumnMetaData> originalColumnMetaDataList, final EncryptRule encryptRule) {
        Collection<PhysicalColumnMetaData> result = new LinkedList<>();
        Collection<String> derivedColumns = encryptRule.getAssistedQueryAndPlainColumns(tableName);
        for (PhysicalColumnMetaData each : originalColumnMetaDataList) {
            if (!derivedColumns.contains(each.getName())) {
                result.add(getEncryptColumnMetaData(tableName, each, encryptRule));
            }
        }
        return result;
    }
    
    private PhysicalColumnMetaData getEncryptColumnMetaData(final String tableName, final PhysicalColumnMetaData originalColumnMetaData, final EncryptRule encryptRule) {
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
