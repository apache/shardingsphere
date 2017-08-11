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

package com.dangdang.ddframe.rdb.sharding.parsing;

import com.dangdang.ddframe.rdb.common.jaxb.helper.SQLStatementHelper;
import com.dangdang.ddframe.rdb.common.util.SqlPlaceholderUtil;
import com.dangdang.ddframe.rdb.sharding.api.fixture.ShardingRuleMockBuilder;
import com.dangdang.ddframe.rdb.sharding.api.rule.ShardingRule;
import com.dangdang.ddframe.rdb.sharding.constant.DatabaseType;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.base.AbstractBaseParseSQLTest;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.base.AbstractBaseParseTest;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.context.table.Tables;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.jaxb.Conditions;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.jaxb.Limit;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.statement.SQLStatement;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import java.util.Collection;

@RunWith(Parameterized.class)
public final class SQLParsingEngineTest extends AbstractBaseParseSQLTest {
    
    public SQLParsingEngineTest(
            final String testCaseName, final String[] parameters, final Tables expectedTables, 
            final Conditions expectedConditions, final SQLStatement expectedSQLStatement, final Limit expectedLimit) {
        super(testCaseName, parameters, expectedTables, expectedConditions, expectedSQLStatement, expectedLimit);
    }
    
    @Parameters(name = "{0}")
    public static Collection<Object[]> dataParameters() {
        return AbstractBaseParseTest.dataParameters();
    }
    
    @Test
    public void assertStatement() {
        for (DatabaseType each : SQLStatementHelper.getTypes(getTestCaseName())) {
            assertStatement(new SQLParsingEngine(each, SqlPlaceholderUtil.replaceStatement(SQLStatementHelper.getSql(getTestCaseName()), getParameters()), buildShardingRule()).parse());
        }
    }
    
    @Test
    public void assertPreparedStatement() {
        for (DatabaseType each : SQLStatementHelper.getTypes(getTestCaseName())) {
            assertPreparedStatement(new SQLParsingEngine(each, SqlPlaceholderUtil.replacePreparedStatement(SQLStatementHelper.getSql(getTestCaseName())), buildShardingRule()).parse());
        }
    }
    
    private ShardingRule buildShardingRule() {
        return new ShardingRuleMockBuilder().addShardingColumns("user_id").addShardingColumns("order_id")
                .addGenerateKeyColumn("t_order", "order_id").build();
    }
}
