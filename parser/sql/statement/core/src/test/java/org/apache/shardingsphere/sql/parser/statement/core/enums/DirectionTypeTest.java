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

package org.apache.shardingsphere.sql.parser.statement.core.enums;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DirectionTypeTest {
    
    @Test
    void assertIsAggregationType() {
        assertTrue(DirectionType.isAllDirectionType(DirectionType.ALL));
        assertTrue(DirectionType.isAllDirectionType(DirectionType.FORWARD_ALL));
        assertTrue(DirectionType.isAllDirectionType(DirectionType.BACKWARD_ALL));
        assertFalse(DirectionType.isAllDirectionType(DirectionType.NEXT));
        assertFalse(DirectionType.isAllDirectionType(DirectionType.PRIOR));
        assertFalse(DirectionType.isAllDirectionType(DirectionType.FIRST));
        assertFalse(DirectionType.isAllDirectionType(DirectionType.LAST));
        assertFalse(DirectionType.isAllDirectionType(DirectionType.ABSOLUTE_COUNT));
        assertFalse(DirectionType.isAllDirectionType(DirectionType.RELATIVE_COUNT));
        assertFalse(DirectionType.isAllDirectionType(DirectionType.COUNT));
        assertFalse(DirectionType.isAllDirectionType(DirectionType.FORWARD));
        assertFalse(DirectionType.isAllDirectionType(DirectionType.FORWARD_COUNT));
        assertFalse(DirectionType.isAllDirectionType(DirectionType.BACKWARD));
        assertFalse(DirectionType.isAllDirectionType(DirectionType.BACKWARD_COUNT));
    }
    
    @Test
    void assertIsForwardCountDirectionType() {
        assertTrue(DirectionType.isForwardCountDirectionType(DirectionType.NEXT));
        assertTrue(DirectionType.isForwardCountDirectionType(DirectionType.COUNT));
        assertTrue(DirectionType.isForwardCountDirectionType(DirectionType.FORWARD));
        assertTrue(DirectionType.isForwardCountDirectionType(DirectionType.FORWARD_COUNT));
        assertFalse(DirectionType.isForwardCountDirectionType(DirectionType.PRIOR));
        assertFalse(DirectionType.isForwardCountDirectionType(DirectionType.FIRST));
        assertFalse(DirectionType.isForwardCountDirectionType(DirectionType.LAST));
        assertFalse(DirectionType.isForwardCountDirectionType(DirectionType.ABSOLUTE_COUNT));
        assertFalse(DirectionType.isForwardCountDirectionType(DirectionType.RELATIVE_COUNT));
        assertFalse(DirectionType.isForwardCountDirectionType(DirectionType.ALL));
        assertFalse(DirectionType.isForwardCountDirectionType(DirectionType.FORWARD_ALL));
        assertFalse(DirectionType.isForwardCountDirectionType(DirectionType.BACKWARD));
        assertFalse(DirectionType.isForwardCountDirectionType(DirectionType.BACKWARD_COUNT));
        assertFalse(DirectionType.isForwardCountDirectionType(DirectionType.BACKWARD_ALL));
    }
    
    @Test
    void assertIsBackwardCountDirectionType() {
        assertTrue(DirectionType.isBackwardCountDirectionType(DirectionType.PRIOR));
        assertTrue(DirectionType.isBackwardCountDirectionType(DirectionType.BACKWARD));
        assertTrue(DirectionType.isBackwardCountDirectionType(DirectionType.BACKWARD_COUNT));
        assertFalse(DirectionType.isBackwardCountDirectionType(DirectionType.NEXT));
        assertFalse(DirectionType.isBackwardCountDirectionType(DirectionType.FIRST));
        assertFalse(DirectionType.isBackwardCountDirectionType(DirectionType.LAST));
        assertFalse(DirectionType.isBackwardCountDirectionType(DirectionType.ABSOLUTE_COUNT));
        assertFalse(DirectionType.isBackwardCountDirectionType(DirectionType.RELATIVE_COUNT));
        assertFalse(DirectionType.isBackwardCountDirectionType(DirectionType.COUNT));
        assertFalse(DirectionType.isBackwardCountDirectionType(DirectionType.ALL));
        assertFalse(DirectionType.isBackwardCountDirectionType(DirectionType.FORWARD));
        assertFalse(DirectionType.isBackwardCountDirectionType(DirectionType.FORWARD_COUNT));
        assertFalse(DirectionType.isBackwardCountDirectionType(DirectionType.FORWARD_ALL));
        assertFalse(DirectionType.isBackwardCountDirectionType(DirectionType.FORWARD_ALL));
    }
}
