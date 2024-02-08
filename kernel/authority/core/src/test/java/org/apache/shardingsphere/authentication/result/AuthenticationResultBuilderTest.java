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

package org.apache.shardingsphere.authentication.result;

import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AuthenticationResultBuilderTest {
    
    @Test
    void assertContinuedWithEmpty() {
        AuthenticationResult actual = AuthenticationResultBuilder.continued();
        assertNull(actual.getUsername());
        assertNull(actual.getDatabase());
        assertFalse(actual.isFinished());
    }
    
    @Test
    void assertContinued() {
        AuthenticationResult actual = AuthenticationResultBuilder.continued("username", "127.0.0.1", "database");
        assertThat(actual.getUsername(), is("username"));
        assertThat(actual.getHostname(), is("127.0.0.1"));
        assertThat(actual.getDatabase(), is("database"));
        assertFalse(actual.isFinished());
    }
    
    @Test
    void assertFinished() {
        AuthenticationResult actual = AuthenticationResultBuilder.finished("username", "127.0.0.1", "database");
        assertThat(actual.getUsername(), is("username"));
        assertThat(actual.getHostname(), is("127.0.0.1"));
        assertThat(actual.getDatabase(), is("database"));
        assertTrue(actual.isFinished());
    }
}
