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

package org.apache.shardingsphere.driver.jdbc.adapter;

import org.apache.shardingsphere.driver.jdbc.core.connection.ShardingSphereConnection;
import org.apache.shardingsphere.driver.jdbc.core.datasource.ShardingSphereDataSource;
import org.junit.Before;
import org.junit.Test;

import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.logging.Logger;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public final class WrapperAdapterTest {
    
    private ShardingSphereDataSource shardingSphereDataSource;
    
    @Before
    public void setUp() throws SQLException {
        shardingSphereDataSource = new ShardingSphereDataSource(Collections.emptyMap(), Collections.emptyList(), new Properties());
    }
    
    @Test
    public void assertUnwrapSuccess() throws SQLException {
        assertThat(shardingSphereDataSource.unwrap(Object.class), is(shardingSphereDataSource));
    }
    
    @Test(expected = SQLException.class)
    public void assertUnwrapFailure() throws SQLException {
        shardingSphereDataSource.unwrap(String.class);
    }
    
    @Test
    public void assertIsWrapperFor() {
        assertTrue(shardingSphereDataSource.isWrapperFor(Object.class));
    }
    
    @Test
    public void assertIsNotWrapperFor() {
        assertFalse(shardingSphereDataSource.isWrapperFor(String.class));
    }
    
    @Test
    public void assertRecordMethodInvocationSuccess() {
        List<?> list = mock(List.class);
        when(list.isEmpty()).thenReturn(true);
        shardingSphereDataSource.recordMethodInvocation(List.class, "isEmpty", new Class[]{}, new Object[]{});
        shardingSphereDataSource.replayMethodsInvocation(list);
    }
    
    @Test(expected = NoSuchMethodException.class)
    public void assertRecordMethodInvocationFailure() {
        shardingSphereDataSource.recordMethodInvocation(String.class, "none", new Class[]{}, new Object[]{});
    }
    
    @Test
    public void assertSetLogWriter() {
        assertThat(shardingSphereDataSource.getLogWriter(), instanceOf(PrintWriter.class));
        shardingSphereDataSource.setLogWriter(null);
        assertNull(shardingSphereDataSource.getLogWriter());
    }
    
    @Test
    public void assertGetParentLogger() {
        assertThat(shardingSphereDataSource.getParentLogger().getName(), is(Logger.GLOBAL_LOGGER_NAME));
    }
    
    @Test
    public void assertGetConnectionWithUsername() {
        assertThat(shardingSphereDataSource.getConnection("username", "password"), instanceOf(ShardingSphereConnection.class));
    }
}
