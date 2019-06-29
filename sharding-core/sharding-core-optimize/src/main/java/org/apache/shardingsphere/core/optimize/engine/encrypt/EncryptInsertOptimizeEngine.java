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

package org.apache.shardingsphere.core.optimize.engine.encrypt;

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.core.optimize.condition.ShardingCondition;
import org.apache.shardingsphere.core.optimize.engine.OptimizeEngine;
import org.apache.shardingsphere.core.optimize.result.InsertClauseOptimizedStatement;
import org.apache.shardingsphere.core.optimize.result.insert.InsertOptimizeResult;
import org.apache.shardingsphere.core.optimize.result.insert.InsertOptimizeResultUnit;
import org.apache.shardingsphere.core.parse.sql.context.insertvalue.InsertValue;
import org.apache.shardingsphere.core.parse.sql.statement.dml.InsertStatement;
import org.apache.shardingsphere.core.rule.EncryptRule;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Insert optimize engine for encrypt.
 *
 * @author panjuan
 */
@RequiredArgsConstructor
public final class EncryptInsertOptimizeEngine implements OptimizeEngine {
    
    private final EncryptRule encryptRule;
    
    private final InsertStatement insertStatement;
    
    private final List<Object> parameters;
    
    @Override
    public InsertClauseOptimizedStatement optimize() {
        InsertOptimizeResult insertOptimizeResult = new InsertOptimizeResult(insertStatement.getColumnNames());
        appendAssistedQueryColumns(insertOptimizeResult);
        int derivedColumnsCount = getDerivedColumnsCount();
        int parametersCount = 0;
        for (InsertValue each : insertStatement.getValues()) {
            InsertOptimizeResultUnit unit = insertOptimizeResult.addUnit(
                    each.getValues(derivedColumnsCount), each.getParameters(parameters, parametersCount, derivedColumnsCount), each.getParametersCount());
            if (encryptRule.getEncryptorEngine().isHasShardingQueryAssistedEncryptor(insertStatement.getTables().getSingleTableName())) {
                fillAssistedQueryUnit(insertOptimizeResult.getColumnNames(), unit);
            }
            parametersCount += each.getParametersCount();
        }
        return new InsertClauseOptimizedStatement(insertStatement, Collections.<ShardingCondition>emptyList(), insertOptimizeResult);
    }
    
    private void appendAssistedQueryColumns(final InsertOptimizeResult insertOptimizeResult) {
        for (String each : encryptRule.getEncryptorEngine().getAssistedQueryColumns(insertStatement.getTables().getSingleTableName())) {
            insertOptimizeResult.getColumnNames().add(each);
        }
    }
    
    private int getDerivedColumnsCount() {
        return encryptRule.getEncryptorEngine().getAssistedQueryColumnCount(insertStatement.getTables().getSingleTableName());
    }
    
    private void fillAssistedQueryUnit(final Collection<String> columnNames, final InsertOptimizeResultUnit unit) {
        for (String each : columnNames) {
            if (encryptRule.getEncryptorEngine().getAssistedQueryColumn(insertStatement.getTables().getSingleTableName(), each).isPresent()) {
                unit.addInsertValue((Comparable<?>) unit.getColumnValue(each), parameters);
            }
        }
    }
}
