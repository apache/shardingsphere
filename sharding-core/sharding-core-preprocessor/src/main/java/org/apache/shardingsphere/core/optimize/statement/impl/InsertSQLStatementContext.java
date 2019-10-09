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

package org.apache.shardingsphere.core.optimize.statement.impl;

import lombok.Getter;
import lombok.ToString;
import org.apache.shardingsphere.core.metadata.table.TableMetas;
import org.apache.shardingsphere.core.optimize.segment.insert.InsertValueContext;
import org.apache.shardingsphere.core.parse.sql.segment.dml.expr.ExpressionSegment;
import org.apache.shardingsphere.core.parse.sql.statement.dml.InsertStatement;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

/**
 * Insert SQL statement context.
 *
 * @author zhangliang
 */
@Getter
@ToString(callSuper = true)
public final class InsertSQLStatementContext extends CommonSQLStatementContext {
    
    private final List<String> columnNames;
    
    private final List<InsertValueContext> insertValueContexts;
    
    public InsertSQLStatementContext(final TableMetas tableMetas, final List<Object> parameters, final InsertStatement sqlStatement) {
        super(sqlStatement);
        columnNames = sqlStatement.useDefaultColumns() ? tableMetas.getAllColumnNames(getTablesContext().getSingleTableName()) : sqlStatement.getColumnNames();
        insertValueContexts = getInsertValueContexts(parameters);
    }
    
    private List<InsertValueContext> getInsertValueContexts(final List<Object> parameters) {
        List<InsertValueContext> result = new LinkedList<>();
        int parametersOffset = 0;
        for (Collection<ExpressionSegment> each : ((InsertStatement) getSqlStatement()).getAllValueExpressions()) {
            InsertValueContext insertValueContext = new InsertValueContext(each, parameters, parametersOffset);
            result.add(insertValueContext);
            parametersOffset += insertValueContext.getParametersCount();
        }
        return result;
    }
    
    /**
     * Get grouped parameters.
     * 
     * @return grouped parameters
     */
    public List<List<Object>> getGroupedParameters() {
        List<List<Object>> result = new LinkedList<>();
        for (InsertValueContext each : insertValueContexts) {
            result.add(each.getParameters());
        }
        return result;
    }
}
