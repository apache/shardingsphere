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

package io.shardingsphere.core.parsing.antlr.filler.impl;

import io.shardingsphere.core.metadata.table.ShardingTableMetaData;
import io.shardingsphere.core.parsing.antlr.filler.SQLStatementFiller;
import io.shardingsphere.core.parsing.antlr.sql.segment.table.TableSegment;
import io.shardingsphere.core.parsing.parser.context.table.Table;
import io.shardingsphere.core.parsing.parser.sql.SQLStatement;
import io.shardingsphere.core.parsing.parser.sql.dql.select.SelectStatement;
import io.shardingsphere.core.rule.ShardingRule;

/**
 * Table filler.
 *
 * @author duhongjun
 */
public final class TableFiller implements SQLStatementFiller<TableSegment> {
    
    @Override
    public void fill(final TableSegment sqlSegment, final SQLStatement sqlStatement, final String sql, final ShardingRule shardingRule, final ShardingTableMetaData shardingTableMetaData) {
        boolean fill = false;
        if (shardingRule.contains(sqlSegment.getName())) {
            fill = true;
        } else {
            if (!(sqlStatement instanceof SelectStatement) && sqlStatement.getTables().isEmpty()) {
                fill = true;
            }
        }
        if (fill) {
            sqlStatement.getTables().add(new Table(sqlSegment.getName(), sqlSegment.getAlias()));
            sqlStatement.getSQLTokens().add(sqlSegment.getToken());
        }
    }
}
