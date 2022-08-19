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

package org.apache.shardingsphere.data.pipeline.core.util;

import org.apache.shardingsphere.mode.repository.cluster.listener.DataChangedEvent;
import org.apache.shardingsphere.mode.repository.cluster.listener.DataChangedEvent.Type;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@SuppressWarnings("rawtypes")
public final class PipelineDistributedBarrierTest {
    
    @BeforeClass
    public static void setUp() {
        PipelineContextUtil.mockModeConfigAndContextManager();
    }
    
    @Test
    public void assertRegisterAndRemove() throws NoSuchFieldException, IllegalAccessException {
        PipelineDistributedBarrier instance = PipelineDistributedBarrier.getInstance();
        String parentPath = "/test";
        instance.register(parentPath, 1);
        Map countDownLatchMap = ReflectionUtil.getFieldValue(instance, "countDownLatchMap", Map.class);
        assertNotNull(countDownLatchMap);
        assertTrue(countDownLatchMap.containsKey(parentPath));
        instance.removeParentNode(parentPath);
        assertFalse(countDownLatchMap.containsKey(parentPath));
    }
    
    @Test
    public void assertAwait() {
        PipelineDistributedBarrier instance = PipelineDistributedBarrier.getInstance();
        String parentPath = "/scaling/j0130317c3054317c7363616c696e675f626d73716c/barrier/enable";
        instance.register(parentPath, 1);
        instance.persistEphemeralChildrenNode(parentPath, 1);
        boolean actual = instance.await(parentPath, 1, TimeUnit.SECONDS);
        assertFalse(actual);
        instance.checkChildrenNodeCount(new DataChangedEvent("/scaling/j0130317c3054317c7363616c696e675f626d73716c/barrier/enable/1", "", Type.ADDED));
        actual = instance.await(parentPath, 1, TimeUnit.SECONDS);
        assertTrue(actual);
    }
}
