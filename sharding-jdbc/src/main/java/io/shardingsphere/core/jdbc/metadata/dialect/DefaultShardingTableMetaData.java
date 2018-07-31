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

package io.shardingsphere.core.jdbc.metadata.dialect;

import com.google.common.util.concurrent.ListeningExecutorService;
import io.shardingsphere.core.jdbc.metadata.JDBCShardingTableMetaData;
import io.shardingsphere.core.metadata.table.ColumnMetaData;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Sharding table meta data for default.
 *
 * @author zhangliang
 */
public final class DefaultShardingTableMetaData extends JDBCShardingTableMetaData {
    
    public DefaultShardingTableMetaData(final ListeningExecutorService executorService, final Map<String, DataSource> dataSourceMap) {
        super(executorService, dataSourceMap);
    }
    
    @Override
    protected List<ColumnMetaData> getColumnMetaDataList(final Connection connection, final String actualTableName) {
        return Collections.emptyList();
    }
}
