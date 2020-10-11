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
import org.apache.shardingsphere.infra.metadata.schema.spi.RuleMetaDataLoader;
import org.apache.shardingsphere.infra.binder.metadata.schema.SchemaMetaData;
import org.apache.shardingsphere.infra.binder.metadata.table.TableMetaData;
import org.apache.shardingsphere.infra.binder.metadata.table.TableMetaDataLoader;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Meta data loader for encrypt.
 */
public final class EncryptMetaDataLoader implements RuleMetaDataLoader<EncryptRule> {
    
    @Override
    public SchemaMetaData load(final DatabaseType databaseType, final Map<String, DataSource> dataSourceMap, final DataNodes dataNodes,
                               final EncryptRule encryptRule, final ConfigurationProperties props, final Collection<String> excludedTableNames) throws SQLException {
        DataSource dataSource = dataSourceMap.values().iterator().next();
        Collection<String> encryptTableNames = encryptRule.getEncryptTableNames();
        Map<String, TableMetaData> result = new HashMap<>(encryptTableNames.size(), 1);
        for (String each : encryptTableNames) {
            if (!excludedTableNames.contains(each)) {
                TableMetaDataLoader.load(dataSource, each, databaseType.getName()).ifPresent(tableMetaData -> result.put(each, tableMetaData));
            }
        }
        return new SchemaMetaData(result);
    }
    
    @Override
    public Optional<TableMetaData> load(final DatabaseType databaseType, final Map<String, DataSource> dataSourceMap, final DataNodes dataNodes, 
                                        final String tableName, final EncryptRule encryptRule, final ConfigurationProperties props) throws SQLException {
        return encryptRule.findEncryptTable(tableName).isPresent() ? TableMetaDataLoader.load(dataSourceMap.values().iterator().next(), tableName, databaseType.getName()) : Optional.empty();
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
