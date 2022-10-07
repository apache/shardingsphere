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

package org.apache.shardingsphere.sql.parser.sql.common.constant;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public final class DirectionTypeTest {
    
    @Test
    public void assertIsAggregationType() {
        assertFalse(DirectionType.isAllDirectionType(DirectionType.NEXT));
        assertFalse(DirectionType.isAllDirectionType(DirectionType.PRIOR));
        assertFalse(DirectionType.isAllDirectionType(DirectionType.FIRST));
        assertFalse(DirectionType.isAllDirectionType(DirectionType.LAST));
        assertFalse(DirectionType.isAllDirectionType(DirectionType.ABSOLUTE_COUNT));
        assertFalse(DirectionType.isAllDirectionType(DirectionType.RELATIVE_COUNT));
        assertFalse(DirectionType.isAllDirectionType(DirectionType.COUNT));
        assertTrue(DirectionType.isAllDirectionType(DirectionType.ALL));
        assertFalse(DirectionType.isAllDirectionType(DirectionType.FORWARD));
        assertFalse(DirectionType.isAllDirectionType(DirectionType.FORWARD_COUNT));
        assertTrue(DirectionType.isAllDirectionType(DirectionType.FORWARD_ALL));
        assertFalse(DirectionType.isAllDirectionType(DirectionType.BACKWARD));
        assertFalse(DirectionType.isAllDirectionType(DirectionType.BACKWARD_COUNT));
        assertTrue(DirectionType.isAllDirectionType(DirectionType.BACKWARD_ALL));
    }
}
