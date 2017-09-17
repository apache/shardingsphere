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
import com.dangdang.ddframe.rdb.common.util.SQLPlaceholderUtil;
import com.dangdang.ddframe.rdb.sharding.api.config.TableRuleConfig;
import com.dangdang.ddframe.rdb.sharding.api.config.strategy.ComplexShardingStrategyConfig;
import com.dangdang.ddframe.rdb.sharding.api.fixture.ShardingRuleMockBuilder;
import com.dangdang.ddframe.rdb.sharding.rule.ShardingRule;
import com.dangdang.ddframe.rdb.sharding.api.strategy.fixture.TestComplexKeysShardingAlgorithm;
import com.dangdang.ddframe.rdb.sharding.constant.DatabaseType;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.base.AbstractBaseParseSQLTest;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.base.AbstractBaseParseTest;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.jaxb.Assert;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.jaxb.helper.ParserJAXBHelper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import java.sql.SQLException;
import java.util.Collection;

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
    public void assertStatement() throws SQLException {
        assertStatement(new SQLParsingEngine(getDatabaseType(), SQLPlaceholderUtil.replaceStatement(SQLStatementHelper.getSql(getTestCaseName()), parameters), buildShardingRule()).parse());
    }
    
    @Test
    public void assertPreparedStatement() throws SQLException {
        for (DatabaseType each : SQLStatementHelper.getTypes(getTestCaseName())) {
            assertPreparedStatement(new SQLParsingEngine(each, SQLPlaceholderUtil.replacePreparedStatement(SQLStatementHelper.getSql(getTestCaseName())), buildShardingRule()).parse());
        }
    }
    
    private ShardingRule buildShardingRule() throws SQLException {
        TableRuleConfig orderTableRuleConfig = new TableRuleConfig();
        orderTableRuleConfig.setLogicTable("t_order");
        orderTableRuleConfig.setActualTables("t_order");
        orderTableRuleConfig.setDataSourceNames("db0, db1");
        ComplexShardingStrategyConfig orderShardingStrategyConfig = new ComplexShardingStrategyConfig();
        orderShardingStrategyConfig.setShardingColumns("user_id, order_id");
        orderShardingStrategyConfig.setAlgorithmClassName(TestComplexKeysShardingAlgorithm.class.getName());
        orderTableRuleConfig.setTableShardingStrategyConfig(orderShardingStrategyConfig);
        TableRuleConfig orderItemTableRuleConfig = new TableRuleConfig();
        orderItemTableRuleConfig.setLogicTable("t_order_item");
        orderItemTableRuleConfig.setActualTables("t_order_item");
        orderItemTableRuleConfig.setDataSourceNames("db0, db1");
        ComplexShardingStrategyConfig orderItemShardingStrategyConfig = new ComplexShardingStrategyConfig();
        orderItemShardingStrategyConfig.setShardingColumns("user_id, order_id, item_id");
        orderItemShardingStrategyConfig.setAlgorithmClassName(TestComplexKeysShardingAlgorithm.class.getName());
        orderItemTableRuleConfig.setTableShardingStrategyConfig(orderItemShardingStrategyConfig);
        return new ShardingRuleMockBuilder().addTableRuleConfig(orderTableRuleConfig).addTableRuleConfig(orderItemTableRuleConfig)
                .addShardingColumns("user_id").addShardingColumns("order_id").addShardingColumns("item_id").addGenerateKeyColumn("t_order_item", "item_id").build();
    }
}
