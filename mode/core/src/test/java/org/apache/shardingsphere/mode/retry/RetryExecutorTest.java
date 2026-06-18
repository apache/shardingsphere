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

package org.apache.shardingsphere.mode.retry;

import org.junit.jupiter.api.Test;

import java.util.function.Predicate;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RetryExecutorTest {
    
    @Test
    void assertExecute() {
        assertTrue(new RetryExecutor(5L, 2L).execute(value -> value > 0, 1));
    }
    
    @Test
    void assertExecuteTimeout() {
        assertFalse(new RetryExecutor(5L, 2L).execute(value -> value > 0, -1));
    }
    
    @Test
    void assertExecuteWithNeverTimeout() {
        assertTrue(new RetryExecutor(-1L, 2L).execute(new Predicate<Integer>() {
            
            private int currentCount;
            
            @Override
            public boolean test(final Integer value) {
                return ++currentCount > 10;
            }
        }, null));
    }
}
