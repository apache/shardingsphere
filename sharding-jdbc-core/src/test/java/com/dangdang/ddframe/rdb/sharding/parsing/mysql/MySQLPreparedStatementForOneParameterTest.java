/*
 * Copyright 1999-2015 dangdang.com.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package com.dangdang.ddframe.rdb.sharding.parsing.mysql;

import com.dangdang.ddframe.rdb.sharding.api.fixture.ShardingRuleMockBuilder;
import com.dangdang.ddframe.rdb.sharding.constant.DatabaseType;
import com.dangdang.ddframe.rdb.sharding.parsing.AbstractBaseParseTest;
import com.dangdang.ddframe.rdb.sharding.parsing.SQLParsingEngine;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.context.ConditionContext;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.context.SQLContext;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.context.TableContext;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import java.util.Collection;

@RunWith(Parameterized.class)
public final class MySQLPreparedStatementForOneParameterTest extends AbstractBaseParseTest {
    
    public MySQLPreparedStatementForOneParameterTest(final String testCaseName, final String sql, final String expectedSQL, final Collection<TableContext> expectedTables, 
            final Collection<ConditionContext> expectedConditionContext, final SQLContext expectedSQLContext) {
        super(testCaseName, sql, expectedSQL, expectedTables, expectedConditionContext, expectedSQLContext);
    }
    
    @Parameters(name = "{0}")
    public static Collection<Object[]> dataParameters() {
        return AbstractBaseParseTest.dataParameters("com/dangdang/ddframe/rdb/sharding/parsing/mysql/prepared_statement/one_param/");
    }
    
    @Test
    public void assertParse() {
        new SQLParsingEngine(DatabaseType.MySQL, getSql(), new ShardingRuleMockBuilder().addShardingColumns("user_id").addShardingColumns("order_id").addShardingColumns("state")
                .addgenerateKeyColumn("order", "order_id").addgenerateKeyColumn("payment", "order_id").addgenerateKeyColumn("payment", "pay_no").build()).parse();
    }
}
