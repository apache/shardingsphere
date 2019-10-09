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

package org.apache.shardingsphere.core.rewrite.sharding.parameter.impl;

import lombok.Setter;
import org.apache.shardingsphere.core.optimize.statement.SQLStatementContext;
import org.apache.shardingsphere.core.optimize.statement.impl.InsertSQLStatementContext;
import org.apache.shardingsphere.core.rewrite.parameter.ParameterBuilder;
import org.apache.shardingsphere.core.rewrite.parameter.impl.GroupedParameterBuilder;
import org.apache.shardingsphere.core.rewrite.parameter.ParameterRewriter;
import org.apache.shardingsphere.core.rewrite.sql.token.generator.SQLRouteResultAware;
import org.apache.shardingsphere.core.route.SQLRouteResult;

import java.util.Iterator;
import java.util.List;

/**
 * Generated key insert value parameter rewriter.
 *
 * @author zhangliang
 */
@Setter
public final class GeneratedKeyInsertValueParameterRewriter implements ParameterRewriter, SQLRouteResultAware {
    
    private SQLRouteResult sqlRouteResult;
    
    @Override
    public void rewrite(final SQLStatementContext sqlStatementContext, final List<Object> parameters, final ParameterBuilder parameterBuilder) {
        if (sqlStatementContext instanceof InsertSQLStatementContext && sqlRouteResult.getGeneratedKey().isPresent() && sqlRouteResult.getGeneratedKey().get().isGenerated()) {
            Iterator<Comparable<?>> generatedValues = sqlRouteResult.getGeneratedKey().get().getGeneratedValues().descendingIterator();
            int count = 0;
            for (List<Object> each : ((GroupedParameterBuilder) parameterBuilder).getParameterGroups()) {
                Comparable<?> generatedValue = generatedValues.next();
                if (!each.isEmpty()) {
                    ((GroupedParameterBuilder) parameterBuilder).getAddedIndexAndParameterGroups().get(count).put(each.size(), generatedValue);
                }
                count++;
            }
        }
    }
}
