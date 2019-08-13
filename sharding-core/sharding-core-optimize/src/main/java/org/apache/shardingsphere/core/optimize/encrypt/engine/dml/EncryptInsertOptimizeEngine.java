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

import org.apache.shardingsphere.core.metadata.table.ShardingTableMetaData;
import org.apache.shardingsphere.core.optimize.api.segment.InsertValue;
import org.apache.shardingsphere.core.optimize.encrypt.engine.EncryptOptimizeEngine;
import org.apache.shardingsphere.core.optimize.encrypt.segment.EncryptInsertColumns;
import org.apache.shardingsphere.core.optimize.encrypt.statement.EncryptInsertOptimizedStatement;
import org.apache.shardingsphere.core.optimize.sharding.segment.insert.InsertOptimizeResultUnit;
import org.apache.shardingsphere.core.optimize.sharding.segment.insert.value.InsertValueEngine;
import org.apache.shardingsphere.core.parse.sql.statement.dml.InsertStatement;
import org.apache.shardingsphere.core.rule.EncryptRule;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * Insert optimize engine for encrypt.
 *
 * @author panjuan
 */
public final class EncryptInsertOptimizeEngine implements EncryptOptimizeEngine<InsertStatement> {
    
    @Override
    public EncryptInsertOptimizedStatement optimize(final EncryptRule encryptRule, 
                                                    final ShardingTableMetaData shardingTableMetaData, final String sql, final List<Object> parameters, final InsertStatement sqlStatement) {
        InsertValueEngine insertValueEngine = new InsertValueEngine();
        EncryptInsertOptimizedStatement result = new EncryptInsertOptimizedStatement(
                sqlStatement, new EncryptInsertColumns(encryptRule, shardingTableMetaData, sqlStatement), insertValueEngine.createInsertValues(sqlStatement));
        int derivedColumnsCount = encryptRule.getAssistedQueryAndPlainColumnCount(sqlStatement.getTable().getTableName());
        int parametersCount = 0;
        for (InsertValue each : result.getValues()) {
            Object[] currentParameters = each.getParameters(parameters, parametersCount, derivedColumnsCount);
            InsertOptimizeResultUnit unit = result.addUnit(each.getValues(derivedColumnsCount), currentParameters, each.getParametersCount());
            if (encryptRule.isHasQueryAssistedColumn(sqlStatement.getTable().getTableName())) {
                fillAssistedQueryUnit(encryptRule, Arrays.asList(currentParameters), sqlStatement.getTable().getTableName(), result.getInsertColumns().getRegularColumnNames(), unit);
            }
            if (encryptRule.isHasPlainColumn(sqlStatement.getTable().getTableName())) {
                fillPlainUnit(encryptRule, Arrays.asList(currentParameters), sqlStatement.getTable().getTableName(), result.getInsertColumns().getRegularColumnNames(), unit);
            }
            parametersCount += each.getParametersCount();
        }
        return result;
    }
    
    private void fillAssistedQueryUnit(final EncryptRule encryptRule, final List<Object> parameters, 
                                       final String tableName, final Collection<String> columnNames, final InsertOptimizeResultUnit unit) {
        for (String each : columnNames) {
            if (encryptRule.getAssistedQueryColumn(tableName, each).isPresent()) {
                unit.addInsertValue((Comparable<?>) unit.getColumnValue(each), parameters);
            }
        }
    }
    
    private void fillPlainUnit(final EncryptRule encryptRule, final List<Object> parameters,
                                       final String tableName, final Collection<String> columnNames, final InsertOptimizeResultUnit unit) {
        for (String each : columnNames) {
            if (encryptRule.getPlainColumn(tableName, each).isPresent()) {
                unit.addInsertValue((Comparable<?>) unit.getColumnValue(each), parameters);
            }
        }
    }
}
