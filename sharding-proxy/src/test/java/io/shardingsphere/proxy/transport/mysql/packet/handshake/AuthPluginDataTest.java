/*
 * Copyright 2016-2018 shardingsphere.io.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package io.shardingsphere.proxy.transport.mysql.packet.handshake;

import com.google.common.primitives.Bytes;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class AuthPluginDataTest {
    
    private AuthPluginData authPluginData;
    
    private final byte[] part1 = {106, 105, 55, 122, 117, 98, 115, 109};
    
    private final byte[] part2 = {68, 102, 53, 122, 65, 49, 84, 79, 85, 115, 116, 113};
    
    @Before
    public void setUp() {
        authPluginData = new AuthPluginData(part1, part2);
    }
    
    @Test
    public void assertGetAuthPluginData() {
        assertThat(authPluginData.getAuthPluginData(), is(Bytes.concat(part1, part2)));
    }
    
    @Test
    public void assertGetAuthPluginDataPart1() {
        assertThat(authPluginData.getAuthPluginDataPart1(), is(part1));
    }
    
    @Test
    public void assertGetAuthPluginDataPart2() {
        assertThat(authPluginData.getAuthPluginDataPart2(), is(part2));
    }
    
    @Test
    public void assertGetAuthPluginDataWithoutArguments() {
        AuthPluginData authPluginData = new AuthPluginData();
        assertThat(authPluginData.getAuthPluginData().length, is(20));
    }
}
