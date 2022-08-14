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

package org.apache.shardingsphere.error.vendor;

import org.apache.shardingsphere.infra.util.exception.sql.vendor.ShardingSphereVendorError;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public final class ShardingSphereVendorErrorTest {
    
    @Test
    public void assertCircuitBreakMode() {
        assertThat(ShardingSphereVendorError.CIRCUIT_BREAK_MODE.getSqlState().getValue(), is("01000"));
        assertThat(ShardingSphereVendorError.CIRCUIT_BREAK_MODE.getVendorCode(), is(1000));
        assertThat(ShardingSphereVendorError.CIRCUIT_BREAK_MODE.getReason(), is("Circuit break open, the request has been ignored"));
    }
    
    @Test
    public void assertUnsupportedCommand() {
        assertThat(ShardingSphereVendorError.UNSUPPORTED_COMMAND.getSqlState().getValue(), is("42000"));
        assertThat(ShardingSphereVendorError.UNSUPPORTED_COMMAND.getVendorCode(), is(1998));
        assertThat(ShardingSphereVendorError.UNSUPPORTED_COMMAND.getReason(), is("Unsupported command: %s"));
    }
    
    @Test
    public void assertUnknownException() {
        assertThat(ShardingSphereVendorError.UNKNOWN_EXCEPTION.getSqlState().getValue(), is("42000"));
        assertThat(ShardingSphereVendorError.UNKNOWN_EXCEPTION.getVendorCode(), is(1999));
        assertThat(ShardingSphereVendorError.UNKNOWN_EXCEPTION.getReason(), is("Unknown exception: %s"));
    }
}
