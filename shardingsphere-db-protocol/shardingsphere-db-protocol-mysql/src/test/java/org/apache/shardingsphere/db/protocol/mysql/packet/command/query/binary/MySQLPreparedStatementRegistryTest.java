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

package org.apache.shardingsphere.db.protocol.mysql.packet.command.query.binary;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;

public final class MySQLPreparedStatementRegistryTest {
    
    private static final int CONNECTION_ID = 1;
    
    private static final String SQL = "SELECT * FROM tbl WHERE id=?";
    
    @Before
    public void setup() {
        MySQLPreparedStatementRegistry.getInstance().registerConnection(CONNECTION_ID);
    }
    
    @Test
    public void assertRegisterIfAbsent() {
        assertThat(MySQLPreparedStatementRegistry.getInstance().getConnectionPreparedStatements(CONNECTION_ID).prepareStatement(SQL, 1), is(1));
        MySQLPreparedStatement actual = MySQLPreparedStatementRegistry.getInstance().getConnectionPreparedStatements(CONNECTION_ID).get(1);
        assertThat(actual.getSql(), is(SQL));
        assertThat(actual.getParameterCount(), is(1));
    }
    
    @Test
    public void assertPrepareSameSQL() {
        assertThat(MySQLPreparedStatementRegistry.getInstance().getConnectionPreparedStatements(CONNECTION_ID).prepareStatement(SQL, 1), is(1));
        assertThat(MySQLPreparedStatementRegistry.getInstance().getConnectionPreparedStatements(CONNECTION_ID).prepareStatement(SQL, 1), is(2));
        MySQLPreparedStatement actual = MySQLPreparedStatementRegistry.getInstance().getConnectionPreparedStatements(CONNECTION_ID).get(1);
        assertThat(actual.getSql(), is(SQL));
        assertThat(actual.getParameterCount(), is(1));
        actual = MySQLPreparedStatementRegistry.getInstance().getConnectionPreparedStatements(CONNECTION_ID).get(1);
        assertThat(actual.getSql(), is(SQL));
        assertThat(actual.getParameterCount(), is(1));
    }
    
    @Test
    public void assertCloseStatement() {
        MySQLPreparedStatementRegistry.getInstance().getConnectionPreparedStatements(CONNECTION_ID).prepareStatement(SQL, 1);
        MySQLPreparedStatementRegistry.getInstance().getConnectionPreparedStatements(CONNECTION_ID).closeStatement(1);
        MySQLPreparedStatement actual = MySQLPreparedStatementRegistry.getInstance().getConnectionPreparedStatements(CONNECTION_ID).get(1);
        assertNull(actual);
    }
    
    @After
    public void tearDown() {
        MySQLPreparedStatementRegistry.getInstance().unregisterConnection(CONNECTION_ID);
    }
}
