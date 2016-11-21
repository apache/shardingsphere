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

package com.dangdang.ddframe.rdb.sharding.id.generator.self;

import com.dangdang.ddframe.rdb.sharding.id.generator.self.fixture.FixClock;
import com.dangdang.ddframe.rdb.sharding.id.generator.self.time.AbstractClock;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

public abstract class AbstractWorkerIdTest {
    
    protected abstract long getWorkerId();
    
    @Before
    public void setup() {
        CommonSelfIdGenerator.setClock(new FixClock(1));
        CommonSelfIdGenerator.initWorkerId();
    }
    
    @After
    public void clear() {
        CommonSelfIdGenerator.setClock(AbstractClock.systemClock());
        CommonSelfIdGenerator.setWorkerId(0L);
    }
    
    @Test
    public void testWorkerId() {
        CommonSelfIdGenerator idGenerator = new CommonSelfIdGenerator();
        
        assertThat((Long) idGenerator.generateId(), is(getWorkerId() << 12L));
        assertThat(idGenerator.getLastTime(), is(CommonSelfIdGenerator.SJDBC_EPOCH));
        assertThat(idGenerator.getSequence(), is(0L));
        assertThat(CommonSelfIdGenerator.getWorkerId(), is(getWorkerId()));
    }
}
