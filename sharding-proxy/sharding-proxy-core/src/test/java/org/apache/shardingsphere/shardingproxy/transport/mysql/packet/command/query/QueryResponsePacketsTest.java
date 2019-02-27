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

package org.apache.shardingsphere.shardingproxy.transport.mysql.packet.command.query;

import org.apache.shardingsphere.core.constant.ShardingConstant;
import org.apache.shardingsphere.shardingproxy.transport.common.packet.command.query.DataHeaderPacket;
import org.apache.shardingsphere.shardingproxy.transport.common.packet.command.query.QueryResponsePackets;
import org.junit.Test;

import java.sql.Types;
import java.util.Arrays;
import java.util.Iterator;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public final class QueryResponsePacketsTest {
    
    @Test
    public void assertGetColumnDefinition41Packets() {
        QueryResponsePackets actual = createQueryResponsePackets();
        assertThat(actual.getDataHeaderPackets().size(), is(2));
        Iterator<DataHeaderPacket> actualDataHeaderPackets = actual.getDataHeaderPackets().iterator();
        assertThat(actualDataHeaderPackets.next().getSequenceId(), is(2));
        assertThat(actualDataHeaderPackets.next().getSequenceId(), is(3));
    }
    
    private QueryResponsePackets createQueryResponsePackets() {
        DataHeaderPacket dataHeaderPacket1 = new DataHeaderPacket(2, ShardingConstant.LOGIC_SCHEMA_NAME, "tbl", "tbl", "id", "id", 10, Types.BIGINT, 0);
        DataHeaderPacket dataHeaderPacket2 = new DataHeaderPacket(3, ShardingConstant.LOGIC_SCHEMA_NAME, "tbl", "tbl", "value", "value", 20, Types.VARCHAR, 0);
        return new QueryResponsePackets(Arrays.asList(dataHeaderPacket1, dataHeaderPacket2), 4);
    }
}
