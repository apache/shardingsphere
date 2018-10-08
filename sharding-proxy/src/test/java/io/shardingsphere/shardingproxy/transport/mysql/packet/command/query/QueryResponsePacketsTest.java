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

package io.shardingsphere.shardingproxy.transport.mysql.packet.command.query;

import io.shardingsphere.core.constant.ShardingConstant;
import io.shardingsphere.shardingproxy.transport.mysql.constant.ColumnType;
import io.shardingsphere.shardingproxy.transport.mysql.packet.generic.EofPacket;
import org.junit.Test;

import java.util.Arrays;
import java.util.Iterator;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public final class QueryResponsePacketsTest {
    
    @Test
    public void assertGetColumnDefinition41Packets() {
        QueryResponsePackets actual = createQueryResponsePackets();
        assertThat(actual.getColumnDefinition41Packets().size(), is(2));
        Iterator<ColumnDefinition41Packet> actualColumnDefinition41Packets = actual.getColumnDefinition41Packets().iterator();
        assertThat(actualColumnDefinition41Packets.next().getSequenceId(), is(2));
        assertThat(actualColumnDefinition41Packets.next().getSequenceId(), is(3));
    }
    
    @Test
    public void assertGetColumnCount() {
        assertThat(createQueryResponsePackets().getColumnCount(), is(2));
    }
    
    @Test
    public void assertGetColumnTypes() {
        assertThat(createQueryResponsePackets().getColumnTypes(), is(Arrays.asList(ColumnType.MYSQL_TYPE_LONG, ColumnType.MYSQL_TYPE_VARCHAR)));
    }
    
    private QueryResponsePackets createQueryResponsePackets() {
        FieldCountPacket fieldCountPacket = new FieldCountPacket(1, 2);
        ColumnDefinition41Packet columnDefinition41Packet1 = new ColumnDefinition41Packet(2, ShardingConstant.LOGIC_SCHEMA_NAME, "tbl", "tbl", "id", "id", 10, ColumnType.MYSQL_TYPE_LONG, 0);
        ColumnDefinition41Packet columnDefinition41Packet2 = new ColumnDefinition41Packet(3, ShardingConstant.LOGIC_SCHEMA_NAME, "tbl", "tbl", "value", "value", 20, ColumnType.MYSQL_TYPE_VARCHAR, 0);
        EofPacket eofPacket = new EofPacket(4);
        return new QueryResponsePackets(fieldCountPacket, Arrays.asList(columnDefinition41Packet1, columnDefinition41Packet2), eofPacket);
    }
}
