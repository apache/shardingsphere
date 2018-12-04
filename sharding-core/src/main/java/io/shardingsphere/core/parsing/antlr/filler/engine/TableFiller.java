/*
 * Copyright 2016-2018 shardingsphere.io.
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

package io.shardingsphere.core.parsing.antlr.filler.engine;

import io.shardingsphere.core.metadata.table.ShardingTableMetaData;
import io.shardingsphere.core.parsing.antlr.filler.SQLSegmentFiller;
import io.shardingsphere.core.parsing.antlr.sql.segment.SQLSegment;
import io.shardingsphere.core.parsing.antlr.sql.segment.table.TableSegment;
import io.shardingsphere.core.parsing.parser.context.table.Table;
import io.shardingsphere.core.parsing.parser.sql.SQLStatement;
import io.shardingsphere.core.parsing.parser.sql.dql.select.SelectStatement;
import io.shardingsphere.core.parsing.parser.token.TableToken;
import io.shardingsphere.core.rule.ShardingRule;

/**
 * Table filler.
 *
 * @author duhongjun
 */
public class TableFiller implements SQLSegmentFiller {
    
    @Override
    public void fill(final SQLSegment sqlSegment, final SQLStatement sqlStatement, final ShardingRule shardingRule, final ShardingTableMetaData shardingTableMetaData) {
        TableSegment tableSegment = (TableSegment) sqlSegment;
        String tableName = tableSegment.getName();
        boolean needAdd = false;
        if (!(sqlStatement instanceof SelectStatement)) {
            needAdd = true;
        } else if (shardingRule.tryFindTableRuleByLogicTable(tableName).isPresent() || shardingRule.isBroadcastTable(tableName) || shardingRule.findBindingTableRule(tableName).isPresent()
                || shardingRule.getShardingDataSourceNames().getDataSourceNames().contains(shardingRule.getShardingDataSourceNames().getDefaultDataSourceName())) {
            needAdd = true;
        }
        if (needAdd) {
            sqlStatement.getTables().add(new Table(tableSegment.getName(), tableSegment.getAlias()));
            sqlStatement.getSQLTokens().add(tableSegment.getToken());
            if (tableSegment.getAlias().isPresent()) {
                if (sqlStatement.getTables().getTableNames().contains(tableSegment.getAlias().get())) {
                    sqlStatement.addSQLToken(new TableToken(tableSegment.getAliasStartPosition(), 0, tableSegment.getAlias().get()));
                }
            }
        }
    }
}
