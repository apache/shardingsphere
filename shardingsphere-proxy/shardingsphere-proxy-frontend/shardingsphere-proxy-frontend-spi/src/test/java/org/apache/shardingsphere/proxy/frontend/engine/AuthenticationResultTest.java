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

package org.apache.shardingsphere.proxy.frontend.engine;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public final class AuthenticationResultTest {
    
    @Test
    public void assertContinuedWithEmpty() {
        AuthenticationResult actual = AuthenticationResult.continued();
        assertNull(actual.getUsername());
        assertNull(actual.getDatabase());
        assertFalse(actual.isFinished());
    }
    
    @Test
    public void assertContinued() {
        AuthenticationResult actual = AuthenticationResult.continued("username", "database");
        assertThat(actual.getUsername(), is("username"));
        assertThat(actual.getDatabase(), is("database"));
        assertFalse(actual.isFinished());
    }
    
    @Test
    public void assertFinished() {
        AuthenticationResult actual = AuthenticationResult.finished("username", "database");
        assertThat(actual.getUsername(), is("username"));
        assertThat(actual.getDatabase(), is("database"));
        assertTrue(actual.isFinished());
    }
}
