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

package org.apache.shardingsphere.agent.core.spi;

import org.apache.shardingsphere.fixture.agent.AgentServiceEmptySPIFixture;
import org.apache.shardingsphere.fixture.agent.AgentServiceSPIFixture;
import org.apache.shardingsphere.fixture.agent.impl.AgentServiceSPIFixtureImpl;
import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.isA;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AgentServiceLoaderTest {
    
    @Test
    void assertGetServiceLoaderWithNullValue() {
        assertThrows(NullPointerException.class, () -> AgentServiceLoader.getServiceLoader(null));
    }
    
    @Test
    void assertGetServiceLoaderWithNoInterface() {
        assertThrows(IllegalArgumentException.class, () -> AgentServiceLoader.getServiceLoader(Object.class));
    }
    
    @Test
    void assertGetServiceLoaderWithEmptyInstances() {
        assertTrue(AgentServiceLoader.getServiceLoader(AgentServiceEmptySPIFixture.class).getServices().isEmpty());
    }
    
    @Test
    void assertGetServiceLoaderWithImplementSPI() {
        AgentServiceLoader<AgentServiceSPIFixture> actual = AgentServiceLoader.getServiceLoader(AgentServiceSPIFixture.class);
        assertThat(actual.getServices().size(), is(1));
        AgentServiceSPIFixture actualInstance = actual.getServices().iterator().next();
        assertThat(actualInstance, isA(AgentServiceSPIFixtureImpl.class));
        assertThat(actualInstance, is(AgentServiceLoader.getServiceLoader(AgentServiceSPIFixture.class).getServices().iterator().next()));
    }
}
