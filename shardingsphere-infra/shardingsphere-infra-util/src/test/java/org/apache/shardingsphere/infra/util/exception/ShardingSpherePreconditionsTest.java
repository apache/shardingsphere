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
    public void assertCheckStateToThrowsExternalException() {
        ShardingSpherePreconditions.checkState(false, new SQLWrapperException(new SQLException()));
    }
    
    @Test
    public void assertCheckStateToNotThrowExternalException() {
        ShardingSpherePreconditions.checkState(true, new SQLWrapperException(new SQLException()));
    }
    
    @Test(expected = ShardingSphereInternalException.class)
    public void assertCheckStateToThrowsInternalException() throws ShardingSphereInternalException {
        ShardingSpherePreconditions.checkState(false, new ShardingSphereInternalExceptionFixture("message"));
    }
    
    @Test
    public void assertCheckStateToNotThrowInternalException() throws ShardingSphereInternalException {
        ShardingSpherePreconditions.checkState(true, new ShardingSphereInternalExceptionFixture("message"));
    }
}
