/*
 * Copyright 1999-2015 dangdang.com.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package com.dangdang.ddframe.rdb.sharding.keygen.fixture;

import com.dangdang.ddframe.rdb.sharding.keygen.DefaultKeyGenerator;
import com.dangdang.ddframe.rdb.sharding.keygen.TimeService;
import lombok.RequiredArgsConstructor;

import java.util.concurrent.atomic.AtomicInteger;

@RequiredArgsConstructor
public final class FixedTimeService extends TimeService {
    
    private final int expectedInvokedTimes;
    
    private final AtomicInteger invokedTimes = new AtomicInteger();
    
    private long current = DefaultKeyGenerator.EPOCH;
    
    @Override
    public long getCurrentMillis() {
        if (invokedTimes.getAndIncrement() < expectedInvokedTimes) {
            return current;
        }
        invokedTimes.set(0);
        return ++current;
    }
}
