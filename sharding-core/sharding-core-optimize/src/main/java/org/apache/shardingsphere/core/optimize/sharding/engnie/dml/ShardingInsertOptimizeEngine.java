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

package org.apache.shardingsphere.core.optimize.sharding.engnie.dml;

import com.google.common.base.Optional;
import org.apache.shardingsphere.core.metadata.table.TableMetas;
import org.apache.shardingsphere.core.optimize.api.segment.InsertValue;
import org.apache.shardingsphere.core.optimize.sharding.engnie.ShardingOptimizeEngine;
import org.apache.shardingsphere.core.optimize.sharding.segment.insert.GeneratedKey;
import org.apache.shardingsphere.core.optimize.sharding.statement.dml.ShardingInsertOptimizedStatement;
import org.apache.shardingsphere.core.parse.sql.segment.dml.expr.ExpressionSegment;
import org.apache.shardingsphere.core.parse.sql.statement.dml.InsertStatement;
import org.apache.shardingsphere.core.rule.ShardingRule;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

/**
 * Insert optimize engine for sharding.
 *
 * @author zhangliang
 * @author maxiaoguang
 * @author panjuan
 */
public final class ShardingInsertOptimizeEngine implements ShardingOptimizeEngine<InsertStatement> {
    
    @Override
    public ShardingInsertOptimizedStatement optimize(final ShardingRule shardingRule, 
                                                     final TableMetas tableMetas, final String sql, final List<Object> parameters, final InsertStatement sqlStatement) {
        Optional<GeneratedKey> generatedKey = GeneratedKey.getGenerateKey(shardingRule, tableMetas, parameters, sqlStatement);
        List<String> columnNames = getColumnNames(tableMetas, sqlStatement, generatedKey.orNull());
        int derivedColumnsCount = getDerivedColumnsCount(shardingRule, sqlStatement.getTable().getTableName(), generatedKey.isPresent() && generatedKey.get().isGenerated());
        List<InsertValue> insertValues = getInsertValues(parameters, sqlStatement, derivedColumnsCount);
        return new ShardingInsertOptimizedStatement(sqlStatement, columnNames, generatedKey.orNull(), insertValues);
    }
    
    private List<String> getColumnNames(final TableMetas tableMetas, final InsertStatement sqlStatement, final GeneratedKey generatedKey) {
        List<String> result = sqlStatement.useDefaultColumns() ? tableMetas.getAllColumnNames(sqlStatement.getTable().getTableName()) : sqlStatement.getColumnNames();
        if (null != generatedKey && generatedKey.isGenerated()) {
            result.remove(generatedKey.getColumnName());
            result.add(generatedKey.getColumnName());
        }
        return result;
    }
    
    private int getDerivedColumnsCount(final ShardingRule shardingRule, final String tableName, final boolean isGeneratedValue) {
        int encryptDerivedColumnsCount = shardingRule.getEncryptRule().getAssistedQueryAndPlainColumns(tableName).size();
        return isGeneratedValue ? encryptDerivedColumnsCount + 1 : encryptDerivedColumnsCount;
    }
    
    private List<InsertValue> getInsertValues(final List<Object> parameters, final InsertStatement sqlStatement, final int derivedColumnsCount) {
        List<InsertValue> result = new LinkedList<>();
        int parametersOffset = 0;
        for (Collection<ExpressionSegment> each : sqlStatement.getAllValueExpressions()) {
            InsertValue insertValue = new InsertValue(each, derivedColumnsCount, parameters, parametersOffset);
            result.add(insertValue);
            parametersOffset += insertValue.getParametersCount();
        }
        return result;
    }
}
