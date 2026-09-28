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

package org.apache.shardingsphere.database.protocol.firebird.constant.buffer.type;

import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FirebirdBlobParameterBufferTypeTest {
    
    @Test
    void assertValueOf() {
        assertThat(FirebirdBlobParameterBufferType.valueOf(3), is(FirebirdBlobParameterBufferType.TYPE));
        assertThrows(NullPointerException.class, () -> FirebirdBlobParameterBufferType.valueOf(999));
    }
    
    @Test
    void assertIsTraditionalType() {
        assertTrue(FirebirdBlobParameterBufferType.isTraditionalType(1));
        assertFalse(FirebirdBlobParameterBufferType.isTraditionalType(2));
    }
    
    @Test
    void assertCreateBuffer() {
        assertNotNull(FirebirdBlobParameterBufferType.createBuffer());
    }
}
