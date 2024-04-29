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

package org.apache.shardingsphere.preconditions;

import org.apache.shardingsphere.agent.core.preconditions.AgentPreconditions;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AgentPreconditionsTest {
    
    @Test
    void assertCheckStateSuccess() {
        assertDoesNotThrow(() -> AgentPreconditions.checkState(true, "Can not locate agent jar file by URL /var/tmp."));
    }
    
    @Test
    void assertCheckStateFailed() {
        assertThrows(IllegalStateException.class, () -> AgentPreconditions.checkState(false, "Can not locate agent jar file by URL /var/tmp."));
    }
    
    @Test
    void assertCheckArgumentSuccess() {
        assertDoesNotThrow(() -> AgentPreconditions.checkArgument(true, "SPI class MySQL is not interface."));
    }
    
    @Test
    void assertCheckArgumentFailed() {
        assertThrows(IllegalArgumentException.class, () -> AgentPreconditions.checkArgument(false, "SPI class MySQL is not interface."));
    }
}
