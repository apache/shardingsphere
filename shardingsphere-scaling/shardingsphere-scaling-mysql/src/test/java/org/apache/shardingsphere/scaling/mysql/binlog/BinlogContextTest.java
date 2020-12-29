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

package org.apache.shardingsphere.scaling.mysql.binlog;

import org.apache.shardingsphere.db.protocol.mysql.constant.MySQLBinaryColumnType;
import org.apache.shardingsphere.db.protocol.mysql.packet.binlog.row.MySQLBinlogTableMapEventPacket;
import org.apache.shardingsphere.db.protocol.mysql.packet.binlog.row.column.MySQLBinlogColumnDef;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

@RunWith(MockitoJUnitRunner.class)
public final class BinlogContextTest {
    
    private static final String TEST_SCHEMA = "test_schema";
    
    private static final String TEST_TABLE = "test_table";
    
    private static final long TEST_TABLE_ID = 1L;
    
    @Mock
    private MySQLBinlogTableMapEventPacket tableMapEventPacket;
    
    private BinlogContext binlogContext;
    
    @Before
    public void setUp() {
        binlogContext = new BinlogContext();
        when(tableMapEventPacket.getSchemaName()).thenReturn(TEST_SCHEMA);
        when(tableMapEventPacket.getTableName()).thenReturn(TEST_TABLE);
    }

    @Test
    public void assertGetTableName() {
        binlogContext.putTableMapEvent(TEST_TABLE_ID, tableMapEventPacket);
        assertThat(binlogContext.getTableName(TEST_TABLE_ID), is(TEST_TABLE));
    }
    
    @Test
    public void assertGetSchemaName() {
        binlogContext.putTableMapEvent(TEST_TABLE_ID, tableMapEventPacket);
        assertThat(binlogContext.getSchemaName(TEST_TABLE_ID), is(TEST_SCHEMA));
    }
    
    @Test
    public void assertGetColumnDefs() {
        binlogContext.putTableMapEvent(TEST_TABLE_ID, tableMapEventPacket);
        List<MySQLBinlogColumnDef> columnDefs = new ArrayList<>(1);
        columnDefs.add(new MySQLBinlogColumnDef(MySQLBinaryColumnType.MYSQL_TYPE_LONG));
        when(tableMapEventPacket.getColumnDefs()).thenReturn(columnDefs);
        assertThat(binlogContext.getColumnDefs(TEST_TABLE_ID), is(columnDefs));
    }
    
    @Test
    public void assertGetTableMapEvent() {
        binlogContext.putTableMapEvent(TEST_TABLE_ID, tableMapEventPacket);
        assertThat(binlogContext.getTableMapEvent(TEST_TABLE_ID), is(tableMapEventPacket));
    }
}
