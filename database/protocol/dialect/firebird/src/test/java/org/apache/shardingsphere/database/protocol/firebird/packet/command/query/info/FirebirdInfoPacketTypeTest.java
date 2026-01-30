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

package org.apache.shardingsphere.database.protocol.firebird.packet.command.query.info;

import org.apache.shardingsphere.database.protocol.firebird.packet.command.query.info.type.blob.FirebirdBlobInfoPacketType;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.query.info.type.common.FirebirdCommonInfoPacketType;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.query.info.type.sql.FirebirdSQLInfoPacketType;
import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FirebirdInfoPacketTypeTest {
    
    @Test
    void assertSQLInfoPacketType() {
        FirebirdInfoPacketType type = FirebirdSQLInfoPacketType.RECORDS;
        assertThat(type.getCode(), is(FirebirdSQLInfoPacketType.RECORDS.getCode()));
        assertFalse(type.isCommon());
    }
    
    @Test
    void assertCommonInfoPacketType() {
        FirebirdInfoPacketType type = FirebirdCommonInfoPacketType.END;
        assertThat(type.getCode(), is(FirebirdCommonInfoPacketType.END.getCode()));
        assertTrue(type.isCommon());
    }

    @Test
    void assertBlobInfoPacketType() {
        FirebirdInfoPacketType type = FirebirdBlobInfoPacketType.TOTAL_LENGTH;
        assertThat(type.getCode(), is(FirebirdBlobInfoPacketType.TOTAL_LENGTH.getCode()));
        assertFalse(type.isCommon());
    }
}
