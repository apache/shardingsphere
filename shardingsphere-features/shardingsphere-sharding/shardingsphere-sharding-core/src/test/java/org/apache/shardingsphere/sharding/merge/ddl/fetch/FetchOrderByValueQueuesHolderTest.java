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

package org.apache.shardingsphere.sharding.merge.ddl.fetch;

import org.junit.Before;
import org.junit.Test;

import java.util.PriorityQueue;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public final class FetchOrderByValueQueuesHolderTest {
    
    @Before
    public void setUp() {
        FetchOrderByValueQueuesHolder.remove();
    }
    
    @Test
    public void assertTrafficContextHolder() {
        assertFalse(FetchOrderByValueQueuesHolder.get().containsKey("t_order_cursor"));
        FetchOrderByValueQueuesHolder.get().computeIfAbsent("t_order_cursor", key -> new PriorityQueue<>());
        assertTrue(FetchOrderByValueQueuesHolder.get().containsKey("t_order_cursor"));
        FetchOrderByValueQueuesHolder.remove();
        assertFalse(FetchOrderByValueQueuesHolder.get().containsKey("t_order_cursor"));
    }
}
