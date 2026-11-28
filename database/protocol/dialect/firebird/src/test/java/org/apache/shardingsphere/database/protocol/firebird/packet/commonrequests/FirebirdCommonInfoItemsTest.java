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

package org.apache.shardingsphere.database.protocol.firebird.packet.commonrequests;

import org.apache.shardingsphere.database.exception.core.exception.protocol.DatabaseProtocolException;
import org.apache.shardingsphere.database.protocol.firebird.payload.FirebirdPacketPayload;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class FirebirdCommonInfoItemsTest {
    
    @Mock
    private FirebirdPacketPayload payload;
    
    @Test
    void assertValueOf() {
        assertThat(FirebirdCommonInfoItems.valueOf(FirebirdCommonInfoItems.END.getCode()), is(FirebirdCommonInfoItems.END));
    }
    
    @Test
    void assertParseCommonInfo() {
        FirebirdCommonInfoItems.parseCommonInfo(payload, FirebirdCommonInfoItems.END);
        verify(payload).writeInt1(FirebirdCommonInfoItems.END.getCode());
    }
    
    @Test
    void assertParseCommonInfoWithUnknownType() {
        assertThrows(DatabaseProtocolException.class, () -> FirebirdCommonInfoItems.parseCommonInfo(payload, FirebirdCommonInfoItems.ERROR));
    }
    
    @Test
    void assertIsCommon() {
        assertTrue(FirebirdCommonInfoItems.DATA_NOT_READY.isCommon());
    }
}
