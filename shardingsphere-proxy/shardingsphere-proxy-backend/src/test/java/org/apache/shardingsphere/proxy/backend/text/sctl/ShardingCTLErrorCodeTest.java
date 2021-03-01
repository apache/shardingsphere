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

package org.apache.shardingsphere.proxy.backend.text.sctl;

import org.apache.shardingsphere.proxy.backend.text.sctl.exception.InvalidShardingCTLFormatException;
import org.apache.shardingsphere.proxy.backend.text.sctl.exception.ShardingCTLException;
import org.apache.shardingsphere.proxy.backend.text.sctl.exception.UnsupportedShardingCTLTypeException;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;

public final class ShardingCTLErrorCodeTest {
    
    @Test
    public void assertInvalidFormat() {
        assertThat(ShardingCTLErrorCode.INVALID_FORMAT.getErrorCode(), is(11000));
        assertThat(ShardingCTLErrorCode.INVALID_FORMAT.getSqlState(), is("S11000"));
        assertThat(ShardingCTLErrorCode.INVALID_FORMAT.getErrorMessage(), is("Invalid format for sharding ctl [%s]."));
    }
    
    @Test
    public void assertUnsupportedType() {
        assertThat(ShardingCTLErrorCode.UNSUPPORTED_TYPE.getErrorCode(), is(11001));
        assertThat(ShardingCTLErrorCode.UNSUPPORTED_TYPE.getSqlState(), is("S11001"));
        assertThat(ShardingCTLErrorCode.UNSUPPORTED_TYPE.getErrorMessage(), is("Could not support sctl type [%s]."));
    }
    
    @Test
    public void assertValueOfWithInvalidFormat() {
        assertThat(ShardingCTLErrorCode.valueOf(new InvalidShardingCTLFormatException("test")), is(ShardingCTLErrorCode.INVALID_FORMAT));
    }
    
    @Test
    public void assertValueOfWithUnsupportedType() {
        assertThat(ShardingCTLErrorCode.valueOf(new UnsupportedShardingCTLTypeException("test")), is(ShardingCTLErrorCode.UNSUPPORTED_TYPE));
    }
    
    @Test(expected = UnsupportedOperationException.class)
    public void assertValueOfWithUnsupportedUnsupportedOperationException() {
        ShardingCTLErrorCode.valueOf(mock(ShardingCTLException.class));
    }
}
