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

package org.apache.shardingsphere.proxy.backend.hbase.converter;

import org.apache.hadoop.hbase.client.Put;
import org.apache.shardingsphere.infra.binder.SQLStatementContextFactory;
import org.apache.shardingsphere.infra.binder.statement.SQLStatementContext;
import org.apache.shardingsphere.proxy.backend.hbase.bean.HBaseOperation;
import org.apache.shardingsphere.proxy.backend.hbase.result.HBaseSupportedSQLStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.SQLStatement;
import org.junit.Test;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public final class HBaseDatabaseUpdateConverterTest {
    
    @Test
    public void assertConvert() {
        SQLStatement sqlStatement = HBaseSupportedSQLStatement.parseSQLStatement(HBaseSupportedSQLStatement.getUpdateStatement());
        SQLStatementContext<?> sqlStatementContext = SQLStatementContextFactory.newInstance(null, sqlStatement, "");
        HBaseDatabaseConverter converter = HBaseDatabaseConverterFactory.newInstance(sqlStatementContext);
        HBaseOperation hbaseOperation = converter.convert();
        assertThat(hbaseOperation.getTableName(), equalTo(HBaseSupportedSQLStatement.HBASE_DATABASE_TABLE_NAME));
        assertThat(hbaseOperation.getOperation(), instanceOf(Put.class));
    }
    
    @Test
    public void assertConvertWithIn() {
        String sql = " update /*+ hbase */ t_test_order set age = 10 where rowKey in (1, '2')";
        SQLStatement sqlStatement = HBaseSupportedSQLStatement.parseSQLStatement(sql);
        SQLStatementContext<?> sqlStatementContext = SQLStatementContextFactory.newInstance(null, sqlStatement, "");
        HBaseDatabaseConverter converter = HBaseDatabaseConverterFactory.newInstance(sqlStatementContext);
        HBaseOperation hBaseOperation = converter.convert();
        assertThat(hBaseOperation.getTableName(), equalTo(HBaseSupportedSQLStatement.HBASE_DATABASE_TABLE_NAME));
        assertThat(hBaseOperation.getOperation(), instanceOf(HBaseUpdateOperationAdapter.class));
        assertThat(((HBaseUpdateOperationAdapter) hBaseOperation.getOperation()).getPuts().size(), is(2));
    }
}
