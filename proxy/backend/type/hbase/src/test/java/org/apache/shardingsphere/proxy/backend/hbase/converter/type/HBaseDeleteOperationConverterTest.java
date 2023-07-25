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

package org.apache.shardingsphere.proxy.backend.hbase.converter.type;

import org.apache.hadoop.hbase.client.Delete;
import org.apache.shardingsphere.infra.binder.engine.SQLBindEngine;
import org.apache.shardingsphere.infra.binder.context.statement.SQLStatementContext;
import org.apache.shardingsphere.proxy.backend.hbase.bean.HBaseOperation;
import org.apache.shardingsphere.proxy.backend.hbase.converter.HBaseOperationConverter;
import org.apache.shardingsphere.proxy.backend.hbase.converter.HBaseOperationConverterFactory;
import org.apache.shardingsphere.proxy.backend.hbase.converter.operation.HBaseDeleteOperation;
import org.apache.shardingsphere.proxy.backend.hbase.result.HBaseSupportedSQLStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.SQLStatement;
import org.junit.jupiter.api.Test;

import java.util.Collections;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

class HBaseDeleteOperationConverterTest {
    
    @Test
    void assertConvert() {
        SQLStatement sqlStatement = HBaseSupportedSQLStatement.parseSQLStatement(HBaseSupportedSQLStatement.getDeleteStatement());
        SQLStatementContext sqlStatementContext = new SQLBindEngine(null, "").bind(sqlStatement, Collections.emptyList());
        HBaseOperationConverter converter = HBaseOperationConverterFactory.newInstance(sqlStatementContext);
        HBaseOperation hbaseOperation = converter.convert();
        assertThat(hbaseOperation.getTableName(), is(HBaseSupportedSQLStatement.HBASE_DATABASE_TABLE_NAME));
        assertThat(hbaseOperation.getOperation(), instanceOf(Delete.class));
    }
    
    @Test
    void assertConvertWithIn() {
        String sql = " delete /*+ hbase */ from t_test_order where rowKey in ('2', '1')";
        SQLStatement sqlStatement = HBaseSupportedSQLStatement.parseSQLStatement(sql);
        SQLStatementContext sqlStatementContext = new SQLBindEngine(null, "").bind(sqlStatement, Collections.emptyList());
        HBaseOperationConverter converter = HBaseOperationConverterFactory.newInstance(sqlStatementContext);
        HBaseOperation hBaseOperation = converter.convert();
        assertThat(hBaseOperation.getTableName(), is(HBaseSupportedSQLStatement.HBASE_DATABASE_TABLE_NAME));
        assertThat(hBaseOperation.getOperation(), instanceOf(HBaseDeleteOperation.class));
        assertThat(((HBaseDeleteOperation) hBaseOperation.getOperation()).getDeletes().size(), is(2));
    }
}
