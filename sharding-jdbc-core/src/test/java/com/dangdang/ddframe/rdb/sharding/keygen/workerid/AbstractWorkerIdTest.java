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

package com.dangdang.ddframe.rdb.sharding.keygen.workerid;

import com.dangdang.ddframe.rdb.sharding.keygen.DefaultKeyGenerator;
import com.dangdang.ddframe.rdb.sharding.keygen.TimeService;
import com.dangdang.ddframe.rdb.sharding.keygen.fixture.FixedTimeService;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

public abstract class AbstractWorkerIdTest {
    
    protected abstract long getWorkerId();
    
    @Before
    public void setup() {
        DefaultKeyGenerator.setTimeService(new FixedTimeService(1));
        DefaultKeyGenerator.initWorkerId();
    }
    
    @After
    public void clear() {
        DefaultKeyGenerator.setTimeService(new TimeService());
        DefaultKeyGenerator.setWorkerId(0L);
    }
    
    @Test
    public void assertWorkerId() {
        DefaultKeyGenerator keyGenerator = new DefaultKeyGenerator();
        assertThat(keyGenerator.generateKey().longValue(), is(getWorkerId() << 12L));
        assertThat(keyGenerator.getLastTime(), is(DefaultKeyGenerator.EPOCH));
        assertThat(keyGenerator.getSequence(), is(0L));
        assertThat(DefaultKeyGenerator.getWorkerId(), is(getWorkerId()));
    }
}
