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

package org.apache.shardingsphere.core.parsing.antlr.filler.impl.dql;

import org.apache.shardingsphere.core.metadata.table.ShardingTableMetaData;
import org.apache.shardingsphere.core.parsing.antlr.filler.SQLStatementFiller;
import org.apache.shardingsphere.core.parsing.antlr.filler.impl.FromWhereFiller;
import org.apache.shardingsphere.core.parsing.antlr.sql.segment.expr.SubquerySegment;
import org.apache.shardingsphere.core.parsing.parser.sql.SQLStatement;
import org.apache.shardingsphere.core.parsing.parser.sql.dql.select.SelectStatement;
import org.apache.shardingsphere.core.rule.ShardingRule;

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
        selectStatement.getSubqueryStatements().add(subqueryStatement);
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
