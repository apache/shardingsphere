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

package org.apache.shardingsphere.data.pipeline.cdc.client.sqlbuilder;

import com.google.protobuf.Any;
import com.google.protobuf.Int32Value;
import com.google.protobuf.StringValue;
import org.apache.shardingsphere.data.pipeline.cdc.protocol.response.DataRecordResult.Record;
import org.apache.shardingsphere.data.pipeline.cdc.protocol.response.DataRecordResult.Record.TableMetaData;
import org.junit.Test;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public final class OpenGaussSQLBuilderTest {
    
    @Test
    public void assertBuildInsertSQLWithUniqueKey() {
        OpenGaussSQLBuilder sqlBuilder = new OpenGaussSQLBuilder();
        TableMetaData tableMetaData = TableMetaData.newBuilder().setTableName("t_order").addUniqueKeyNames("order_id").setDatabase("cdc_db").build();
        Record record = Record.newBuilder().setTableMetaData(tableMetaData).putAllAfter(buildAfterMap()).build();
        String actualSql = sqlBuilder.buildInsertSQL(record);
        String expectedSql = "INSERT INTO t_order(order_id,user_id,status) VALUES(?,?,?) ON DUPLICATE KEY UPDATE user_id=EXCLUDED.user_id,status=EXCLUDED.status";
        assertThat(actualSql, is(expectedSql));
    }
    
    private Map<String, Any> buildAfterMap() {
        Map<String, Any> result = new LinkedHashMap<>();
        result.put("order_id", Any.pack(Int32Value.of(1)));
        result.put("user_id", Any.pack(Int32Value.of(2)));
        result.put("status", Any.pack(StringValue.of("OK")));
        return result;
    }
    
    @Test
    public void assertBuildInsertSQLWithoutUniqueKey() {
        OpenGaussSQLBuilder sqlBuilder = new OpenGaussSQLBuilder();
        TableMetaData tableMetaData = TableMetaData.newBuilder().setTableName("t_order").setDatabase("cdc_db").build();
        Record record = Record.newBuilder().setTableMetaData(tableMetaData).putAllAfter(buildAfterMap()).build();
        String actualSql = sqlBuilder.buildInsertSQL(record);
        String expectedSql = "INSERT INTO t_order(order_id,user_id,status) VALUES(?,?,?)";
        assertThat(actualSql, is(expectedSql));
    }
}
