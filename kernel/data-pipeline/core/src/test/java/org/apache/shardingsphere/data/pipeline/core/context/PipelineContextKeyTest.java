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
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PipelineContextKeyTest {
    
    @Test
    void assertHashCodeEqualsForProxyMode() {
        PipelineContextKey contextKey1 = PipelineContextKey.build(null, InstanceType.PROXY);
        PipelineContextKey contextKey2 = PipelineContextKey.build("sharding_db", InstanceType.PROXY);
        assertThat(contextKey1.hashCode(), is(contextKey2.hashCode()));
        assertThat(contextKey1, is(contextKey2));
    }
    
    @Test
    void assertHashCodeEqualsForJdbcMode() {
        PipelineContextKey contextKey1 = PipelineContextKey.build("logic_db", InstanceType.JDBC);
        PipelineContextKey contextKey2 = PipelineContextKey.build("sharding_db", InstanceType.JDBC);
        assertTrue(contextKey1.hashCode() != contextKey2.hashCode());
        assertNotEquals(contextKey1, contextKey2);
    }
}
