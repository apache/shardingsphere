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

package org.apache.shardingsphere.shardingproxy.transport.mysql.packet.command;

import org.apache.shardingsphere.shardingproxy.transport.common.packet.CommandResponsePackets;
import org.apache.shardingsphere.shardingproxy.transport.mysql.packet.generic.MySQLOKPacket;
import org.apache.shardingsphere.shardingproxy.transport.spi.DatabasePacket;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public final class CommandResponsePacketsTest {
    
    @Test
    public void assertGetHeadPacket() {
        DatabasePacket packet1 = new MySQLOKPacket(1);
        DatabasePacket packet2 = new MySQLOKPacket(2);
        CommandResponsePackets actual = new CommandResponsePackets(packet1);
        actual.getPackets().add(packet2);
        assertThat(actual.getHeadPacket(), is(packet1));
    }
}
