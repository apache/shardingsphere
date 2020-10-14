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

package org.apache.shardingsphere.infra.metadata.model.physical.jdbc.handler.impl;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;

import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.apache.shardingsphere.infra.database.type.DatabaseTypes;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class OracleDatabaseSpecialFacadeTest {

    private static final String USER_NAME = "root";

    private static final String TABLE_NAME_PATTERN = "t_order_0";

    private final DatabaseType oracleDatabaseType = DatabaseTypes.getTrunkDatabaseType("Oracle");

    private final DatabaseType mysqlDatabaseType = DatabaseTypes.getTrunkDatabaseType("MySQL");

    @Mock
    private Connection connection;

    @Mock
    private DatabaseMetaData databaseMetaData;

    @Test
    public void assertGetSchema() throws SQLException {
        when(connection.getMetaData()).thenReturn(databaseMetaData);
        when(databaseMetaData.getUserName()).thenReturn(USER_NAME);
        assertThat(DatabaseSpecialHandlerFacade.getSchema(connection, oracleDatabaseType), is(USER_NAME.toUpperCase()));
    }
    
    @Test
    public void assertDecorateTableNamePattern() {
        assertThat(DatabaseSpecialHandlerFacade.getTableNamePattern(TABLE_NAME_PATTERN, oracleDatabaseType), is(TABLE_NAME_PATTERN.toUpperCase()));
        assertThat(DatabaseSpecialHandlerFacade.getTableNamePattern(TABLE_NAME_PATTERN, mysqlDatabaseType), is(TABLE_NAME_PATTERN));
    }
}
