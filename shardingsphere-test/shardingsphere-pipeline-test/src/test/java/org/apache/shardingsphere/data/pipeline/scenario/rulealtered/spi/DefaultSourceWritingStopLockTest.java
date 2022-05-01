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

package org.apache.shardingsphere.data.pipeline.scenario.rulealtered.spi;

import org.apache.shardingsphere.data.pipeline.core.util.PipelineContextUtil;
import org.junit.Before;
import org.junit.Test;

public final class DefaultSourceWritingStopLockTest {
    
    private final DefaultSourceWritingStopLock defaultSourceWritingStopLock = new DefaultSourceWritingStopLock();
    
    private final String lockName = "lock1";
    
    private final String jobId = "jobId1";
    
    @Before
    public void setup() {
        PipelineContextUtil.mockModeConfigAndContextManager();
    }
    
    @Test
    public void assertSuccessLockReleaseLock() {
        defaultSourceWritingStopLock.lock(lockName, jobId);
        defaultSourceWritingStopLock.releaseLock(lockName, jobId);
    }
    
    @Test
    public void assertSuccessLockTwiceReleaseLock() {
        defaultSourceWritingStopLock.lock(lockName, jobId);
        defaultSourceWritingStopLock.lock(lockName, jobId);
        defaultSourceWritingStopLock.releaseLock(lockName, jobId);
    }
    
    @Test
    public void assertSuccessLockReleaseLockTwice() {
        defaultSourceWritingStopLock.lock(lockName, jobId);
        defaultSourceWritingStopLock.releaseLock(lockName, jobId);
        defaultSourceWritingStopLock.releaseLock(lockName, jobId);
    }
    
    @Test
    public void assertSuccessReleaseNullLock() {
        defaultSourceWritingStopLock.releaseLock(lockName, jobId);
    }
}
