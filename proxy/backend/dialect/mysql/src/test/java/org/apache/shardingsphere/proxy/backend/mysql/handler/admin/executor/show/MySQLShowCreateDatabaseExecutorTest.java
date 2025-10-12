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

package org.apache.shardingsphere.proxy.backend.mysql.handler.admin.executor.show;

import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.sql.parser.statement.mysql.dal.show.database.MySQLShowCreateDatabaseStatement;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.SQLException;
import java.util.Collections;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
class MySQLShowCreateDatabaseExecutorTest {
    
    private final DatabaseType databaseType = TypedSPILoader.getService(DatabaseType.class, "MySQL");
    
    @Test
    void assertExecute() throws SQLException {
        MySQLShowCreateDatabaseStatement sqlStatement = new MySQLShowCreateDatabaseStatement(databaseType, "foo_db");
        MySQLShowCreateDatabaseExecutor executor = new MySQLShowCreateDatabaseExecutor(sqlStatement);
        executor.execute(mock(),
                new ShardingSphereMetaData(Collections.singleton(new ShardingSphereDatabase("foo_db", databaseType, mock(), mock(), Collections.emptyList())), mock(), mock(), mock()));
        assertThat(executor.getQueryResultMetaData().getColumnCount(), is(2));
        assertTrue(executor.getMergedResult().next());
        assertThat(executor.getMergedResult().getValue(1, Object.class), is("foo_db"));
        assertFalse(executor.getMergedResult().next());
    }
}
