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

package org.apache.shardingsphere.database.protocol.mysql.packet.handshake;

import com.google.common.primitives.Bytes;
import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

class MySQLAuthenticationPluginDataTest {
    
    @Test
    void assertGetAuthPluginData() {
        byte[] actualPart1 = {106, 105, 55, 122, 117, 98, 115, 109};
        byte[] actualPart2 = {68, 102, 53, 122, 65, 49, 84, 79, 85, 115, 116, 113};
        MySQLAuthenticationPluginData actual = new MySQLAuthenticationPluginData(actualPart1, actualPart2);
        assertThat(actual.getAuthenticationPluginDataPart1(), is(actualPart1));
        assertThat(actual.getAuthenticationPluginDataPart2(), is(actualPart2));
        assertThat(actual.getAuthenticationPluginData(), is(Bytes.concat(actualPart1, actualPart2)));
    }
    
    @Test
    void assertGetAuthPluginDataWithoutArguments() {
        MySQLAuthenticationPluginData actual = new MySQLAuthenticationPluginData();
        assertThat(actual.getAuthenticationPluginDataPart1().length, is(8));
        assertThat(actual.getAuthenticationPluginDataPart2().length, is(12));
        assertThat(actual.getAuthenticationPluginData().length, is(20));
    }
}
