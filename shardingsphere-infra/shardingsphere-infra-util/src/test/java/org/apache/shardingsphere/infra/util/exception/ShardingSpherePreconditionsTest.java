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

import org.junit.Test;

import java.sql.SQLException;

public final class ShardingSpherePreconditionsTest {
    
    @Test(expected = SQLException.class)
    public void assertCheckStateToThrowsException() throws SQLException {
        ShardingSpherePreconditions.checkState(false, SQLException::new);
    }
    
    @Test
    public void assertCheckStateToNotThrowException() throws SQLException {
        ShardingSpherePreconditions.checkState(true, SQLException::new);
    }
    
    @Test(expected = SQLException.class)
    public void assertCheckNotNullToThrowsException() throws SQLException {
        ShardingSpherePreconditions.checkNotNull(null, SQLException::new);
    }
    
    @Test
    public void assertCheckNotNullToNotThrowException() throws SQLException {
        ShardingSpherePreconditions.checkNotNull(new Object(), SQLException::new);
    }
}
