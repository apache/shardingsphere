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

package org.apache.shardingsphere.data.pipeline.mysql.ingest.incremental.binlog;

import org.apache.shardingsphere.database.protocol.mysql.packet.binlog.row.MySQLBinlogTableMapEventPacket;
import org.junit.jupiter.api.Test;

import java.util.HashMap;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class MySQLBinlogContextTest {
    
    @Test
    void assertGetTableMapEvent() {
        MySQLBinlogContext binlogContext = new MySQLBinlogContext(4, new HashMap<>());
        MySQLBinlogTableMapEventPacket tableMapEventPacket = createTableMapEventPacket();
        binlogContext.putTableMapEvent(tableMapEventPacket);
        assertThat(binlogContext.getTableMapEvent(1L), is(tableMapEventPacket));
    }
    
    private MySQLBinlogTableMapEventPacket createTableMapEventPacket() {
        MySQLBinlogTableMapEventPacket result = mock(MySQLBinlogTableMapEventPacket.class);
        when(result.getTableId()).thenReturn(1L);
        return result;
    }
}
