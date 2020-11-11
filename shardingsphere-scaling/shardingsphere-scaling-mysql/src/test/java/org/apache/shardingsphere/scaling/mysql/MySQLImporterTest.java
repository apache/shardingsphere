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

package org.apache.shardingsphere.scaling.mysql;

import org.apache.shardingsphere.scaling.core.config.ImporterConfiguration;
import org.apache.shardingsphere.scaling.core.datasource.DataSourceManager;
import org.apache.shardingsphere.scaling.core.execute.executor.record.Column;
import org.apache.shardingsphere.scaling.core.execute.executor.record.DataRecord;
import org.apache.shardingsphere.scaling.mysql.binlog.BinlogPosition;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

@RunWith(MockitoJUnitRunner.class)
public final class MySQLImporterTest {
    
    @Mock
    private ImporterConfiguration importerConfig;
    
    @Mock
    private DataSourceManager dataSourceManager;
    
    @Test
    public void assertCreateSqlBuilder() {
        MySQLImporter mySQLImporter = new MySQLImporter(importerConfig, dataSourceManager);
        String insertSQL = mySQLImporter.createSQLBuilder().buildInsertSQL(mockDataRecord());
        assertThat(insertSQL, is("INSERT INTO `t_order`(`id`,`name`) VALUES(?,?) ON DUPLICATE KEY UPDATE `name`=VALUES(`name`)"));
    }
    
    private DataRecord mockDataRecord() {
        DataRecord result = new DataRecord(new BinlogPosition("binlog-000001", 4), 2);
        result.setTableName("t_order");
        result.addColumn(new Column("id", 1, true, true));
        result.addColumn(new Column("name", "", true, false));
        return result;
    }
}
