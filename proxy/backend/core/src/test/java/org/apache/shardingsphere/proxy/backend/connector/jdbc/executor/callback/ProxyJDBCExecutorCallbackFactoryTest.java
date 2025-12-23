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

package org.apache.shardingsphere.proxy.backend.connector.jdbc.executor.callback;

import org.apache.shardingsphere.infra.exception.generic.UnsupportedSQLOperationException;
import org.apache.shardingsphere.infra.executor.sql.prepare.driver.jdbc.JDBCDriverType;
import org.apache.shardingsphere.proxy.backend.connector.jdbc.executor.callback.impl.ProxyPreparedStatementExecutorCallback;
import org.apache.shardingsphere.proxy.backend.connector.jdbc.executor.callback.impl.ProxyStatementExecutorCallback;
import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;

class ProxyJDBCExecutorCallbackFactoryTest {
    
    @Test
    void assertNewInstanceWithStatementDriverType() {
        ProxyJDBCExecutorCallback actual = ProxyJDBCExecutorCallbackFactory.newInstance(JDBCDriverType.STATEMENT, mock(), mock(), mock(), mock(), true, true, true);
        assertThat(actual, instanceOf(ProxyStatementExecutorCallback.class));
    }
    
    @Test
    void assertNewInstanceWithPreparedStatementDriverType() {
        ProxyJDBCExecutorCallback actual = ProxyJDBCExecutorCallbackFactory.newInstance(JDBCDriverType.PREPARED_STATEMENT, mock(), mock(), mock(), mock(), false, false, false);
        assertThat(actual, instanceOf(ProxyPreparedStatementExecutorCallback.class));
    }
    
    @Test
    void assertNewInstanceWithUnsupportedDriverType() {
        UnsupportedSQLOperationException ex = assertThrows(UnsupportedSQLOperationException.class, () -> ProxyJDBCExecutorCallbackFactory.newInstance(
                null, mock(), mock(), mock(), mock(), false, false, false));
        assertThat(ex.getMessage(), is("Unsupported SQL operation: Unsupported driver type: `null`."));
    }
}
