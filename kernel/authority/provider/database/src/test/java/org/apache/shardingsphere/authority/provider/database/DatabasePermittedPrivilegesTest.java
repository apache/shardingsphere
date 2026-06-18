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

package org.apache.shardingsphere.authority.provider.database;

import org.apache.shardingsphere.authority.constant.AuthorityConstants;
import org.junit.jupiter.api.Test;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DatabasePermittedPrivilegesTest {
    
    @Test
    void assertHasPrivilegesWithWildcard() {
        assertTrue(new DatabasePermittedPrivileges(Collections.singleton(AuthorityConstants.PRIVILEGE_WILDCARD)).hasPrivileges("foo_db"));
    }
    
    @Test
    void assertHasPrivileges() {
        assertTrue(new DatabasePermittedPrivileges(Collections.singleton("foo_db")).hasPrivileges("foo_db"));
    }
    
    @Test
    void assertHasNotPrivileges() {
        assertFalse(new DatabasePermittedPrivileges(Collections.singleton("foo_db")).hasPrivileges("bar_db"));
    }
}
