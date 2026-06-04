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

package org.apache.shardingsphere.infra.metadata.identifier;

import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;

class ShardingSphereIdentifierTest {
    
    @Test
    void assertConstructorWithValue() {
        assertThat(new ShardingSphereIdentifier("foo").getValue(), is("foo"));
        assertThat(new ShardingSphereIdentifier("foo").getStandardizeValue(), is("foo"));
    }
    
    @Test
    void assertEqualsWithNotShardingSphereIdentifier() {
        assertThat(new ShardingSphereIdentifier("foo"), not(new Object()));
    }
    
    @Test
    void assertEqualsWithNullValue() {
        assertThat(new ShardingSphereIdentifier("foo"), not(new ShardingSphereIdentifier(null)));
        assertThat(new ShardingSphereIdentifier(null), not(new ShardingSphereIdentifier("foo")));
        assertThat(new ShardingSphereIdentifier(null), is(new ShardingSphereIdentifier(null)));
    }
    
    @Test
    void assertEqualsWithNoDatabaseType() {
        assertThat(new ShardingSphereIdentifier("foo"), is(new ShardingSphereIdentifier("foo")));
        assertThat(new ShardingSphereIdentifier("foo"), is(new ShardingSphereIdentifier("FOO")));
    }
    
    @Test
    void assertHashCodeWithNoDatabaseType() {
        assertThat(new ShardingSphereIdentifier("foo").hashCode(), is(new ShardingSphereIdentifier("foo").hashCode()));
        assertThat(new ShardingSphereIdentifier("foo").hashCode(), is(new ShardingSphereIdentifier("FOO").hashCode()));
    }
}
