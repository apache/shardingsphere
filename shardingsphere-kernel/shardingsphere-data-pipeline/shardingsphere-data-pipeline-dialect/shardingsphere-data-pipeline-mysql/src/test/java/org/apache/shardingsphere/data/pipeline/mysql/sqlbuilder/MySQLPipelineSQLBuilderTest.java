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

package org.apache.shardingsphere.data.pipeline.mysql.sqlbuilder;

import com.google.common.collect.ImmutableMap;
import org.apache.shardingsphere.data.pipeline.api.ingest.position.PlaceholderPosition;
import org.apache.shardingsphere.data.pipeline.api.ingest.record.Column;
import org.apache.shardingsphere.data.pipeline.api.ingest.record.DataRecord;
import org.junit.Test;

import java.util.Collections;
import java.util.Set;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public final class MySQLPipelineSQLBuilderTest {
    
    private final MySQLPipelineSQLBuilder sqlBuilder = new MySQLPipelineSQLBuilder(ImmutableMap.<String, Set<String>>builder().put("t2", Collections.singleton("sc")).build());
    
    @Test
    public void assertBuildInsertSQL() {
        String actual = sqlBuilder.buildInsertSQL(mockDataRecord("t1"));
        assertThat(actual, is("INSERT INTO `t1`(`id`,`sc`,`c1`,`c2`,`c3`) VALUES(?,?,?,?,?) ON DUPLICATE KEY UPDATE `sc`=VALUES(`sc`),`c1`=VALUES(`c1`),`c2`=VALUES(`c2`),`c3`=VALUES(`c3`)"));
    }
    
    @Test
    public void assertBuildInsertSQLHasShardingColumn() {
        String actual = sqlBuilder.buildInsertSQL(mockDataRecord("t2"));
        assertThat(actual, is("INSERT INTO `t2`(`id`,`sc`,`c1`,`c2`,`c3`) VALUES(?,?,?,?,?) ON DUPLICATE KEY UPDATE `c1`=VALUES(`c1`),`c2`=VALUES(`c2`),`c3`=VALUES(`c3`)"));
    }
    
    @Test
    public void assertBuildSumCrc32SQL() {
        String actual = sqlBuilder.buildCRC32SQL("t2", "id");
        assertThat(actual, is("SELECT BIT_XOR(CAST(CRC32(`id`) AS UNSIGNED)) AS checksum FROM `t2`"));
    }
    
    private DataRecord mockDataRecord(final String tableName) {
        DataRecord result = new DataRecord(new PlaceholderPosition(), 4);
        result.setTableName(tableName);
        result.addColumn(new Column("id", "", false, true));
        result.addColumn(new Column("sc", "", false, false));
        result.addColumn(new Column("c1", "", true, false));
        result.addColumn(new Column("c2", "", true, false));
        result.addColumn(new Column("c3", "", true, false));
        return result;
    }
}
