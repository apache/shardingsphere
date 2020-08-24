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

import org.apache.shardingsphere.db.protocol.mysql.packet.command.query.binary.fixture.BinaryStatementRegistryUtil;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;

public final class MySQLBinaryStatementRegistryTest {
    
    private final String sql = "SELECT * FROM tbl WHERE id=?";
    
    @Before
    @After
    public void reset() {
        BinaryStatementRegistryUtil.reset();
    }
    
    @Test
    public void assertRegisterIfAbsent() {
        assertThat(MySQLBinaryStatementRegistry.getInstance().register(sql, 1), is(1));
        MySQLBinaryStatement actual = MySQLBinaryStatementRegistry.getInstance().getBinaryStatement(1);
        assertThat(actual.getSql(), is(sql));
        assertThat(actual.getParametersCount(), is(1));
    }
    
    @Test
    public void assertRegisterIfPresent() {
        assertThat(MySQLBinaryStatementRegistry.getInstance().register(sql, 1), is(1));
        assertThat(MySQLBinaryStatementRegistry.getInstance().register(sql, 1), is(1));
        MySQLBinaryStatement actual = MySQLBinaryStatementRegistry.getInstance().getBinaryStatement(1);
        assertThat(actual.getSql(), is(sql));
        assertThat(actual.getParametersCount(), is(1));
    }
    
    @Test
    public void assertRemoveIfPresent() {
        MySQLBinaryStatementRegistry.getInstance().register(sql, 1);
        MySQLBinaryStatementRegistry.getInstance().remove(1);
        MySQLBinaryStatement actual = MySQLBinaryStatementRegistry.getInstance().getBinaryStatement(1);
        assertNull(actual);
    }
}
