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

package org.apache.shardingsphere.globalclock.type.tso.provider.local;

import org.apache.shardingsphere.globalclock.provider.GlobalClockProvider;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.isA;

class LocalTSOProviderTest {
    
    private GlobalClockProvider tsoProvider;
    
    @BeforeEach
    void setUp() {
        tsoProvider = TypedSPILoader.getService(GlobalClockProvider.class, "TSO.local");
    }
    
    @Test
    void assertGetCurrentTimestamp() {
        assertThat(tsoProvider.getCurrentTimestamp(), is(0L));
        assertThat(tsoProvider.getNextTimestamp(), is(1L));
        assertThat(tsoProvider.getCurrentTimestamp(), is(1L));
    }
    
    @Test
    void assertGetInstanceByDefault() {
        assertThat(TypedSPILoader.getService(GlobalClockProvider.class, null), isA(LocalTSOProvider.class));
    }
}
