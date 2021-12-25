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

package org.apache.shardingsphere.data.pipeline.api.datasource;

import org.apache.shardingsphere.infra.database.type.dialect.H2DatabaseType;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import javax.sql.DataSource;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.logging.Logger;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class PipelineDataSourceWrapperTest {

    private static final String CLIENT_USERNAME = "username";

    private static final String CLIENT_PASSWORD = "password";

    private static final int LOGIN_TIMEOUT = 15;

    @Mock(extraInterfaces = AutoCloseable.class)
    private DataSource dataSource;

    @Mock
    private Connection connection;

    @Mock
    private PrintWriter printWriter;

    @Mock
    private Logger parentLogger;

    @Before
    public void setUp() throws SQLException {
        when(dataSource.getConnection()).thenReturn(connection);
        when(dataSource.getConnection(CLIENT_USERNAME, CLIENT_PASSWORD)).thenReturn(connection);
        when(dataSource.getLogWriter()).thenReturn(printWriter);
        when(dataSource.getLoginTimeout()).thenReturn(LOGIN_TIMEOUT);
        when(dataSource.isWrapperFor(any())).thenReturn(Boolean.TRUE);
        when(dataSource.getParentLogger()).thenReturn(parentLogger);
    }

    @Test
    public void assertGetConnection() throws SQLException {
        PipelineDataSourceWrapper dataSourceWrapper = new PipelineDataSourceWrapper(dataSource, new H2DatabaseType());
        assertThat(dataSourceWrapper.getConnection(), is(connection));
        assertThat(dataSourceWrapper.getConnection(CLIENT_USERNAME, CLIENT_PASSWORD), is(connection));
        assertGetLogWriter(dataSourceWrapper.getLogWriter());
        assertGetLoginTimeout(dataSourceWrapper.getLoginTimeout());
        assertIsWrappedFor(dataSourceWrapper.isWrapperFor(any()));
        assertGetParentLogger(dataSourceWrapper.getParentLogger());
    }

    private void assertGetLogWriter(final PrintWriter actual) {
        assertThat(actual, is(printWriter));
    }

    private void assertGetLoginTimeout(final int actual) {
        assertThat(actual, is(LOGIN_TIMEOUT));
    }

    private void assertIsWrappedFor(final boolean actual) {
        assertThat(actual, is(Boolean.TRUE));
    }

    private void assertGetParentLogger(final Logger actual) {
        assertThat(actual, is(parentLogger));
    }

    @Test(expected = SQLException.class)
    public void assertSetLoginTimeoutFailure() throws SQLException {
        doThrow(new SQLException("")).when(dataSource).setLoginTimeout(LOGIN_TIMEOUT);
        new PipelineDataSourceWrapper(dataSource, new H2DatabaseType()).setLoginTimeout(LOGIN_TIMEOUT);
    }

    @Test(expected = SQLException.class)
    public void assertSetLogWriterFailure() throws SQLException {
        doThrow(new SQLException("")).when(dataSource).setLogWriter(printWriter);
        new PipelineDataSourceWrapper(dataSource, new H2DatabaseType()).setLogWriter(printWriter);
    }

    @Test(expected = SQLException.class)
    public void assertCloseExceptionFailure() throws Exception {
        doThrow(new Exception("")).when((AutoCloseable) dataSource).close();
        new PipelineDataSourceWrapper(dataSource, new H2DatabaseType()).close();
    }

    @Test(expected = SQLException.class)
    public void assertCloseSQLExceptionFailure() throws Exception {
        doThrow(new SQLException("")).when((AutoCloseable) dataSource).close();
        new PipelineDataSourceWrapper(dataSource, new H2DatabaseType()).close();
    }
}
