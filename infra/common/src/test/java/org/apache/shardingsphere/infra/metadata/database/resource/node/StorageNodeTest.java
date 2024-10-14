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

package org.apache.shardingsphere.infra.metadata.database.resource.node;

import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;

class StorageNodeTest {
    
    @Test
    void assertEquals() {
        assertThat(new StorageNode("localhost", 3306, "root"), is(new StorageNode("LOCALHOST", 3306, "ROOT")));
    }
    
    @Test
    void assertNotEqualsWithDifferentClassType() {
        assertThat(new StorageNode("localhost", 3306, "root"), not(new Object()));
    }
    
    @Test
    void assertNotEqualsWithDifferentName() {
        assertThat(new StorageNode("localhost", 3306, "root"), not(new StorageNode("12.0.0.1", 3306, "ROOT")));
    }
    
    @Test
    void assertHashcode() {
        assertThat(new StorageNode("localhost", 3306, "root").hashCode(), is(new StorageNode("LOCALHOST", 3306, "ROOT").hashCode()));
    }
    
    @Test
    void assertToString() {
        assertThat(new StorageNode("localhost", 3306, "root").toString(), is("localhost_3306_root"));
    }
}
