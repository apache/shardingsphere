/*
 * Copyright 1999-2015 dangdang.com.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package io.shardingjdbc.core.routing.router;

import com.google.common.base.Optional;
import io.shardingjdbc.core.api.config.ShardingRuleConfiguration;
import io.shardingjdbc.core.api.config.TableRuleConfiguration;
import io.shardingjdbc.core.constant.DatabaseType;
import io.shardingjdbc.core.parsing.parser.context.table.Table;
import io.shardingjdbc.core.parsing.parser.sql.dml.insert.InsertStatement;
import io.shardingjdbc.core.parsing.parser.sql.dql.DQLStatement;
import io.shardingjdbc.core.parsing.parser.sql.dql.select.SelectStatement;
import io.shardingjdbc.core.rule.ShardingRule;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertThat;

public class ParsingSQLRouterTest {

    private ParsingSQLRouter parsingSQLRouter;

    @Before
    public void setRouterContext() {
        ShardingRuleConfiguration shardingRuleConfig = new ShardingRuleConfiguration();
        TableRuleConfiguration tableRuleConfig = new TableRuleConfiguration();
        tableRuleConfig.setLogicTable("LOGIC_TABLE");
        tableRuleConfig.setActualDataNodes("ds${0..1}.table_${0..2}");
        shardingRuleConfig.getTableRuleConfigs().add(tableRuleConfig);
        ShardingRule shardingRule = new ShardingRule(shardingRuleConfig, Arrays.asList("ds0", "ds1"));
        parsingSQLRouter=new ParsingSQLRouter(shardingRule, DatabaseType.MySQL,true);
    }

    @Test
    public void assertParse() {
        assertThat(parsingSQLRouter.parse("select t from table t", false), instanceOf(SelectStatement.class));
    }

    @Test
    public void assertDQLStatementRoute() {
        parsingSQLRouter.route("select t from table t", Collections.emptyList(), new DQLStatement());
    }

    @Test
    public void assertInsertStatementRoute() {
        String routeSql = "INSERT INTO `table_0` (`id`, `user_id`, `status`) VALUES (?, ?, ?)";
        InsertStatement insertStatement = new InsertStatement();
        insertStatement.getTables().add(new Table("logic_table",Optional.of("t")));
        parsingSQLRouter.route(routeSql, Collections.emptyList(), insertStatement);
    }
}