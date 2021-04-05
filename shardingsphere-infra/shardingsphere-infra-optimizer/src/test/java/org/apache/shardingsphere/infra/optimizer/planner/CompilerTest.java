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

package org.apache.shardingsphere.infra.optimizer.planner;

import org.apache.shardingsphere.infra.database.type.DatabaseTypeRegistry;
import org.apache.shardingsphere.infra.database.type.dialect.MySQLDatabaseType;
import org.apache.shardingsphere.infra.metadata.schema.ShardingSphereSchema;
import org.apache.shardingsphere.infra.optimizer.ExecStmt;
import org.apache.shardingsphere.infra.optimizer.rel.physical.SSLimitSort;
import org.apache.shardingsphere.infra.optimizer.schema.AbstractSchemaTest;
import org.apache.shardingsphere.infra.parser.ShardingSphereSQLParserEngine;
import org.apache.shardingsphere.sql.parser.sql.common.statement.SQLStatement;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.instanceOf;

public class CompilerTest extends AbstractSchemaTest {
    
    private ShardingSphereSchema schema;
    
    private ShardingSphereSQLParserEngine sqlStatementParserEngine;
    
    @Before
    public void init() {
        schema = buildSchema();
        sqlStatementParserEngine = new ShardingSphereSQLParserEngine(DatabaseTypeRegistry.getTrunkDatabaseTypeName(
                new MySQLDatabaseType()));
    }
    
    @Test
    public void testUnSupportedJoinType() {
        String sql = "select * from t_order order by 1 asc";
        SQLStatement sqlStatement = sqlStatementParserEngine.parse(sql, false);
        ExecStmt execStmt = Compiler.compileQuery("logic_db", schema, sqlStatement);
        Assert.assertFalse(execStmt.isSuccess());
    }
    
    @Test
    public void testSelectWithAgg() {
        String sql = "select user_id, count(order_id) from t_order group by user_id order by user_id limit 10";
        SQLStatement sqlStatement = sqlStatementParserEngine.parse(sql, false);
        ExecStmt execStmt = Compiler.compileQuery("logic_db", schema, sqlStatement);
        Assert.assertTrue(execStmt.isSuccess());
        Assert.assertThat(execStmt.getPhysicalPlan(), instanceOf(SSLimitSort.class));
    }
}
