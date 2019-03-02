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

package org.apache.shardingsphere.shardingproxy.transport.mysql.packet.command.admin.quit;

import org.apache.shardingsphere.shardingproxy.transport.mysql.packet.MySQLPacket;
import org.apache.shardingsphere.shardingproxy.transport.mysql.packet.command.MySQLCommandPacketType;
import org.apache.shardingsphere.shardingproxy.transport.mysql.packet.generic.MySQLOKPacket;
import org.apache.shardingsphere.shardingproxy.transport.mysql.payload.MySQLPacketPayload;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Collection;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public final class MySQLComQuitPacketTest {
    
    @Mock
    private MySQLPacketPayload payload;
    
    @Test
    public void assertExecute() {
        Collection<MySQLPacket> actual = new MySQLComQuitPacket().execute();
        assertThat(actual.size(), is(1));
        MySQLPacket mysqlPacket = actual.iterator().next();
        assertThat(mysqlPacket.getSequenceId(), is(1));
        assertThat(((MySQLOKPacket) mysqlPacket).getAffectedRows(), is(0L));
        assertThat(((MySQLOKPacket) mysqlPacket).getLastInsertId(), is(0L));
        assertThat(((MySQLOKPacket) mysqlPacket).getWarnings(), is(0));
        assertThat(((MySQLOKPacket) mysqlPacket).getInfo(), is(""));
    }
    
    @Test
    public void assertWrite() {
        MySQLComQuitPacket actual = new MySQLComQuitPacket();
        actual.write(payload);
        verify(payload).writeInt1(MySQLCommandPacketType.COM_QUIT.getValue());
    }
}
