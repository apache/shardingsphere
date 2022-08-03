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

package org.apache.shardingsphere.infra.util.eventbus;

import org.apache.shardingsphere.infra.util.eventbus.fixture.EventListenerFixture;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public final class EventBusContextTest {
    
    @Test
    public void assertEventBusContextTest() {
        EventBusContext eventBusContext = new EventBusContext();
        EventListenerFixture listener = new EventListenerFixture();
        eventBusContext.register(listener);
        eventBusContext.post("foo_event");
        assertThat(listener.getEvents().size(), is(1));
        assertThat(listener.getEvents().get(0), is("foo_event"));
    }
}
