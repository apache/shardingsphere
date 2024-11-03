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

package org.apache.shardingsphere.shadow.route.engine;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.infra.binder.context.statement.dml.DeleteStatementContext;
import org.apache.shardingsphere.infra.binder.context.statement.dml.InsertStatementContext;
import org.apache.shardingsphere.infra.binder.context.statement.dml.SelectStatementContext;
import org.apache.shardingsphere.infra.binder.context.statement.dml.UpdateStatementContext;
import org.apache.shardingsphere.infra.session.query.QueryContext;
import org.apache.shardingsphere.shadow.route.engine.finder.ShadowDataSourceMappingsFinder;
import org.apache.shardingsphere.shadow.route.engine.finder.dml.ShadowDeleteStatementDataSourceMappingsFinder;
import org.apache.shardingsphere.shadow.route.engine.finder.dml.ShadowInsertStatementDataSourceMappingsFinder;
import org.apache.shardingsphere.shadow.route.engine.finder.dml.ShadowSelectStatementDataSourceMappingsFinder;
import org.apache.shardingsphere.shadow.route.engine.finder.dml.ShadowUpdateStatementDataSourceMappingsFinder;
import org.apache.shardingsphere.shadow.route.engine.finder.other.ShadowNonDMLStatementDataSourceMappingsFinder;
import org.apache.shardingsphere.sql.parser.statement.core.statement.SQLStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.dml.DeleteStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.dml.InsertStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.dml.SelectStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.dml.UpdateStatement;

/**
 * Shadow data source mappings finder.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ShadowDataSourceMappingsFinderFactory {
    
    /**
     * Create new instance of shadow data source mappings finder.
     *
     * @param queryContext query context
     * @return created instance
     */
    public static ShadowDataSourceMappingsFinder newInstance(final QueryContext queryContext) {
        SQLStatement sqlStatement = queryContext.getSqlStatementContext().getSqlStatement();
        if (sqlStatement instanceof InsertStatement) {
            return createShadowInsertStatementRouteEngine(queryContext);
        }
        if (sqlStatement instanceof DeleteStatement) {
            return createShadowDeleteStatementRouteEngine(queryContext);
        }
        if (sqlStatement instanceof UpdateStatement) {
            return createShadowUpdateStatementRouteEngine(queryContext);
        }
        if (sqlStatement instanceof SelectStatement) {
            return createShadowSelectStatementRouteEngine(queryContext);
        }
        return createShadowNonMDLStatementRouteEngine(queryContext);
    }
    
    private static ShadowDataSourceMappingsFinder createShadowNonMDLStatementRouteEngine(final QueryContext queryContext) {
        return new ShadowNonDMLStatementDataSourceMappingsFinder(queryContext.getHintValueContext());
    }
    
    private static ShadowDataSourceMappingsFinder createShadowSelectStatementRouteEngine(final QueryContext queryContext) {
        return new ShadowSelectStatementDataSourceMappingsFinder((SelectStatementContext) queryContext.getSqlStatementContext(), queryContext.getParameters(), queryContext.getHintValueContext());
    }
    
    private static ShadowDataSourceMappingsFinder createShadowUpdateStatementRouteEngine(final QueryContext queryContext) {
        return new ShadowUpdateStatementDataSourceMappingsFinder((UpdateStatementContext) queryContext.getSqlStatementContext(), queryContext.getParameters(), queryContext.getHintValueContext());
    }
    
    private static ShadowDataSourceMappingsFinder createShadowDeleteStatementRouteEngine(final QueryContext queryContext) {
        return new ShadowDeleteStatementDataSourceMappingsFinder((DeleteStatementContext) queryContext.getSqlStatementContext(), queryContext.getParameters(), queryContext.getHintValueContext());
    }
    
    private static ShadowDataSourceMappingsFinder createShadowInsertStatementRouteEngine(final QueryContext queryContext) {
        return new ShadowInsertStatementDataSourceMappingsFinder((InsertStatementContext) queryContext.getSqlStatementContext(), queryContext.getHintValueContext());
    }
}
