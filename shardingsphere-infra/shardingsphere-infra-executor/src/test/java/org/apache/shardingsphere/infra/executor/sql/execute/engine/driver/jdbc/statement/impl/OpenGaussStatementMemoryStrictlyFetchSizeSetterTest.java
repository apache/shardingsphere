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

package org.apache.shardingsphere.infra.executor.sql.execute.engine.driver.jdbc.statement.impl;

import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.config.props.ConfigurationPropertyKey;
import org.junit.Test;

import java.sql.SQLException;
import java.sql.Statement;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public final class OpenGaussStatementMemoryStrictlyFetchSizeSetterTest {
    
    @Test
    public void assertSetFetchSize() throws SQLException {
        Statement statement = mock(Statement.class);
        ConfigurationProperties props = mock(ConfigurationProperties.class);
        when(props.getValue(ConfigurationPropertyKey.PROXY_BACKEND_QUERY_FETCH_SIZE)).thenReturn(-1);
        new OpenGaussStatementMemoryStrictlyFetchSizeSetter().setFetchSize(statement, props);
        verify(statement).setFetchSize(1);
    }
    
    @Test
    public void assertGetType() {
        assertThat(new OpenGaussStatementMemoryStrictlyFetchSizeSetter().getType(), is("openGauss"));
    }
}
