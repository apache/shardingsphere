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

package org.apache.shardingsphere.shadow.metadata.loader;

import org.apache.shardingsphere.core.rule.ShadowRule;
import org.apache.shardingsphere.underlying.common.metadata.datasource.DataSourceMetas;
import org.apache.shardingsphere.underlying.common.metadata.table.TableMetaData;
import org.apache.shardingsphere.underlying.common.metadata.table.TableMetas;
import org.apache.shardingsphere.underlying.common.metadata.table.init.loader.ConnectionManager;
import org.apache.shardingsphere.underlying.common.metadata.table.init.loader.TableMetaDataLoader;
import org.apache.shardingsphere.underlying.common.metadata.table.init.loader.impl.DefaultTableMetaDataLoader;

import java.sql.SQLException;

/**
 * Table meta data loader for shadow.
 */
public final class ShadowTableMetaDataLoader implements TableMetaDataLoader<ShadowRule> {
    
    private final DefaultTableMetaDataLoader defaultTableMetaDataLoader;
    
    public ShadowTableMetaDataLoader(final DataSourceMetas dataSourceMetas, final ConnectionManager connectionManager) {
        defaultTableMetaDataLoader = new DefaultTableMetaDataLoader(dataSourceMetas, connectionManager);
    }
    
    @Override
    public TableMetaData load(final String tableName, final ShadowRule shadowRule) throws SQLException {
        return defaultTableMetaDataLoader.load(tableName, shadowRule);
    }
    
    @Override
    public TableMetas loadAll(final ShadowRule shadowRule) throws SQLException {
        return defaultTableMetaDataLoader.loadAll(shadowRule);
    }
}
