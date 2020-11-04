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

import com.google.common.collect.Maps;
import org.apache.shardingsphere.scaling.core.execute.executor.importer.PreparedSQL;
import org.apache.shardingsphere.scaling.core.execute.executor.record.Column;
import org.apache.shardingsphere.scaling.core.execute.executor.record.DataRecord;
import org.apache.shardingsphere.scaling.mysql.binlog.BinlogPosition;
import org.hamcrest.Matchers;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public final class MySQLSqlBuilderTest {
    
    @Test
    public void assertBuildInsertSQL() {
        PreparedSQL actual = new MySQLSQLBuilder(Maps.newHashMap()).buildInsertSQL(mockDataRecord());
        assertThat(actual.getSql(), is("INSERT INTO `t_order`(`id`,`name`,`age`) VALUES(?,?,?) ON DUPLICATE KEY UPDATE `name`=?,`age`=?"));
        assertThat(actual.getValuesIndex().toArray(), Matchers.arrayContaining(0, 1, 2, 1, 2));
    }
    
    private DataRecord mockDataRecord() {
        DataRecord result = new DataRecord(new BinlogPosition("", 1), 2);
        result.setTableName("t_order");
        result.addColumn(new Column("id", 1, true, true));
        result.addColumn(new Column("name", "", true, false));
        result.addColumn(new Column("age", 1, true, false));
        return result;
    }
}
