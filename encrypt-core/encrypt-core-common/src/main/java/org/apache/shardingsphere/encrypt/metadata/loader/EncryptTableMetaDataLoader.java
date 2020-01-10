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

package org.apache.shardingsphere.encrypt.metadata.loader;

import org.apache.shardingsphere.encrypt.metadata.decorator.EncryptTableMetaDataDecorator;
import org.apache.shardingsphere.encrypt.rule.EncryptRule;
import org.apache.shardingsphere.underlying.common.metadata.datasource.DataSourceMetas;
import org.apache.shardingsphere.underlying.common.metadata.table.TableMetaData;
import org.apache.shardingsphere.underlying.common.metadata.table.TableMetas;
import org.apache.shardingsphere.underlying.common.metadata.table.init.loader.ConnectionManager;
import org.apache.shardingsphere.underlying.common.metadata.table.init.loader.TableMetaDataLoader;
import org.apache.shardingsphere.underlying.common.metadata.table.init.loader.impl.DefaultTableMetaDataLoader;

import java.sql.SQLException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Table meta data loader for encrypt.
 *
 * @author zhangliang
 * @author panjuan
 */
public final class EncryptTableMetaDataLoader implements TableMetaDataLoader<EncryptRule> {
    
    private final DefaultTableMetaDataLoader defaultTableMetaDataLoader;
    
    private final EncryptTableMetaDataDecorator encryptTableMetaDataDecorator;
    
    public EncryptTableMetaDataLoader(final DataSourceMetas dataSourceMetas, final ConnectionManager connectionManager) {
        defaultTableMetaDataLoader = new DefaultTableMetaDataLoader(dataSourceMetas, connectionManager);
        encryptTableMetaDataDecorator = new EncryptTableMetaDataDecorator();
    }
    
    @Override
    public TableMetaData load(final String tableName, final EncryptRule encryptRule) throws SQLException {
        return encryptTableMetaDataDecorator.decorate(defaultTableMetaDataLoader.load(tableName, encryptRule), tableName, encryptRule);
    }
    
    @Override
    public TableMetas loadAll(final EncryptRule encryptRule) throws SQLException {
        TableMetas tableMetas = defaultTableMetaDataLoader.loadAll(encryptRule);
        Collection<String> allTableNames = tableMetas.getAllTableNames();
        Map<String, TableMetaData> result = new HashMap<>(allTableNames.size(), 1);
        for (String each : allTableNames) {
            result.put(each, encryptTableMetaDataDecorator.decorate(tableMetas.get(each), each, encryptRule));
        }
        return new TableMetas(result);
    }
}
