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

package org.apache.shardingsphere.scaling.mysql.client;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public final class ServerVersionTest {
    
    @Test
    public void assertGreaterThan() {
        ServerVersion actual = new ServerVersion("5.7.12");
        assertTrue(actual.greaterThanOrEqualTo(4, 0, 0));
        assertTrue(actual.greaterThanOrEqualTo(5, 6, 0));
        assertTrue(actual.greaterThanOrEqualTo(5, 7, 11));
    }
    
    @Test
    public void assertLowerThan() {
        ServerVersion actual = new ServerVersion("5.6.6");
        assertFalse(actual.greaterThanOrEqualTo(8, 0, 0));
        assertFalse(actual.greaterThanOrEqualTo(5, 7, 11));
        assertFalse(actual.greaterThanOrEqualTo(5, 6, 8));
    }
    
    @Test
    public void assertEqualTo() {
        ServerVersion actual = new ServerVersion("5.6.6");
        assertTrue(actual.greaterThanOrEqualTo(5, 6, 6));
    }
}
