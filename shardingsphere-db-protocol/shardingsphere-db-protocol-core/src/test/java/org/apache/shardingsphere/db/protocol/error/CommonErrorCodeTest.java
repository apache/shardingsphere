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

package org.apache.shardingsphere.db.protocol.error;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public final class CommonErrorCodeTest {
    
    @Test
    public void assertCircuitBreakMode() {
        assertThat(CommonErrorCode.CIRCUIT_BREAK_MODE.getErrorCode(), is(1000));
        assertThat(CommonErrorCode.CIRCUIT_BREAK_MODE.getSqlState(), is("C1000"));
        assertThat(CommonErrorCode.CIRCUIT_BREAK_MODE.getErrorMessage(), is("Circuit break mode is ON."));
    }
    
    @Test
    public void assertSharingTableRulesNotExisted() {
        assertThat(CommonErrorCode.SHARDING_TABLE_RULES_NOT_EXISTED.getErrorCode(), is(1106));
        assertThat(CommonErrorCode.SHARDING_TABLE_RULES_NOT_EXISTED.getSqlState(), is("C1106"));
        assertThat(CommonErrorCode.SHARDING_TABLE_RULES_NOT_EXISTED.getErrorMessage(), is("Sharding table rules %s do not exist in schema %s."));
    }
    
    @Test
    public void assertTableInUsed() {
        assertThat(CommonErrorCode.SHARDING_TABLE_RULES_IN_USED_BY_BINDING_TABLE.getErrorCode(), is(1107));
        assertThat(CommonErrorCode.SHARDING_TABLE_RULES_IN_USED_BY_BINDING_TABLE.getSqlState(), is("C1107"));
        assertThat(CommonErrorCode.SHARDING_TABLE_RULES_IN_USED_BY_BINDING_TABLE.getErrorMessage(), is("Sharding table rules %s are still used by binding table rule."));
    }
    
    @Test
    public void assertUnsupportedCommand() {
        assertThat(CommonErrorCode.UNSUPPORTED_COMMAND.getErrorCode(), is(1998));
        assertThat(CommonErrorCode.UNSUPPORTED_COMMAND.getSqlState(), is("C1998"));
        assertThat(CommonErrorCode.UNSUPPORTED_COMMAND.getErrorMessage(), is("Unsupported command: [%s]"));
    }
    
    @Test
    public void assertUnknownException() {
        assertThat(CommonErrorCode.UNKNOWN_EXCEPTION.getErrorCode(), is(1999));
        assertThat(CommonErrorCode.UNKNOWN_EXCEPTION.getSqlState(), is("C1999"));
        assertThat(CommonErrorCode.UNKNOWN_EXCEPTION.getErrorMessage(), is("Unknown exception: [%s]"));
    }
}
