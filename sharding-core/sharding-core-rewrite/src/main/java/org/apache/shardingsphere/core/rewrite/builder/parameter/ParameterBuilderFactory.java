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

package org.apache.shardingsphere.core.rewrite.builder.parameter;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.core.optimize.segment.insert.InsertValueContext;
import org.apache.shardingsphere.core.optimize.statement.impl.InsertSQLStatementContext;
import org.apache.shardingsphere.core.rewrite.builder.parameter.group.GroupedParameterBuilder;
import org.apache.shardingsphere.core.rewrite.builder.parameter.standard.StandardParameterBuilder;
import org.apache.shardingsphere.core.rewrite.statement.RewriteStatement;
import org.apache.shardingsphere.core.route.SQLRouteResult;

import java.util.LinkedList;
import java.util.List;

/**
 * Parameter builder factory.
 *
 * @author zhangliang
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ParameterBuilderFactory {
    
    /**
     * Create new instance of parameter builder.
     * 
     * @param rewriteStatement rewrite statement
     * @param parameters parameters
     * @param sqlRouteResult SQL route result
     * @return instance of parameter builder
     */
    public static ParameterBuilder newInstance(final RewriteStatement rewriteStatement, final List<Object> parameters, final SQLRouteResult sqlRouteResult) {
        return rewriteStatement.getSqlStatementContext() instanceof InsertSQLStatementContext
                ? new GroupedParameterBuilder(parameters, getGroupedParameters(rewriteStatement), rewriteStatement.getShardingConditions()) : new StandardParameterBuilder(parameters, sqlRouteResult);
    }
    
    /**
     * Create new instance of parameter builder.
     * 
     * @param rewriteStatement rewrite statement
     * @param parameters parameters
     * @return instance of parameter builder
     */
    public static ParameterBuilder newInstance(final RewriteStatement rewriteStatement, final List<Object> parameters) {
        return rewriteStatement.getSqlStatementContext() instanceof InsertSQLStatementContext
                ? new GroupedParameterBuilder(parameters, getGroupedParameters(rewriteStatement), null) : new StandardParameterBuilder(parameters);
    }
    
    private static List<List<Object>> getGroupedParameters(final RewriteStatement rewriteStatement) {
        List<List<Object>> result = new LinkedList<>();
        for (InsertValueContext each : ((InsertSQLStatementContext) rewriteStatement.getSqlStatementContext()).getInsertValueContexts()) {
            result.add(each.getParameters());
        }
        return result;
    }
}
