/*
 * Copyright 2016-2018 shardingsphere.io.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package io.shardingsphere.proxy.metadata;

import com.google.common.util.concurrent.ListeningExecutorService;
import io.shardingsphere.core.metadata.ShardingMetaData;
import io.shardingsphere.core.metadata.TableMetaData;
import io.shardingsphere.core.rule.DataNode;
import io.shardingsphere.core.rule.ShardingDataSourceNames;
import io.shardingsphere.proxy.backend.jdbc.datasource.JDBCBackendDataSource;
import lombok.Getter;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Map;

/**
 * Sharding metadata for proxy.
 *
 * @author panjuan
 */
@Getter
public final class ProxyShardingMetaData extends ShardingMetaData {
    
    private final JDBCBackendDataSource backendDataSource;
    
    public ProxyShardingMetaData(final ListeningExecutorService executorService, final JDBCBackendDataSource backendDataSource) {
        super(executorService);
        this.backendDataSource = backendDataSource;
    }
    
    @Override
    public TableMetaData getTableMetaData(final DataNode dataNode, final ShardingDataSourceNames shardingDataSourceNames, final Map<String, Connection> connectionMap) throws SQLException {
        return new ShardingMetaDataHandler(
                backendDataSource.getDataSource(shardingDataSourceNames.getRawMasterDataSourceName(dataNode.getDataSourceName())), dataNode.getTableName()).getTableMetaData();
    }
    
    @Override
    public Collection<String> getTableNamesFromDefaultDataSource(final String defaultDataSourceName) throws SQLException {
        return new ShardingMetaDataHandler(backendDataSource.getDataSource(defaultDataSourceName), "").getTableNamesFromDefaultDataSource();
    }
}
