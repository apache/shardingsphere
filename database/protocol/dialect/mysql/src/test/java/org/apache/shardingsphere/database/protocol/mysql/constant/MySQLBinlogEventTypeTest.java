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

package org.apache.shardingsphere.database.protocol.mysql.constant;

import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MySQLBinlogEventTypeTest {
    
    @Test
    void assertGetValue() {
        assertThat(MySQLBinlogEventType.WRITE_ROWS_EVENT_V2.getValue(), is(0x1e));
    }
    
    @Test
    void assertValueOfValidType() {
        for (MySQLBinlogEventType each : MySQLBinlogEventType.values()) {
            Optional<MySQLBinlogEventType> eventType = MySQLBinlogEventType.valueOf(each.getValue());
            assertTrue(eventType.isPresent());
            assertThat(eventType.get().getValue(), is(each.getValue()));
        }
    }
    
    @Test
    void assertValueOfInvalidType() {
        assertFalse(MySQLBinlogEventType.valueOf(-1).isPresent());
    }
}
