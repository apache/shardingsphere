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

package org.apache.shardingsphere.core.parse.antlr.filler.encrypt.segment.impl.insert;

import com.google.common.base.Optional;
import org.apache.shardingsphere.core.metadata.table.ShardingTableMetaData;
import org.apache.shardingsphere.core.parse.antlr.filler.encrypt.SQLSegmentEncryptFiller;
import org.apache.shardingsphere.core.parse.antlr.sql.segment.dml.InsertValuesSegment;
import org.apache.shardingsphere.core.parse.antlr.sql.segment.dml.expr.CommonExpressionSegment;
import org.apache.shardingsphere.core.parse.parser.context.insertvalue.InsertValue;
import org.apache.shardingsphere.core.parse.parser.expression.SQLExpression;
import org.apache.shardingsphere.core.parse.parser.sql.SQLStatement;
import org.apache.shardingsphere.core.parse.parser.sql.dml.insert.InsertStatement;
import org.apache.shardingsphere.core.rule.EncryptRule;

/**
 * Insert values filler.
 *
 * @author zhangliang
 */
public final class EncryptInsertValuesFiller implements SQLSegmentEncryptFiller<InsertValuesSegment> {
    
    @Override
    public void fill(final InsertValuesSegment sqlSegment, final SQLStatement sqlStatement, final String sql, final EncryptRule encryptRule, final ShardingTableMetaData shardingTableMetaData) {
        InsertStatement insertStatement = (InsertStatement) sqlStatement;
        insertStatement.getInsertValues().getValues().add(getInsertValue(sqlSegment, sql));
        insertStatement.setParametersIndex(insertStatement.getParametersIndex() + sqlSegment.getParametersCount());
    }
    
    private InsertValue getInsertValue(final InsertValuesSegment sqlSegment, final String sql) {
        InsertValue result = new InsertValue(sqlSegment.getType(), sqlSegment.getParametersCount());
        for (CommonExpressionSegment each : sqlSegment.getValues()) {
            Optional<SQLExpression> sqlExpression = each.convertToSQLExpression(sql);
            if (sqlExpression.isPresent()) {
                result.getColumnValues().add(sqlExpression.get());
            }
        }
        return result;
    }
}
