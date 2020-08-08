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

package org.apache.shardingsphere.scaling.core.execute.engine;

import org.apache.shardingsphere.scaling.core.execute.executor.ShardingScalingExecutor;
import org.junit.Test;

import java.util.concurrent.RejectedExecutionException;

import static org.junit.Assert.fail;

public final class ShardingScalingExecuteEngineTest {
    
    @Test
    public void assertSubmitMoreThanMaxWorkerNumber() {
        ShardingScalingExecuteEngine executeEngine = new ShardingScalingExecuteEngine(2);
        try {
            for (int i = 0; i < 5; i++) {
                executeEngine.submit(mockShardingScalingExecutor());
            }
        } catch (final RejectedExecutionException ex) {
            fail();
        }
    }
    
    private ShardingScalingExecutor mockShardingScalingExecutor() {
        return new ShardingScalingExecutor() {
            @Override
            public void run() {
                try {
                    Thread.sleep(100L);
                } catch (final InterruptedException ignored) {
                }
            }
            
            @Override
            public void start() {
            }
            
            @Override
            public void stop() {
            }
        };
    }
}
