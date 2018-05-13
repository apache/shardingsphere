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

package io.shardingjdbc.core.parsing;

import io.shardingjdbc.core.common.jaxb.helper.SQLStatementHelper;
import io.shardingjdbc.core.common.util.SQLPlaceholderUtil;
import io.shardingjdbc.core.api.config.TableRuleConfiguration;
import io.shardingjdbc.core.api.config.strategy.ComplexShardingStrategyConfiguration;
import io.shardingjdbc.core.api.fixture.ShardingRuleMockBuilder;
import io.shardingjdbc.core.rule.ShardingRule;
import io.shardingjdbc.core.api.algorithm.fixture.TestComplexKeysShardingAlgorithm;
import io.shardingjdbc.core.constant.DatabaseType;
import io.shardingjdbc.core.parsing.parser.base.AbstractBaseParseSQLTest;
import io.shardingjdbc.core.parsing.parser.base.AbstractBaseParseTest;
import io.shardingjdbc.core.parsing.parser.jaxb.Assert;
import io.shardingjdbc.core.parsing.parser.jaxb.helper.ParserJAXBHelper;
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
        TableRuleConfiguration orderTableRuleConfig = new TableRuleConfiguration();
        orderTableRuleConfig.setLogicTable("t_order");
        orderTableRuleConfig.setActualDataNodes("db0.t_order,db1.t_order");
        orderTableRuleConfig.setTableShardingStrategyConfig(new ComplexShardingStrategyConfiguration("user_id, order_id", TestComplexKeysShardingAlgorithm.class.getName()));
        TableRuleConfiguration orderItemTableRuleConfig = new TableRuleConfiguration();
        orderItemTableRuleConfig.setLogicTable("t_order_item");
        orderItemTableRuleConfig.setActualDataNodes("db0.t_order_item,db1.t_order_item");
        orderItemTableRuleConfig.setTableShardingStrategyConfig(new ComplexShardingStrategyConfiguration("user_id, order_id, item_id", TestComplexKeysShardingAlgorithm.class.getName()));
        return new ShardingRuleMockBuilder().addTableRuleConfig(orderTableRuleConfig).addTableRuleConfig(orderItemTableRuleConfig)
                .addShardingColumns("user_id").addShardingColumns("order_id").addShardingColumns("item_id").addGenerateKeyColumn("t_order_item", "item_id").build();
    }
}
