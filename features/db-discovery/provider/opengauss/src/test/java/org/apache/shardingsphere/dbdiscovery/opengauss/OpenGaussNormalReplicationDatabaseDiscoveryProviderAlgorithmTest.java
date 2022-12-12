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

package org.apache.shardingsphere.dbdiscovery.opengauss;

import org.junit.Test;

import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.SQLException;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public final class OpenGaussNormalReplicationDatabaseDiscoveryProviderAlgorithmTest {
    
    @Test
    public void assertIsPrimaryInstance() throws SQLException {
        assertTrue(new OpenGaussNormalReplicationDatabaseDiscoveryProviderAlgorithm().isPrimaryInstance(mockDatSource()));
    }
    
    private DataSource mockDatSource() throws SQLException {
        DataSource result = mock(DataSource.class, RETURNS_DEEP_STUBS);
        ResultSet resultSet = mock(ResultSet.class);
        when(result.getConnection().createStatement().executeQuery("SELECT local_role,db_state FROM pg_stat_get_stream_replications()")).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true, false);
        when(resultSet.getString("local_role")).thenReturn("Primary");
        when(resultSet.getString("db_state")).thenReturn("Normal");
        when(result.getConnection().getMetaData().getURL()).thenReturn("jdbc:postgres://127.0.0.1:3306/foo_ds");
        return result;
    }
}
