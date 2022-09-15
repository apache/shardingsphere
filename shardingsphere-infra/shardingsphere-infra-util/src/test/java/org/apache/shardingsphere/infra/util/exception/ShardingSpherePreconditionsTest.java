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

package org.apache.shardingsphere.infra.util.exception;

import org.apache.shardingsphere.infra.util.exception.external.ShardingSphereExternalException;
import org.apache.shardingsphere.infra.util.exception.external.sql.type.wrapper.SQLWrapperException;
import org.apache.shardingsphere.infra.util.exception.internal.ShardingSphereInternalException;
import org.apache.shardingsphere.infra.util.exception.internal.fixture.ShardingSphereInternalExceptionFixture;
import org.junit.Test;

import java.sql.SQLException;

public final class ShardingSpherePreconditionsTest {
    
    @Test(expected = ShardingSphereExternalException.class)
    public void assertCheckStateToThrowsException() {
        ShardingSpherePreconditions.checkState(false, () -> new SQLWrapperException(new SQLException()));
    }
    
    @Test
    public void assertCheckStateToNotThrowException() {
        ShardingSpherePreconditions.checkState(true, () -> new SQLWrapperException(new SQLException()));
    }
    
    @Test(expected = ShardingSphereExternalException.class)
    public void assertCheckNotNullToThrowsExternalException() {
        ShardingSpherePreconditions.checkNotNull(null, new SQLWrapperException(new SQLException()));
    }
    
    @SuppressWarnings("ObviousNullCheck")
    @Test
    public void assertCheckNotNullToNotThrowExternalException() {
        ShardingSpherePreconditions.checkNotNull(new Object(), new SQLWrapperException(new SQLException()));
    }
    
    @Test(expected = ShardingSphereInternalException.class)
    public void assertCheckNotNullToThrowsInternalException() throws ShardingSphereInternalException {
        ShardingSpherePreconditions.checkNotNull(null, new ShardingSphereInternalExceptionFixture("message"));
    }
    
    @SuppressWarnings("ObviousNullCheck")
    @Test
    public void assertCheckNotNullToNotThrowInternalException() throws ShardingSphereInternalException {
        ShardingSpherePreconditions.checkNotNull(new Object(), new ShardingSphereInternalExceptionFixture("message"));
    }
    
    @Test(expected = SQLException.class)
    public void assertCheckNotNullToThrowsSQLException() throws SQLException {
        ShardingSpherePreconditions.checkNotNull(null, new SQLException("message"));
    }
    
    @SuppressWarnings("ObviousNullCheck")
    @Test
    public void assertCheckNotNullToNotThrowSQLException() throws SQLException {
        ShardingSpherePreconditions.checkNotNull(new Object(), new SQLException("message"));
    }
}
