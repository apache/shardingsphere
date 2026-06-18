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

package org.apache.shardingsphere.proxy.frontend.exception;

import org.apache.shardingsphere.database.exception.core.exception.SQLDialectException;
import org.apache.shardingsphere.infra.exception.external.server.ShardingSphereServerException;
import org.apache.shardingsphere.infra.exception.external.sql.ShardingSphereSQLException;
import org.apache.shardingsphere.infra.exception.internal.ShardingSphereInternalException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ExpectedExceptionsTest {
    
    @Test
    void assertIsExpected() {
        assertTrue(ExpectedExceptions.isExpected(ShardingSphereServerException.class));
        assertTrue(ExpectedExceptions.isExpected(SQLDialectException.class));
        assertTrue(ExpectedExceptions.isExpected(ShardingSphereSQLException.class));
    }
    
    @Test
    void assertIsNotExpected() {
        assertFalse(ExpectedExceptions.isExpected(Exception.class));
        assertFalse(ExpectedExceptions.isExpected(IllegalArgumentException.class));
        assertFalse(ExpectedExceptions.isExpected(ShardingSphereInternalException.class));
    }
}
