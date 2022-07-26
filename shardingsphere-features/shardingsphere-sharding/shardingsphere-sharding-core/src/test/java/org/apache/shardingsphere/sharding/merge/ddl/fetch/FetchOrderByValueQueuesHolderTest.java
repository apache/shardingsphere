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

import java.util.LinkedList;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public final class FetchOrderByValueQueuesHolderTest {
    
    @Before
    public void setUp() {
        FetchOrderByValueGroupsHolder.remove();
    }
    
    @Test
    public void assertFetchOrderByValueQueuesHolder() {
        assertFalse(FetchOrderByValueGroupsHolder.getOrderByValueGroups().containsKey("t_order_cursor"));
        assertFalse(FetchOrderByValueGroupsHolder.getMinGroupRowCounts().containsKey("t_order_cursor"));
        FetchOrderByValueGroupsHolder.getOrderByValueGroups().computeIfAbsent("t_order_cursor", key -> new LinkedList<>());
        FetchOrderByValueGroupsHolder.getMinGroupRowCounts().put("t_order_cursor", 0L);
        assertTrue(FetchOrderByValueGroupsHolder.getOrderByValueGroups().containsKey("t_order_cursor"));
        assertTrue(FetchOrderByValueGroupsHolder.getMinGroupRowCounts().containsKey("t_order_cursor"));
        FetchOrderByValueGroupsHolder.remove();
        assertTrue(FetchOrderByValueGroupsHolder.getOrderByValueGroups().isEmpty());
        assertTrue(FetchOrderByValueGroupsHolder.getMinGroupRowCounts().isEmpty());
    }
}
