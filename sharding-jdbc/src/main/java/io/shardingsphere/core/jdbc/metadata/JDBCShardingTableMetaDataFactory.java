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

package io.shardingsphere.core.jdbc.metadata;

import com.google.common.util.concurrent.ListeningExecutorService;
import io.shardingsphere.core.constant.DatabaseType;
import io.shardingsphere.core.jdbc.metadata.dialect.DefaultShardingTableMetaData;
import io.shardingsphere.core.jdbc.metadata.dialect.H2ShardingTableMetaData;
import io.shardingsphere.core.jdbc.metadata.dialect.MySQLShardingTableMetaData;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import javax.sql.DataSource;
import java.util.Map;

/**
 * JDBC sharding table meta data factory.
 *
 * @author zhangliang
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class JDBCShardingTableMetaDataFactory {
    
    /**
     * Create new instance of JDBC sharding table meta data.
     * 
     * @param databaseType database type
     * @param executorService executor service
     * @param dataSourceMap data source map
     * @return new instance of JDBC sharding table meta data
     */
    public static JDBCShardingTableMetaData newInstance(final DatabaseType databaseType, final ListeningExecutorService executorService, final Map<String, DataSource> dataSourceMap) {
        switch (databaseType) {
            case H2:
                return new H2ShardingTableMetaData(executorService, dataSourceMap);
            case MySQL:
                return new MySQLShardingTableMetaData(executorService, dataSourceMap);
            default:
                return new DefaultShardingTableMetaData(executorService, dataSourceMap);
        }
    }
}
