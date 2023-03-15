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
import org.apache.shardingsphere.data.pipeline.cdc.protocol.response.DataRecordResult.Record.MetaData;
import org.apache.shardingsphere.data.pipeline.cdc.protocol.response.TableColumn;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public final class PostgreSQLSQLBuilderTest {
    
    @Test
    public void assertBuildInsertSQLWithUniqueKey() {
        PostgreSQLSQLBuilder sqlBuilder = new PostgreSQLSQLBuilder();
        MetaData metaData = MetaData.newBuilder().setTableName("t_order").setDatabase("cdc_db").build();
        Record record = Record.newBuilder().setMetaData(metaData).addAllAfter(buildAfter()).build();
        String actualSql = sqlBuilder.buildInsertSQL(record, Collections.singletonList("order_id"));
        String expectedSql = "INSERT INTO t_order(order_id,user_id,status) VALUES(?,?,?) ON CONFLICT (order_id) DO UPDATE SET user_id=EXCLUDED.user_id,status=EXCLUDED.status";
        assertThat(actualSql, is(expectedSql));
    }
    
    private List<TableColumn> buildAfter() {
        List<TableColumn> result = new LinkedList<>();
        result.add(TableColumn.newBuilder().setName("order_id").setValue(Any.pack(Int32Value.of(1))).build());
        result.add(TableColumn.newBuilder().setName("user_id").setValue(Any.pack(Int32Value.of(2))).build());
        result.add(TableColumn.newBuilder().setName("status").setValue(Any.pack(StringValue.of("OK"))).build());
        return result;
    }
    
    @Test
    public void assertBuildInsertSQLWithoutUniqueKey() {
        PostgreSQLSQLBuilder sqlBuilder = new PostgreSQLSQLBuilder();
        MetaData metaData = MetaData.newBuilder().setTableName("t_order").setDatabase("cdc_db").build();
        Record record = Record.newBuilder().setMetaData(metaData).addAllAfter(buildAfter()).build();
        String actualSql = sqlBuilder.buildInsertSQL(record, Collections.emptyList());
        String expectedSql = "INSERT INTO t_order(order_id,user_id,status) VALUES(?,?,?)";
        assertThat(actualSql, is(expectedSql));
    }
}
