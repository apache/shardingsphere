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

package org.apache.shardingsphere.infra.metadata.user;

import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GranteeTest {
    
    @Test
    void assertAccept() {
        Grantee grantee = new Grantee("name", "%");
        assertTrue(grantee.accept(new Grantee("name", "")));
        assertTrue(grantee.accept(new Grantee("name", "127.0.0.1")));
    }
    
    @Test
    void assertNotAcceptWithDifferentUsername() {
        assertFalse(new Grantee("name", "%").accept(new Grantee("name1", "")));
    }
    
    @Test
    void assertNotAcceptWithoutPermittedHost() {
        assertFalse(new Grantee("name", "127.0.0.1").accept(new Grantee("name", "127.0.0.2")));
    }
    
    @Test
    void assertEquals() {
        assertThat(new Grantee("name", "%"), is(new Grantee("NAME", "")));
    }
    
    @Test
    void assertNotEqualsWithDifferentClassTypes() {
        assertThat(new Grantee("name", "%"), not(new Object()));
    }
    
    @Test
    void assertNotEqualsWithDifferentUsername() {
        assertThat(new Grantee("name", "%"), not(new Grantee("name1", "%")));
    }
    
    @Test
    void assertNotEqualsWithDifferentHostname() {
        assertThat(new Grantee("name", "%"), not(new Grantee("name", "127.0.0.1")));
    }
    
    @Test
    void assertSameHashCode() {
        assertThat(new Grantee("name", "%").hashCode(), is(new Grantee("name", "").hashCode()));
    }
    
    @Test
    void assertDifferentHashCode() {
        assertThat(new Grantee("name", "").hashCode(), not(new Grantee("name", "127.0.0.1").hashCode()));
    }
    
    @Test
    void assertToString() {
        assertThat(new Grantee("name").toString(), is("name@%"));
        assertThat(new Grantee("name@127.0.0.1").toString(), is("name@127.0.0.1"));
        assertThat(new Grantee("name", "127.0.0.1").toString(), is("name@127.0.0.1"));
        assertThat(new Grantee("name", "%").toString(), is("name@%"));
        assertThat(new Grantee("name", "").toString(), is("name@%"));
    }
}
