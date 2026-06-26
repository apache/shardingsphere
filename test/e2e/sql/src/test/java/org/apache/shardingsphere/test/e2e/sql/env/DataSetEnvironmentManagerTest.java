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

package org.apache.shardingsphere.test.e2e.sql.env;

import org.apache.shardingsphere.database.connector.postgresql.type.PostgreSQLDatabaseType;
import org.apache.shardingsphere.test.e2e.env.runtime.type.scenario.path.ScenarioDataPath;
import org.apache.shardingsphere.test.e2e.env.runtime.type.scenario.path.ScenarioDataPath.Type;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class DataSetEnvironmentManagerTest {
    
    @Test
    void assertCleanDataSkipsUnsupportedTables() throws SQLException {
        Collection<String> actualSQLs = getTruncateSQLs();
        assertTrue(actualSQLs.contains("TRUNCATE TABLE \"t_product\""));
        assertFalse(actualSQLs.contains("TRUNCATE TABLE \"t_product_extend\""));
    }
    
    private Collection<String> getTruncateSQLs() throws SQLException {
        Map<String, DataSource> dataSourceMap = new LinkedHashMap<>(10, 1F);
        Collection<Connection> connections = new LinkedList<>();
        for (int i = 0; i < 10; i++) {
            addDataSource("sql_federation_ds_" + i, dataSourceMap, connections);
        }
        new DataSetEnvironmentManager(new ScenarioDataPath("db_tbl_sql_federation", Type.ACTUAL).getDataSetFile(), dataSourceMap, new PostgreSQLDatabaseType()).cleanData(Collections.emptyList());
        return getPreparedStatementSQLs(connections);
    }
    
    private void addDataSource(final String dataSourceName, final Map<String, DataSource> dataSourceMap, final Collection<Connection> connections) throws SQLException {
        DataSource dataSource = mock(DataSource.class);
        Connection connection = mock(Connection.class);
        DatabaseMetaData metaData = mock(DatabaseMetaData.class);
        PreparedStatement preparedStatement = mock(PreparedStatement.class);
        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.getMetaData()).thenReturn(metaData);
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(metaData.getDatabaseProductName()).thenReturn("PostgreSQL");
        when(metaData.getURL()).thenReturn("jdbc:postgresql://localhost:5432/" + dataSourceName);
        dataSourceMap.put(dataSourceName, dataSource);
        connections.add(connection);
    }
    
    private Collection<String> getPreparedStatementSQLs(final Collection<Connection> connections) throws SQLException {
        Collection<String> result = new LinkedList<>();
        for (Connection each : connections) {
            ArgumentCaptor<String> actualSQLCaptor = ArgumentCaptor.forClass(String.class);
            verify(each, atLeastOnce()).prepareStatement(actualSQLCaptor.capture());
            result.addAll(actualSQLCaptor.getAllValues());
        }
        return result;
    }
}
