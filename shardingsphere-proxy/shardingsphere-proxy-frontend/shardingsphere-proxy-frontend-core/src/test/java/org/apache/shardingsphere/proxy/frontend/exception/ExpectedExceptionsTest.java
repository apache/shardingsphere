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

import org.apache.shardingsphere.infra.config.exception.ShardingSphereConfigurationException;
import org.apache.shardingsphere.infra.exception.ShardingSphereException;
import org.apache.shardingsphere.proxy.backend.exception.BackendException;
import org.apache.shardingsphere.proxy.backend.exception.NoDatabaseSelectedException;
import org.apache.shardingsphere.proxy.backend.text.sctl.exception.ShardingCTLException;
import org.apache.shardingsphere.sql.parser.exception.SQLParsingException;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public final class ExpectedExceptionsTest {
    
    @Test
    public void assertIsExpected() {
        assertTrue(ExpectedExceptions.isExpected(ShardingSphereException.class));
        assertTrue(ExpectedExceptions.isExpected(ShardingSphereConfigurationException.class));
        assertTrue(ExpectedExceptions.isExpected(SQLParsingException.class));
        assertTrue(ExpectedExceptions.isExpected(ShardingCTLException.class));
        assertTrue(ExpectedExceptions.isExpected(BackendException.class));
        assertTrue(ExpectedExceptions.isExpected(NoDatabaseSelectedException.class));
    }
    
    @Test
    public void assertIsNotExpected() {
        assertFalse(ExpectedExceptions.isExpected(Exception.class));
        assertFalse(ExpectedExceptions.isExpected(IllegalArgumentException.class));
    }
}
