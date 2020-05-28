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

package org.apache.shardingsphere.driver.jdbc.unsupported;

import org.apache.shardingsphere.driver.common.base.AbstractShardingSphereDataSourceForShardingTest;
import org.apache.shardingsphere.driver.jdbc.core.connection.ShardingSphereConnection;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.sql.ParameterMetaData;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.ArrayList;
import java.util.List;

public final class UnsupportedOperationParameterMetaTest extends AbstractShardingSphereDataSourceForShardingTest {
    
    private static final String SQL = "SELECT user_id AS usr_id FROM t_order WHERE status = 'init'";
    
    private final List<ShardingSphereConnection> shardingSphereConnections = new ArrayList<>();
    
    private final List<ParameterMetaData> parameterMetaData = new ArrayList<>();
    
    @Before
    public void init() throws SQLException {
        ShardingSphereConnection connection = getShardingSphereDataSource().getConnection();
        shardingSphereConnections.add(connection);
        PreparedStatement preparedStatement = connection.prepareStatement(SQL);
        parameterMetaData.add(preparedStatement.getParameterMetaData());
    }
    
    @After
    public void close() throws SQLException {
        for (ShardingSphereConnection each : shardingSphereConnections) {
            each.close();
        }
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertIsNullable() throws SQLException {
        for (ParameterMetaData each : parameterMetaData) {
            each.getParameterClassName(1);
        }
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertIsSigned() throws SQLException {
        for (ParameterMetaData each : parameterMetaData) {
            each.isSigned(1);
        }
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertGetPrecision() throws SQLException {
        for (ParameterMetaData each : parameterMetaData) {
            each.getPrecision(1);
        }
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertGetScale() throws SQLException {
        for (ParameterMetaData each : parameterMetaData) {
            each.getScale(1);
        }
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertGetParameterType() throws SQLException {
        for (ParameterMetaData each : parameterMetaData) {
            each.getParameterType(1);
        }
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertGetParameterTypeName() throws SQLException {
        for (ParameterMetaData each : parameterMetaData) {
            each.getParameterTypeName(1);
        }
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertGetParameterClassName() throws SQLException {
        for (ParameterMetaData each : parameterMetaData) {
            each.getParameterClassName(1);
        }
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertGetParameterMode() throws SQLException {
        for (ParameterMetaData each : parameterMetaData) {
            each.getParameterMode(1);
        }
    }
}
