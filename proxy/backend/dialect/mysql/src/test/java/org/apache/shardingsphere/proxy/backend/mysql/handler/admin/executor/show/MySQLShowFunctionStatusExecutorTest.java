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
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;
import org.apache.shardingsphere.sql.parser.statement.mysql.dal.show.function.MySQLShowFunctionStatusStatement;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
class MySQLShowFunctionStatusExecutorTest {
    
    private static final String DATABASE_PATTERN = "db_%s";
    
    private final DatabaseType databaseType = TypedSPILoader.getService(DatabaseType.class, "MySQL");
    
    @Test
    void assertExecute() throws SQLException {
        MySQLShowFunctionStatusExecutor executor = new MySQLShowFunctionStatusExecutor(new MySQLShowFunctionStatusStatement(databaseType, null));
        executor.execute(mock(ConnectionSession.class), new ShardingSphereMetaData(createDatabases(), mock(), mock(), mock()));
        assertThat(executor.getQueryResultMetaData().getColumnCount(), is(11));
    }
    
    private Collection<ShardingSphereDatabase> createDatabases() {
        return IntStream.range(0, 10)
                .mapToObj(each -> new ShardingSphereDatabase(String.format(DATABASE_PATTERN, each), databaseType, mock(), mock(), Collections.emptyList())).collect(Collectors.toList());
    }
}
