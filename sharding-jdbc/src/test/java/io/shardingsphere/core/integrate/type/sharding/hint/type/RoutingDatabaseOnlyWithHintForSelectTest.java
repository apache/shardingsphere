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

package io.shardingsphere.core.integrate.type.sharding.hint.type;

import io.shardingsphere.core.constant.DatabaseType;
import io.shardingsphere.core.integrate.sql.DatabaseTestSQL;
import io.shardingsphere.core.integrate.type.sharding.hint.base.AbstractRoutingDatabaseOnlyWithHintTest;
import io.shardingsphere.core.integrate.type.sharding.hint.helper.HintDatabaseShardingValueHelper;
import io.shardingsphere.core.jdbc.core.datasource.ShardingDataSource;
import io.shardingsphere.core.util.SQLPlaceholderUtil;
import org.dbunit.DatabaseUnitException;
import org.junit.Before;
import org.junit.Test;

import java.sql.SQLException;
import java.util.Map;

public class RoutingDatabaseOnlyWithHintForSelectTest extends AbstractRoutingDatabaseOnlyWithHintTest {
    
    private Map<DatabaseType, ShardingDataSource> shardingDataSources;
    
    @Before
    public void init() throws SQLException {
        shardingDataSources = initShardingDataSources();
    }
    
    @Test
    public void assertSelectEqualsWithSingleTable() throws SQLException, DatabaseUnitException {
        for (Map.Entry<DatabaseType, ShardingDataSource> each : shardingDataSources.entrySet()) {
            String sql = SQLPlaceholderUtil.replacePreparedStatement(DatabaseTestSQL.SELECT_EQUALS_WITH_SINGLE_TABLE_SQL);
            assertDataSet("integrate/dataset/sharding/db/expect/select/SelectEqualsWithSingleTable_0.xml", new HintDatabaseShardingValueHelper(10), 
                    each.getValue().getConnection(), sql, each.getKey(), 10, 1000);
            assertDataSet("integrate/dataset/sharding/db/expect/select/SelectEqualsWithSingleTable_1.xml", new HintDatabaseShardingValueHelper(12), 
                    each.getValue().getConnection(), sql, each.getKey(), 12, 1201);
            assertDataSet("integrate/dataset/Empty.xml", new HintDatabaseShardingValueHelper(12), 
                    each.getValue().getConnection(), sql, each.getKey(), 12, 1000);
        }
    }
}
