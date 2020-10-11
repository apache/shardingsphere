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

package org.apache.shardingsphere.infra.binder.metadata.util;

import java.sql.Connection;
import java.sql.SQLException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class JdbcUtilTest {
    
    @Mock
    private Connection connection;
    
    @Test
    public void assertMysqlGetSchema() throws SQLException {
        when(connection.getSchema()).thenReturn("demo_ds");
        String schema = JdbcUtil.getSchema(connection, "");
        assertThat(schema, is("demo_ds"));
    }
    
    @Test
    public void assertOracleGetSchema() {
        String schema = JdbcUtil.getSchema(connection, "Oracle");
        assertThat(schema, is(nullValue()));
    }
}

