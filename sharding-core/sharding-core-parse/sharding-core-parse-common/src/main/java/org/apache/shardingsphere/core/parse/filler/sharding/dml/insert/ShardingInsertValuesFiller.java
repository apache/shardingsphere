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

package org.apache.shardingsphere.core.parse.filler.sharding.dml.insert;

import com.google.common.base.Optional;
import lombok.Setter;
import org.apache.shardingsphere.core.metadata.table.ShardingTableMetaData;
import org.apache.shardingsphere.core.parse.aware.ShardingRuleAware;
import org.apache.shardingsphere.core.parse.aware.ShardingTableMetaDataAware;
import org.apache.shardingsphere.core.parse.filler.SQLSegmentFiller;
import org.apache.shardingsphere.core.parse.sql.context.condition.AndCondition;
import org.apache.shardingsphere.core.parse.sql.context.condition.Column;
import org.apache.shardingsphere.core.parse.sql.context.condition.Condition;
import org.apache.shardingsphere.core.parse.sql.context.insertvalue.InsertValue;
import org.apache.shardingsphere.core.parse.sql.segment.dml.InsertValuesSegment;
import org.apache.shardingsphere.core.parse.sql.segment.dml.expr.ExpressionSegment;
import org.apache.shardingsphere.core.parse.sql.segment.dml.expr.simple.SimpleExpressionSegment;
import org.apache.shardingsphere.core.parse.sql.statement.SQLStatement;
import org.apache.shardingsphere.core.parse.sql.statement.dml.InsertStatement;
import org.apache.shardingsphere.core.rule.ShardingRule;

import java.util.Collection;
import java.util.Iterator;

/**
 * Insert values filler for sharding.
 *
 * @author zhangliang
 * @author panjuan
 */
@Setter
public final class ShardingInsertValuesFiller implements SQLSegmentFiller<InsertValuesSegment>, ShardingRuleAware, ShardingTableMetaDataAware {
    
    private ShardingRule shardingRule;
    
    private ShardingTableMetaData shardingTableMetaData;
    
    @Override
    public void fill(final InsertValuesSegment sqlSegment, final SQLStatement sqlStatement) {
        InsertStatement insertStatement = (InsertStatement) sqlStatement;
        fillColumns(sqlSegment, insertStatement);
        fillValues(sqlSegment, insertStatement);
        fillCondition(sqlSegment, insertStatement);
    }
    
    private void fillColumns(final InsertValuesSegment sqlSegment, final InsertStatement insertStatement) {
        if (insertStatement.getColumnNames().isEmpty()) {
            fillColumnsFromMetaData(insertStatement);
        }
        reviseColumnNamesForGenerateKeyColumn(sqlSegment, insertStatement);
    }
    
    private void fillColumnsFromMetaData(final InsertStatement insertStatement) {
        Collection<String> assistedQueryColumns = shardingRule.getEncryptRule().getEncryptorEngine().getAssistedQueryColumns(insertStatement.getTables().getSingleTableName());
        for (String each : shardingTableMetaData.getAllColumnNames(insertStatement.getTables().getSingleTableName())) {
            if (!assistedQueryColumns.contains(each)) {
                insertStatement.getColumnNames().add(each);
            }
        }
    }
    
    private void reviseColumnNamesForGenerateKeyColumn(final InsertValuesSegment sqlSegment, final InsertStatement insertStatement) {
        Optional<String> generateKeyColumnName = shardingRule.findGenerateKeyColumnName(insertStatement.getTables().getSingleTableName());
        if (generateKeyColumnName.isPresent() && insertStatement.getColumnNames().size() != sqlSegment.getValues().size()) {
            insertStatement.getColumnNames().remove(generateKeyColumnName.get());
        }
    }
    
    private void fillValues(final InsertValuesSegment sqlSegment, final InsertStatement insertStatement) {
        insertStatement.getValues().add(new InsertValue(sqlSegment.getValues()));
    }
    
    private void fillCondition(final InsertValuesSegment segment, final InsertStatement insertStatement) {
        AndCondition andCondition = new AndCondition();
        Iterator<String> columnNames = insertStatement.getColumnNames().iterator();
        for (ExpressionSegment each : segment.getValues()) {
            if (each instanceof SimpleExpressionSegment) {
                fillShardingCondition(andCondition, insertStatement.getTables().getSingleTableName(), columnNames.next(), (SimpleExpressionSegment) each);
            }
        }
        insertStatement.getShardingConditions().getOrConditions().add(andCondition);
    }
    
    private void fillShardingCondition(final AndCondition andCondition, final String tableName, final String columnName, final SimpleExpressionSegment expressionSegment) {
        if (shardingRule.isShardingColumn(columnName, tableName)) {
            andCondition.getConditions().add(new Condition(new Column(columnName, tableName), null, expressionSegment));
        }
    }
}
