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

import org.apache.shardingsphere.sql.parser.statement.core.value.identifier.IdentifierValue;
import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;

class ShardingSphereIdentifierTest {
    
    @Test
    void assertConstructor() {
        assertThat(new ShardingSphereIdentifier("foo", mock()).getValue(), is("foo"));
        assertThat(new ShardingSphereIdentifier(new IdentifierValue("`foo`")).getValue(), is("foo"));
        assertThat(new ShardingSphereIdentifier(new IdentifierValue("`foo`"), mock()).getValue(), is("foo"));
    }
    
    @Test
    void assertEqualsWithNotShardingSphereIdentifier() {
        assertThat(new ShardingSphereIdentifier("foo"), not(new Object()));
    }
    
    @Test
    void assertEqualsWithNullValue() {
        assertThat(new ShardingSphereIdentifier("foo"), not(new ShardingSphereIdentifier((String) null)));
        assertThat(new ShardingSphereIdentifier((String) null), not(new ShardingSphereIdentifier("foo")));
        assertThat(new ShardingSphereIdentifier((String) null), is(new ShardingSphereIdentifier((String) null)));
    }
    
    @Test
    void assertEqualsWithCaseSensitive() {
        assertThat(new ShardingSphereIdentifier(new IdentifierValue("`foo`")), is(new ShardingSphereIdentifier(new IdentifierValue("`foo`"))));
        assertThat(new ShardingSphereIdentifier(new IdentifierValue("`foo`")), is(new ShardingSphereIdentifier(new IdentifierValue("foo"))));
        assertThat(new ShardingSphereIdentifier(new IdentifierValue("`foo`")), not(new ShardingSphereIdentifier("FOO")));
    }
    
    @Test
    void assertEqualsWithCaseInsensitive() {
        assertThat(new ShardingSphereIdentifier("foo"), is(new ShardingSphereIdentifier("foo")));
        assertThat(new ShardingSphereIdentifier("foo"), is(new ShardingSphereIdentifier("FOO")));
    }
    
    @Test
    void assertHashCodeWithCaseSensitive() {
        assertThat(new ShardingSphereIdentifier(new IdentifierValue("`foo`")).hashCode(), is(new ShardingSphereIdentifier(new IdentifierValue("`foo`")).hashCode()));
        assertThat(new ShardingSphereIdentifier(new IdentifierValue("`foo`")).hashCode(), not(new ShardingSphereIdentifier(new IdentifierValue("`FOO`")).hashCode()));
    }
    
    @Test
    void assertHashCodeWithCaseInsensitive() {
        assertThat(new ShardingSphereIdentifier("foo").hashCode(), is(new ShardingSphereIdentifier("foo").hashCode()));
        assertThat(new ShardingSphereIdentifier("foo").hashCode(), is(new ShardingSphereIdentifier("FOO").hashCode()));
    }
    
    @Test
    void assertToString() {
        assertThat(new ShardingSphereIdentifier("foo").toString(), is("foo"));
        assertThat(new ShardingSphereIdentifier("FOO").toString(), is("FOO"));
        assertThat(new ShardingSphereIdentifier(new IdentifierValue("`foo`")).toString(), is("foo"));
    }
}
