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

package org.apache.shardingsphere.data.pipeline.mysql.ingest.binlog;

import org.apache.shardingsphere.db.protocol.mysql.packet.binlog.row.MySQLBinlogTableMapEventPacket;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.HashMap;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class MySQLBinlogContextTest {
    
    private static final String TEST_SCHEMA = "test_schema";
    
    private static final String TEST_TABLE = "test_table";
    
    private static final long TEST_TABLE_ID = 1L;
    
    @Mock
    private MySQLBinlogTableMapEventPacket tableMapEventPacket;
    
    private MySQLBinlogContext binlogContext;
    
    @BeforeEach
    void setUp() {
        binlogContext = new MySQLBinlogContext(4, new HashMap<>());
        when(tableMapEventPacket.getSchemaName()).thenReturn(TEST_SCHEMA);
        when(tableMapEventPacket.getTableName()).thenReturn(TEST_TABLE);
    }
    
    @Test
    void assertGetTableMapEvent() {
        binlogContext.putTableMapEvent(TEST_TABLE_ID, tableMapEventPacket);
        assertThat(binlogContext.getTableMapEvent(TEST_TABLE_ID), is(tableMapEventPacket));
    }
}
