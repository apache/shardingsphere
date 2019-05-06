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

package io.shardingsphere.core.parsing.antlr.filler.impl.dql;

import io.shardingsphere.core.metadata.table.ShardingTableMetaData;
import io.shardingsphere.core.parsing.antlr.filler.SQLStatementFiller;
import io.shardingsphere.core.parsing.antlr.filler.impl.FromWhereFiller;
import io.shardingsphere.core.parsing.antlr.sql.segment.expr.SubquerySegment;
import io.shardingsphere.core.parsing.parser.sql.SQLStatement;
import io.shardingsphere.core.parsing.parser.sql.dql.select.SelectStatement;
import io.shardingsphere.core.rule.ShardingRule;

/**
 * Subquery filler.
 *
 * @author duhongjun
 */
public final class SubqueryFiller implements SQLStatementFiller<SubquerySegment> {
    
    @Override
    public void fill(final SubquerySegment sqlSegment, final SQLStatement sqlStatement, final String sql, final ShardingRule shardingRule, final ShardingTableMetaData shardingTableMetaData) {
        SelectStatement selectStatement = (SelectStatement) sqlStatement;
        SelectStatement subqueryStatement = new SelectStatement();
        selectStatement.getSubQueryStatements().add(subqueryStatement);
        if (sqlSegment.getSelectClauseSegment().isPresent()) {
            new SelectClauseFiller().fill(sqlSegment.getSelectClauseSegment().get(), subqueryStatement, sql, shardingRule, shardingTableMetaData);
        }
        if (sqlSegment.getFromWhereSegment().isPresent()) {
            new FromWhereFiller().fill(sqlSegment.getFromWhereSegment().get(), subqueryStatement, sql, shardingRule, shardingTableMetaData);
        }
        if (!sqlSegment.isSubqueryInFrom()) {
            return;
        }
        if (sqlSegment.getGroupBySegment().isPresent()) {
            new GroupByFiller().fill(sqlSegment.getGroupBySegment().get(), subqueryStatement, sql, shardingRule, shardingTableMetaData);
        }
        if (sqlSegment.getOrderBySegment().isPresent()) {
            new OrderByFiller().fill(sqlSegment.getOrderBySegment().get(), subqueryStatement, sql, shardingRule, shardingTableMetaData);
        }
    }
}
