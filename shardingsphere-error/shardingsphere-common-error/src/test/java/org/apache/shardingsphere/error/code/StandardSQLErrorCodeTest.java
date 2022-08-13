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

package org.apache.shardingsphere.error.code;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public final class StandardSQLErrorCodeTest {
    
    @Test
    public void assertCircuitBreakMode() {
        assertThat(StandardSQLErrorCode.CIRCUIT_BREAK_MODE.getVendorCode(), is(1000));
        assertThat(StandardSQLErrorCode.CIRCUIT_BREAK_MODE.getSqlState().getValue(), is("C1000"));
        assertThat(StandardSQLErrorCode.CIRCUIT_BREAK_MODE.getReason(), is("Circuit break mode is ON"));
    }
    
    @Test
    public void assertUnsupportedCommand() {
        assertThat(StandardSQLErrorCode.UNSUPPORTED_COMMAND.getVendorCode(), is(1998));
        assertThat(StandardSQLErrorCode.UNSUPPORTED_COMMAND.getSqlState().getValue(), is("C1998"));
        assertThat(StandardSQLErrorCode.UNSUPPORTED_COMMAND.getReason(), is("Unsupported command: %s"));
    }
    
    @Test
    public void assertUnknownException() {
        assertThat(StandardSQLErrorCode.UNKNOWN_EXCEPTION.getVendorCode(), is(1999));
        assertThat(StandardSQLErrorCode.UNKNOWN_EXCEPTION.getSqlState().getValue(), is("C1999"));
        assertThat(StandardSQLErrorCode.UNKNOWN_EXCEPTION.getReason(), is("Unknown exception: %s"));
    }
}
