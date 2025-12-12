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

package org.apache.shardingsphere.database.connector.core.jdbcurl.judger;

import org.apache.shardingsphere.database.connector.core.jdbcurl.parser.ConnectionProperties;
import org.junit.jupiter.api.Test;

import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DatabaseInstanceJudgerTest {
    
    @Test
    void assertIsInSameDatabaseInstance() {
        ConnectionProperties connectionProps1 = new ConnectionProperties("127.0.0.1", 9999, "foo", "foo", new Properties());
        ConnectionProperties connectionProps2 = new ConnectionProperties("127.0.0.1", 9999, "bar", "bar", new Properties());
        assertTrue(DatabaseInstanceJudger.isInSameDatabaseInstance(connectionProps1, connectionProps2));
    }
    
    @Test
    void assertIsNotInSameDatabaseInstanceWithDifferentHostname() {
        ConnectionProperties connectionProps1 = new ConnectionProperties("127.0.0.1", 9999, "foo", "foo", new Properties());
        ConnectionProperties connectionProps2 = new ConnectionProperties("127.0.0.2", 9999, "foo", "foo", new Properties());
        assertFalse(DatabaseInstanceJudger.isInSameDatabaseInstance(connectionProps1, connectionProps2));
    }
    
    @Test
    void assertIsNotInSameDatabaseInstanceWithDifferentPort() {
        ConnectionProperties connectionProps1 = new ConnectionProperties("127.0.0.1", 9999, "foo", "foo", new Properties());
        ConnectionProperties connectionProps2 = new ConnectionProperties("127.0.0.1", 8888, "foo", "foo", new Properties());
        assertFalse(DatabaseInstanceJudger.isInSameDatabaseInstance(connectionProps1, connectionProps2));
    }
}
