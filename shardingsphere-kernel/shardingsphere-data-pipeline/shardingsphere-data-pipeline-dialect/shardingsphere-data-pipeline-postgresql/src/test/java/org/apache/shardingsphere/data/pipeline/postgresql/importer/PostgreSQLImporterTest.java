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

package org.apache.shardingsphere.data.pipeline.postgresql.importer;

import org.apache.shardingsphere.data.pipeline.api.config.rulealtered.ImporterConfiguration;
import org.apache.shardingsphere.data.pipeline.api.ingest.channel.PipelineChannel;
import org.apache.shardingsphere.data.pipeline.api.ingest.record.Column;
import org.apache.shardingsphere.data.pipeline.api.ingest.record.DataRecord;
import org.apache.shardingsphere.data.pipeline.core.datasource.PipelineDataSourceManager;
import org.apache.shardingsphere.data.pipeline.postgresql.ingest.wal.WalPosition;
import org.apache.shardingsphere.data.pipeline.postgresql.ingest.wal.decode.PostgreSQLLogSequenceNumber;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.postgresql.replication.LogSequenceNumber;

import java.util.Collections;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

@RunWith(MockitoJUnitRunner.class)
public final class PostgreSQLImporterTest {
    
    @Mock
    private ImporterConfiguration importerConfig;
    
    @Mock
    private PipelineDataSourceManager dataSourceManager;
    
    @Mock
    private PipelineChannel channel;
    
    @Test
    public void assertCreateSQLBuilder() {
        String insertSQL = new PostgreSQLImporter(importerConfig, dataSourceManager, channel).createSQLBuilder(Collections.emptyMap()).buildInsertSQL(mockDataRecord());
        assertThat(insertSQL, is("INSERT INTO \"t_order\"(\"id\",\"name\") VALUES(?,?) ON CONFLICT (id) DO NOTHING"));
    }
    
    private DataRecord mockDataRecord() {
        DataRecord result = new DataRecord(new WalPosition(new PostgreSQLLogSequenceNumber(LogSequenceNumber.valueOf(100L))), 2);
        result.setTableName("t_order");
        result.addColumn(new Column("id", 1, true, true));
        result.addColumn(new Column("name", "", true, false));
        return result;
    }
}
