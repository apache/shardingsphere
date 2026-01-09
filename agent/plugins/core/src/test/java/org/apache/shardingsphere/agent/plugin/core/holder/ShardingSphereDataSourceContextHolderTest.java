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

package org.apache.shardingsphere.agent.plugin.core.holder;

import org.apache.shardingsphere.agent.plugin.core.context.ShardingSphereDataSourceContext;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

class ShardingSphereDataSourceContextHolderTest {
    
    @AfterEach
    void reset() {
        ShardingSphereDataSourceContextHolder.getShardingSphereDataSourceContexts().clear();
    }
    
    @Test
    void assertPutGetAndRemove() {
        ShardingSphereDataSourceContext context = new ShardingSphereDataSourceContext("logic_db", mock(ContextManager.class));
        ShardingSphereDataSourceContextHolder.put("instance_1", context);
        assertThat(ShardingSphereDataSourceContextHolder.getShardingSphereDataSourceContexts().size(), is(1));
        assertThat(ShardingSphereDataSourceContextHolder.getShardingSphereDataSourceContexts().get("instance_1"), is(context));
        ShardingSphereDataSourceContextHolder.remove("instance_1");
        assertTrue(ShardingSphereDataSourceContextHolder.getShardingSphereDataSourceContexts().isEmpty());
    }
}
