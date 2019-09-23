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

package org.apache.shardingsphere.core.optimize.api.statement;

import lombok.Getter;
import lombok.ToString;
import org.apache.shardingsphere.core.metadata.table.TableMetas;
import org.apache.shardingsphere.core.optimize.api.segment.InsertValue;
import org.apache.shardingsphere.core.optimize.api.segment.Tables;
import org.apache.shardingsphere.core.optimize.encrypt.statement.EncryptOptimizedStatement;
import org.apache.shardingsphere.core.optimize.sharding.statement.ShardingOptimizedStatement;
import org.apache.shardingsphere.core.parse.sql.segment.dml.expr.ExpressionSegment;
import org.apache.shardingsphere.core.parse.sql.statement.dml.InsertStatement;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

/**
 * Insert optimized statement.
 *
 * @author zhangliang
 */
@Getter
@ToString
public final class InsertOptimizedStatement implements ShardingOptimizedStatement, EncryptOptimizedStatement {
    
    private final InsertStatement sqlStatement;
    
    private final Tables tables;
    
    private final List<String> columnNames;
    
    private final List<InsertValue> insertValues;
    
    public InsertOptimizedStatement(final TableMetas tableMetas, final List<Object> parameters, final InsertStatement sqlStatement) {
        this.sqlStatement = sqlStatement;
        tables = new Tables(sqlStatement);
        columnNames = sqlStatement.useDefaultColumns() ? tableMetas.getAllColumnNames(tables.getSingleTableName()) : sqlStatement.getColumnNames();
        insertValues = getInsertValues(parameters, sqlStatement);
    }
    
    private List<InsertValue> getInsertValues(final List<Object> parameters, final InsertStatement sqlStatement) {
        List<InsertValue> result = new LinkedList<>();
        int parametersOffset = 0;
        for (Collection<ExpressionSegment> each : sqlStatement.getAllValueExpressions()) {
            InsertValue insertValue = new InsertValue(each, parameters, parametersOffset);
            result.add(insertValue);
            parametersOffset += insertValue.getParametersCount();
        }
        return result;
    }
}
