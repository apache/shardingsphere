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

package org.apache.shardingsphere.database.protocol.firebird.constant.protocol;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertNull;

class FirebirdConnectionProtocolVersionTest {
    
    private static final int CONNECTION_ID = 1;
    
    @AfterEach
    void tearDown() {
        FirebirdConnectionProtocolVersion.getInstance().unsetProtocolVersion(CONNECTION_ID);
    }
    
    @Test
    void assertSetAndGetProtocolVersion() {
        FirebirdConnectionProtocolVersion.getInstance().setProtocolVersion(CONNECTION_ID, FirebirdProtocolVersion.PROTOCOL_VERSION17);
        assertThat(FirebirdConnectionProtocolVersion.getInstance().getProtocolVersion(CONNECTION_ID),
                is(FirebirdProtocolVersion.PROTOCOL_VERSION17));
    }
    
    @Test
    void assertUnregisterConnection() {
        FirebirdConnectionProtocolVersion.getInstance().setProtocolVersion(CONNECTION_ID, FirebirdProtocolVersion.PROTOCOL_VERSION18);
        FirebirdConnectionProtocolVersion.getInstance().unsetProtocolVersion(CONNECTION_ID);
        assertNull(FirebirdConnectionProtocolVersion.getInstance().getProtocolVersion(CONNECTION_ID));
    }
}
