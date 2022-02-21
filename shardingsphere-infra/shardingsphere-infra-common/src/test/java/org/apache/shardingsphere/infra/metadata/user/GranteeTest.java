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

import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public final class GranteeTest {
    
    @Test
    public void assertEquals() {
        Grantee grantee = new Grantee("name", "%");
        Grantee grantee1 = new Grantee("name", "");
        Grantee grantee2 = new Grantee("name", "127.0.0.1");
        assertTrue(grantee.equals(grantee1));
        assertTrue(grantee.equals(grantee2));
    }
    
    @Test
    public void assertHashcode() {
        Grantee grantee = new Grantee("name", "%");
        Grantee grantee1 = new Grantee("name", "");
        Grantee grantee2 = new Grantee("name", "127.0.0.1");
        assertThat(grantee.hashCode(), is(grantee1.hashCode()));
        assertThat(grantee1.hashCode(), not(grantee2.hashCode()));
    }
    
    @Test
    public void assertHostname() {
        Grantee grantee = new Grantee("name", "%");
        Grantee grantee1 = new Grantee("name", "");
        assertThat(grantee.getHostname(), is("%"));
        assertThat(grantee1.getHostname(), is("%"));
    }
}
