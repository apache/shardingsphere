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

package org.apache.shardingsphere.driver.jdbc.core.statement;

import org.apache.shardingsphere.driver.common.base.AbstractShardingSphereDataSourceForReplicaQueryTest;
import org.junit.Test;

import java.sql.PreparedStatement;
import java.sql.SQLException;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public final class ReplicaQueryPreparedStatementTest extends AbstractShardingSphereDataSourceForReplicaQueryTest {
    
    @Test(expected = SQLException.class)
    public void assertQueryWithNull() throws SQLException {
        try (PreparedStatement preparedStatement = getReplicaQueryDataSource().getConnection().prepareStatement(null)) {
            preparedStatement.executeQuery();
        }
    }
    
    @Test(expected = SQLException.class)
    public void assertQueryWithEmptyString() throws SQLException {
        try (PreparedStatement preparedStatement = getReplicaQueryDataSource().getConnection().prepareStatement("")) {
            preparedStatement.executeQuery();
        }
    }
    
    @Test
    public void assertGetParameterMetaData() throws SQLException {
        try (PreparedStatement preparedStatement = getReplicaQueryDataSource().getConnection().prepareStatement("SELECT * FROM t_global where id = ?")) {
            assertThat(preparedStatement.getParameterMetaData().getParameterCount(), is(1));
        }
    }
}
