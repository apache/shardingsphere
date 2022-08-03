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

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import com.google.common.eventbus.Subscribe;
import java.util.ArrayList;
import java.util.List;
import org.junit.Test;

public final class EventBusContextTest {
    
    @Test
    public void assertEventBusContextTest() {
        StringEvent stringEvent = new StringEvent();
        String event = "event";
        EventBusContext eventBusContext = new EventBusContext();
        eventBusContext.register(stringEvent);
        eventBusContext.post(event);
        List<String> events = stringEvent.getEvents();
        assertThat(events.size(), is(1));
        assertThat(events.get(0), is(event));
    }

    final class StringEvent {

        private List<String> events = new ArrayList<>();

        @Subscribe
        public void lister(final String event) {
            events.add(event);
        }

        public List<String> getEvents() {
            return events;
        }
    }
}
