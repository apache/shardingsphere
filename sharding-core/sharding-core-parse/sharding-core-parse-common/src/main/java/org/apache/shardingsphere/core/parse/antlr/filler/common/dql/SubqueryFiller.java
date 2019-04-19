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

package org.apache.shardingsphere.core.parse.antlr.filler.common.dql;

import org.apache.shardingsphere.core.parse.antlr.filler.api.SQLSegmentFiller;
import org.apache.shardingsphere.core.parse.antlr.sql.segment.dml.expr.SubquerySegment;
import org.apache.shardingsphere.core.parse.antlr.sql.statement.SQLStatement;
import org.apache.shardingsphere.core.parse.antlr.sql.statement.dml.SelectStatement;

/**
 * Subquery filler.
 *
 * @author duhongjun
 */
public final class SubqueryFiller implements SQLSegmentFiller<SubquerySegment> {
    
    @Override
    public void fill(final SubquerySegment sqlSegment, final SQLStatement sqlStatement) {
        SelectStatement selectStatement = (SelectStatement) sqlStatement;
        SelectStatement subqueryStatement = new SelectStatement();
        subqueryStatement.setParentStatement(selectStatement);
        selectStatement.getSubqueryStatements().add(subqueryStatement);
        if (sqlSegment.getSelectItemsSegment().isPresent()) {
            new SelectItemsFiller().fill(sqlSegment.getSelectItemsSegment().get(), subqueryStatement);
        }
        if (!sqlSegment.isSubqueryInFrom()) {
            return;
        }
        if (sqlSegment.getGroupBySegment().isPresent()) {
            new GroupByFiller().fill(sqlSegment.getGroupBySegment().get(), subqueryStatement);
        }
        if (sqlSegment.getOrderBySegment().isPresent()) {
            new OrderByFiller().fill(sqlSegment.getOrderBySegment().get(), subqueryStatement);
        }
    }
}
