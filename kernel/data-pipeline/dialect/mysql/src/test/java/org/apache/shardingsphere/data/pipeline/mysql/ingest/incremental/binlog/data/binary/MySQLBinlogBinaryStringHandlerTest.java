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

package org.apache.shardingsphere.data.pipeline.mysql.ingest.incremental.binlog.data.binary;

import org.apache.shardingsphere.data.pipeline.core.metadata.model.PipelineColumnMetaData;
import org.apache.shardingsphere.database.protocol.mysql.packet.binlog.row.column.value.string.MySQLBinaryString;
import org.junit.jupiter.api.Test;

import java.sql.Types;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class MySQLBinlogBinaryStringHandlerTest {
    
    @Test
    void assertHandleWithBinaryColumn() {
        PipelineColumnMetaData metaData = mock(PipelineColumnMetaData.class);
        when(metaData.getDataType()).thenReturn(Types.BINARY);
        MySQLBinaryString value = new MySQLBinaryString(new byte[]{49});
        assertThat(MySQLBinlogBinaryStringHandler.handle(metaData, value), is(new byte[]{49}));
    }
    
    @Test
    void assertHandleWithNotBinaryColumn() {
        PipelineColumnMetaData metaData = mock(PipelineColumnMetaData.class);
        when(metaData.getDataType()).thenReturn(Types.VARCHAR);
        MySQLBinaryString value = new MySQLBinaryString(new byte[]{49});
        assertThat(MySQLBinlogBinaryStringHandler.handle(metaData, value), is("1"));
    }
}
