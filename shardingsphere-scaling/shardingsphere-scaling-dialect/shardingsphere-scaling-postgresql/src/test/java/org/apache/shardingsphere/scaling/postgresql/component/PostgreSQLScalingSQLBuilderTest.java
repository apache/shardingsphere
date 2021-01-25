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

package org.apache.shardingsphere.scaling.postgresql.component;

import com.google.common.collect.Maps;
import org.apache.shardingsphere.scaling.core.execute.executor.record.Column;
import org.apache.shardingsphere.scaling.core.execute.executor.record.DataRecord;
import org.apache.shardingsphere.scaling.postgresql.wal.WalPosition;
import org.junit.Test;
import org.postgresql.replication.LogSequenceNumber;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public final class PostgreSQLScalingSQLBuilderTest {
    
    @Test
    public void assertBuildInsertSQL() {
        String actual = new PostgreSQLScalingSQLBuilder(Maps.newHashMap()).buildInsertSQL(mockDataRecord());
        assertThat(actual, is("INSERT INTO \"t_order\"(\"id\",\"name\") VALUES(?,?) ON CONFLICT (id) DO NOTHING"));
    }
    
    private DataRecord mockDataRecord() {
        DataRecord result = new DataRecord(new WalPosition(LogSequenceNumber.valueOf(100L)), 2);
        result.setTableName("t_order");
        result.addColumn(new Column("id", 1, true, true));
        result.addColumn(new Column("name", "", true, false));
        return result;
    }
}
