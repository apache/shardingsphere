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

package org.apache.shardingsphere.infra.eventbus;

import com.google.common.eventbus.Subscribe;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

public final class ShardingSphereEventBusTest {
    
    @Test
    public void assertInstance() {
        assertThat(ShardingSphereEventBus.getInstance(), is(ShardingSphereEventBus.getInstance()));
    }
    
    @Test
    public void assertResult() {
        new SubscriberWithoutReturn();
        new Subscriber();
        new Subscriber();
        CompletableEventResult result = ShardingSphereEventBus.getInstance().post(new Event());
        assertEquals(3, result.getResult().size());
        result = ShardingSphereEventBus.getInstance().post(new EventWithoutSubscriber());
        assertEquals(1, result.getResult().size());
    }
    
    private static final class Event {
    }
    
    private static final class EventWithoutSubscriber {
    }
    
    private static final class SubscriberWithoutReturn {
        
        SubscriberWithoutReturn() {
            ShardingSphereEventBus.getInstance().register(this);
        }
        
        @Subscribe
        public void handler(final Event event) {
        }
    }
    
    private static final class Subscriber {
        
        Subscriber() {
            ShardingSphereEventBus.getInstance().register(this);
        }
        
        @Subscribe
        public boolean handler(final Event event) {
            return true;
        }
    }
}
