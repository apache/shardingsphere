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

package org.apache.shardingsphere.core.optimize.encrypt.engine.dml;

import org.apache.shardingsphere.core.metadata.table.TableMetas;
import org.apache.shardingsphere.core.optimize.api.segment.InsertValue;
import org.apache.shardingsphere.core.optimize.encrypt.engine.EncryptOptimizeEngine;
import org.apache.shardingsphere.core.optimize.encrypt.statement.EncryptInsertOptimizedStatement;
import org.apache.shardingsphere.core.parse.sql.segment.dml.expr.ExpressionSegment;
import org.apache.shardingsphere.core.parse.sql.statement.dml.InsertStatement;
import org.apache.shardingsphere.core.rule.EncryptRule;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

/**
 * Insert optimize engine for encrypt.
 *
 * @author panjuan
 */
public final class EncryptInsertOptimizeEngine implements EncryptOptimizeEngine<InsertStatement> {
    
    @Override
    public EncryptInsertOptimizedStatement optimize(final EncryptRule encryptRule, final TableMetas tableMetas, final String sql, final List<Object> parameters, final InsertStatement sqlStatement) {
        int derivedColumnsCount = getDerivedColumnsCount(encryptRule, sqlStatement.getTable().getTableName());
        return new EncryptInsertOptimizedStatement(sqlStatement, tableMetas, getInsertValues(parameters, sqlStatement, derivedColumnsCount));
    }
    
    private int getDerivedColumnsCount(final EncryptRule encryptRule, final String tableName) {
        return encryptRule.getAssistedQueryAndPlainColumns(tableName).size();
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
