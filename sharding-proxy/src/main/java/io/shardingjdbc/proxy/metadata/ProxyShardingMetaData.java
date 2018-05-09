/*
 * Copyright 1999-2015 dangdang.com.
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

package io.shardingjdbc.proxy.metadata;

import io.shardingjdbc.core.metadata.ColumnMetaData;
import io.shardingjdbc.core.metadata.ShardingMetaData;
import io.shardingjdbc.core.rule.DataNode;
import io.shardingjdbc.core.rule.ShardingDataSourceNames;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Map;

/**
 * Sharding metadata for proxy.
 *
 * @author panjuan
 */
@RequiredArgsConstructor
@Getter
public final class ProxyShardingMetaData extends ShardingMetaData {
    
    private final Map<String, DataSource> dataSourceMap;
    
    @Override
    public Collection<ColumnMetaData> getColumnMetaDataList(final DataNode dataNode, final ShardingDataSourceNames shardingDataSourceNames, Connection connection) throws SQLException {
        return new ShardingMetaDataHandler(dataSourceMap.get(shardingDataSourceNames.getRawMasterDataSourceName(dataNode.getDataSourceName())), dataNode.getTableName()).getColumnMetaDataList();
    }
}


