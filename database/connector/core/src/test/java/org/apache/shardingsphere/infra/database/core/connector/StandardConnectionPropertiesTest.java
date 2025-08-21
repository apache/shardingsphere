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

package org.apache.shardingsphere.infra.database.core.connector;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class StandardConnectionPropertiesTest {
    
    @Test
    void assertIsInSameDatabaseInstance() {
        assertTrue(new StandardConnectionProperties("127.0.0.1", 9999, "foo", "foo")
                .isInSameDatabaseInstance(new StandardConnectionProperties("127.0.0.1", 9999, "bar", "bar")));
    }
    
    @Test
    void assertIsNotInSameDatabaseInstanceWithDifferentHostname() {
        assertFalse(new StandardConnectionProperties("127.0.0.1", 9999, "foo", "foo")
                .isInSameDatabaseInstance(new StandardConnectionProperties("127.0.0.2", 9999, "foo", "foo")));
    }
    
    @Test
    void assertIsNotInSameDatabaseInstanceWithDifferentPort() {
        assertFalse(new StandardConnectionProperties("127.0.0.1", 9999, "foo", "foo")
                .isInSameDatabaseInstance(new StandardConnectionProperties("127.0.0.1", 8888, "foo", "foo")));
    }
}
