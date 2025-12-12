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

package org.apache.shardingsphere.data.pipeline.core.context;

import org.apache.shardingsphere.infra.instance.metadata.InstanceType;
import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;

class PipelineContextKeyTest {
    
    @Test
    void assertEqualsWithSameObject() {
        PipelineContextKey pipelineContextKey = new PipelineContextKey(InstanceType.JDBC);
        assertThat(pipelineContextKey, is(pipelineContextKey));
    }
    
    @SuppressWarnings({"SimplifiableAssertion", "ConstantValue"})
    @Test
    void assertEqualsWithNull() {
        assertFalse(new PipelineContextKey(InstanceType.JDBC).equals(null));
    }
    
    @Test
    void assertEqualsWithDifferentClassTypes() {
        assertThat(new PipelineContextKey(InstanceType.JDBC), not(new Object()));
    }
    
    @Test
    void assertEqualsWithDifferentInstanceTypes() {
        assertThat(new PipelineContextKey(InstanceType.JDBC), not(new PipelineContextKey(InstanceType.PROXY)));
    }
    
    @Test
    void assertEqualsWithProxyMode() {
        assertThat(new PipelineContextKey(null, InstanceType.PROXY), is(new PipelineContextKey("foo_db", InstanceType.PROXY)));
    }
    
    @Test
    void assertEqualsWithJDBCMode() {
        assertThat(new PipelineContextKey("foo_db", InstanceType.JDBC), is(new PipelineContextKey("foo_db", InstanceType.JDBC)));
    }
    
    @Test
    void assertNotEqualsWithJDBCMode() {
        assertThat(new PipelineContextKey("foo_db", InstanceType.JDBC), not(new PipelineContextKey("bar_db", InstanceType.JDBC)));
    }
    
    @Test
    void assertHashCodeWithProxyMode() {
        assertThat(new PipelineContextKey(InstanceType.PROXY).hashCode(), is(new PipelineContextKey("foo_db", InstanceType.PROXY).hashCode()));
    }
    
    @Test
    void assertHashCodeWithJDBCMode() {
        assertThat(new PipelineContextKey("foo_db", InstanceType.JDBC).hashCode(), not(new PipelineContextKey("bar_db", InstanceType.JDBC).hashCode()));
    }
}
