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

package org.apache.shardingsphere.data.pipeline.core.lock;

import org.apache.shardingsphere.data.pipeline.core.util.PipelineContextUtil;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import static org.junit.Assert.assertTrue;

@RunWith(MockitoJUnitRunner.class)
public final class PipelineSimpleLockTest {
    
    @BeforeClass
    public static void beforeClass() {
        PipelineContextUtil.mockModeConfigAndContextManager();
    }
    
    @Test
    public void assertTryLockAndReleaseLock() {
        PipelineSimpleLock pipelineSimpleLock = PipelineSimpleLock.getInstance();
        String lockName = "test";
        long timeoutMillis = 50L;
        boolean locked = pipelineSimpleLock.tryLock(lockName, timeoutMillis);
        assertTrue(locked);
        pipelineSimpleLock.releaseLock(lockName);
        locked = pipelineSimpleLock.tryLock(lockName, timeoutMillis);
        assertTrue(locked);
        pipelineSimpleLock.releaseLock(lockName);
    }
    
}
