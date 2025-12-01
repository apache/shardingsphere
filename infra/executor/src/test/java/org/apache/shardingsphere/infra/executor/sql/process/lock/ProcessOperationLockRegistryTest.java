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

package org.apache.shardingsphere.infra.executor.sql.process.lock;

import org.awaitility.Awaitility;
import org.junit.jupiter.api.Test;

import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.lessThanOrEqualTo;

class ProcessOperationLockRegistryTest {
    
    @Test
    void assertLockAndNotify() {
        String lockId = "foo_id";
        long startMillis = System.currentTimeMillis();
        Executors.newFixedThreadPool(1).submit(() -> {
            Awaitility.await().pollDelay(50L, TimeUnit.MILLISECONDS).until(() -> true);
            ProcessOperationLockRegistry.getInstance().notify(lockId);
        });
        waitUntilReleaseReady(lockId);
        long currentMillis = System.currentTimeMillis();
        assertThat(currentMillis, greaterThanOrEqualTo(startMillis + 50L));
        assertThat(currentMillis, lessThanOrEqualTo(startMillis + 5000L));
    }
    
    private void waitUntilReleaseReady(final String lockId) {
        ProcessOperationLockRegistry.getInstance().waitUntilReleaseReady(lockId, 1, new ProcessOperationLockReleaseStrategy() {
            
            private final AtomicBoolean firstTime = new AtomicBoolean(true);
            
            @Override
            public boolean isReadyToRelease() {
                return !firstTime.getAndSet(false);
            }
        });
    }
}
