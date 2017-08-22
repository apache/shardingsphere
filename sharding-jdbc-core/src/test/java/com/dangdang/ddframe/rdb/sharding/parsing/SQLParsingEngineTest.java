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
import com.dangdang.ddframe.rdb.sharding.api.rule.TableRule;
import com.dangdang.ddframe.rdb.sharding.constant.DatabaseType;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.base.AbstractBaseParseSQLTest;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.base.AbstractBaseParseTest;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.jaxb.Assert;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.jaxb.helper.ParserJAXBHelper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

@RunWith(Parameterized.class)
public final class SQLParsingEngineTest extends AbstractBaseParseSQLTest {
    
    private final String[] parameters;
    
    public SQLParsingEngineTest(
            final String testCaseName, final DatabaseType databaseType, final Assert assertObj) {
        super(testCaseName, databaseType, assertObj);
        parameters = ParserJAXBHelper.getParameters(assertObj.getParameters());
    }
    
    @Parameters(name = "{0}In{1}")
    public static Collection<Object[]> dataParameters() {
        return AbstractBaseParseTest.dataParameters();
    }
    
    @Test
    public void assertStatement() {
        assertStatement(new SQLParsingEngine(getDatabaseType(), SqlPlaceholderUtil.replaceStatement(SQLStatementHelper.getSql(getTestCaseName()), parameters), buildShardingRule()).parse());
    }
    
    @Test
    public void assertPreparedStatement() {
        for (DatabaseType each : SQLStatementHelper.getTypes(getTestCaseName())) {
            assertPreparedStatement(new SQLParsingEngine(each, SqlPlaceholderUtil.replacePreparedStatement(SQLStatementHelper.getSql(getTestCaseName())), buildShardingRule()).parse());
        }
    }
    
    private ShardingRule buildShardingRule() {
        TableRule orderTableRule = TableRule.builder("t_order").actualTables(Collections.singletonList("t_order")).dataSourceNames(Arrays.asList("db0", "db1")).build();
        return new ShardingRuleMockBuilder().addTableRules(orderTableRule).addShardingColumns("user_id").addShardingColumns("order_id").addShardingColumns("item_id")
                .addGenerateKeyColumn("t_order_item", "item_id").build();
    }
}
