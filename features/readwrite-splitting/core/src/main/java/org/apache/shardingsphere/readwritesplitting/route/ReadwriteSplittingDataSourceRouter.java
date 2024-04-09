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

package org.apache.shardingsphere.readwritesplitting.route;

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.infra.binder.context.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.hint.HintValueContext;
import org.apache.shardingsphere.infra.session.connection.ConnectionContext;
import org.apache.shardingsphere.readwritesplitting.route.qualified.QualifiedReadwriteSplittingDataSourceRouter;
import org.apache.shardingsphere.readwritesplitting.route.qualified.type.QualifiedReadwriteSplittingPrimaryDataSourceRouter;
import org.apache.shardingsphere.readwritesplitting.route.qualified.type.QualifiedReadwriteSplittingTransactionalDataSourceRouter;
import org.apache.shardingsphere.readwritesplitting.route.standard.StandardReadwriteSplittingDataSourceRouter;
import org.apache.shardingsphere.readwritesplitting.rule.ReadwriteSplittingDataSourceRule;

import java.util.Arrays;
import java.util.Collection;

/**
 * Data source router for readwrite-splitting.
 */
@RequiredArgsConstructor
public final class ReadwriteSplittingDataSourceRouter {
    
    private final ReadwriteSplittingDataSourceRule rule;
    
    private final ConnectionContext connectionContext;
    
    /**
     * Route.
     *
     * @param sqlStatementContext SQL statement context
     * @param hintValueContext hint value context
     * @return data source name
     */
    public String route(final SQLStatementContext sqlStatementContext, final HintValueContext hintValueContext) {
        for (QualifiedReadwriteSplittingDataSourceRouter each : getQualifiedRouters(connectionContext)) {
            if (each.isQualified(sqlStatementContext, rule, hintValueContext)) {
                return each.route(rule);
            }
        }
        return new StandardReadwriteSplittingDataSourceRouter().route(rule);
    }
    
    private Collection<QualifiedReadwriteSplittingDataSourceRouter> getQualifiedRouters(final ConnectionContext connectionContext) {
        return Arrays.asList(new QualifiedReadwriteSplittingPrimaryDataSourceRouter(), new QualifiedReadwriteSplittingTransactionalDataSourceRouter(connectionContext));
    }
}
