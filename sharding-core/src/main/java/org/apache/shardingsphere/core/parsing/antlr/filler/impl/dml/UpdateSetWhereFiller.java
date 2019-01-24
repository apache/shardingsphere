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

package org.apache.shardingsphere.core.parsing.antlr.filler.impl.dml;

import java.util.Map.Entry;

import org.apache.shardingsphere.core.metadata.table.ShardingTableMetaData;
import org.apache.shardingsphere.core.parsing.antlr.filler.impl.OrConditionFiller;
import org.apache.shardingsphere.core.parsing.antlr.sql.segment.FromWhereSegment;
import org.apache.shardingsphere.core.parsing.antlr.sql.segment.dml.UpdateSetWhereSegment;
import org.apache.shardingsphere.core.parsing.antlr.sql.segment.expr.ExpressionSegment;
import org.apache.shardingsphere.core.parsing.parser.sql.SQLStatement;
import org.apache.shardingsphere.core.parsing.parser.sql.dml.DMLStatement;
import org.apache.shardingsphere.core.rule.ShardingRule;

/**
 * Update set where filler.
 *
 * @author duhongjun
 */
public final class UpdateSetWhereFiller extends DeleteFromWhereFiller {
    
    @Override
    public void fill(final FromWhereSegment sqlSegment, final SQLStatement sqlStatement, final String sql, final ShardingRule shardingRule, final ShardingTableMetaData shardingTableMetaData) {
        super.fill(sqlSegment, sqlStatement, sql, shardingRule, shardingTableMetaData);
        UpdateSetWhereSegment updateSetWhereSegment = (UpdateSetWhereSegment) sqlSegment;
        DMLStatement dmlStatement = (DMLStatement) sqlStatement;
        for (Entry<String, ExpressionSegment> each : updateSetWhereSegment.getUpdateColumns().entrySet()) {
            dmlStatement.getUpdateColumns().put(each.getKey(), new OrConditionFiller().buildExpression(each.getValue(), sql).get());
        }
        dmlStatement.setDeleteStatement(false);
    }
}

