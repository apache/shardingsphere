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

package org.apache.shardingsphere.database.protocol.opengauss.packet.authentication;

import org.apache.shardingsphere.infra.util.string.HexStringUtils;
import org.junit.jupiter.api.Test;
import org.mockito.internal.configuration.plugins.Plugins;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class OpenGaussMacCalculatorTest {
    
    @Test
    void assertRequestServerMac() throws ReflectiveOperationException {
        OpenGaussAuthenticationHexData authHexData = new OpenGaussAuthenticationHexData();
        Plugins.getMemberAccessor().set(OpenGaussAuthenticationHexData.class.getDeclaredField("salt"), authHexData, "73616c74");
        Plugins.getMemberAccessor().set(OpenGaussAuthenticationHexData.class.getDeclaredField("nonce"), authHexData, "6e6f6e6365");
        String actual = OpenGaussMacCalculator.requestServerMac("password", authHexData, 4096);
        assertThat(actual, is("788471142739dac2d5f7d6de58af1425aec57db7a4461e6ba78d2f93af132445"));
    }
    
    @Test
    void assertRequestClientMac() {
        byte[] actual = OpenGaussMacCalculator.requestClientMac("password", "73616c74", 4096);
        assertThat(HexStringUtils.toHexString(actual), is("8e795a7388652dea59019ec44c5dcc39e7c6a90e1fa1c260cbde4a2e57f1c933"));
    }
    
    @Test
    void assertRequestClientMacWithEmptySalt() {
        assertThrows(IllegalArgumentException.class, () -> OpenGaussMacCalculator.requestClientMac("password", "", 4096));
    }
    
    @Test
    void assertCalculateClientMac() {
        byte[] storedKey = OpenGaussMacCalculator.requestClientMac("password", "73616c74", 4096);
        byte[] actual = OpenGaussMacCalculator.calculateClientMac("00112233445566778899aabbccddeeff00112233445566778899aabbccddeeff", "6e6f6e6365", storedKey);
        assertThat(HexStringUtils.toHexString(actual), is("8503aa194430c80f5c156328ff53a90b1c3ddb9190ffae48eaba46e7e377aba7"));
    }
    
    @Test
    void assertCalculateClientMacWithDifferentLength() {
        byte[] storedKey = OpenGaussMacCalculator.requestClientMac("password", "73616c74", 4096);
        assertThrows(IllegalArgumentException.class, () -> OpenGaussMacCalculator.calculateClientMac("00", "6e6f6e6365", storedKey));
    }
}
