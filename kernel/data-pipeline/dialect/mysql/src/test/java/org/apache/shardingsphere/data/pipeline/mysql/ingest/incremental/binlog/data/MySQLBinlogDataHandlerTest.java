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

package org.apache.shardingsphere.data.pipeline.mysql.ingest.incremental.binlog.data;

import org.apache.shardingsphere.data.pipeline.core.metadata.model.PipelineColumnMetaData;
import org.apache.shardingsphere.data.pipeline.mysql.ingest.incremental.binlog.data.binary.MySQLBinlogBinaryStringHandler;
import org.apache.shardingsphere.data.pipeline.mysql.ingest.incremental.binlog.data.unsigned.MySQLBinlogUnsignedNumberHandlerEngine;
import org.apache.shardingsphere.database.protocol.mysql.packet.binlog.row.column.value.string.MySQLBinaryString;
import org.apache.shardingsphere.test.infra.framework.extension.mock.AutoMockExtension;
import org.apache.shardingsphere.test.infra.framework.extension.mock.StaticMockSettings;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;

import java.util.Optional;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(AutoMockExtension.class)
@StaticMockSettings({MySQLBinlogBinaryStringHandler.class, MySQLBinlogUnsignedNumberHandlerEngine.class})
class MySQLBinlogDataHandlerTest {
    
    @Mock
    private PipelineColumnMetaData metaData;
    
    @Test
    void assertHandleWithNullValue() {
        assertNull(MySQLBinlogDataHandler.handle(metaData, null));
    }
    
    @Test
    void assertHandleWithMySQLBinaryStringValue() {
        MySQLBinaryString value = mock(MySQLBinaryString.class);
        when(MySQLBinlogBinaryStringHandler.handle(metaData, value)).thenReturn("1");
        assertThat(MySQLBinlogDataHandler.handle(metaData, value), is("1"));
    }
    
    @Test
    void assertHandleWithUnsignedNumberValue() {
        when(MySQLBinlogUnsignedNumberHandlerEngine.handle(metaData, 1)).thenReturn(Optional.of(1));
        assertThat(MySQLBinlogDataHandler.handle(metaData, 1), is(1));
    }
}
