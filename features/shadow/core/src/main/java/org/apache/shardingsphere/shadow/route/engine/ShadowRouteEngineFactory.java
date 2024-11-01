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
import org.apache.shardingsphere.shadow.route.engine.dml.ShadowDeleteStatementRouteEngine;
import org.apache.shardingsphere.shadow.route.engine.dml.ShadowInsertStatementRouteEngine;
import org.apache.shardingsphere.shadow.route.engine.dml.ShadowSelectStatementRouteEngine;
import org.apache.shardingsphere.shadow.route.engine.dml.ShadowUpdateStatementRouteEngine;
import org.apache.shardingsphere.shadow.route.engine.impl.ShadowNonDMLStatementRouteEngine;
import org.apache.shardingsphere.sql.parser.statement.core.statement.SQLStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.dml.DeleteStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.dml.InsertStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.dml.SelectStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.dml.UpdateStatement;

/**
 * Shadow routing engine factory.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ShadowRouteEngineFactory {
    
    /**
     * Create new instance of shadow route engine.
     *
     * @param queryContext query context
     * @return created instance
     */
    public static ShadowRouteEngine newInstance(final QueryContext queryContext) {
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
    
    private static ShadowRouteEngine createShadowNonMDLStatementRouteEngine(final QueryContext queryContext) {
        return new ShadowNonDMLStatementRouteEngine(queryContext.getHintValueContext());
    }
    
    private static ShadowRouteEngine createShadowSelectStatementRouteEngine(final QueryContext queryContext) {
        return new ShadowSelectStatementRouteEngine((SelectStatementContext) queryContext.getSqlStatementContext(), queryContext.getParameters(), queryContext.getHintValueContext());
    }
    
    private static ShadowRouteEngine createShadowUpdateStatementRouteEngine(final QueryContext queryContext) {
        return new ShadowUpdateStatementRouteEngine((UpdateStatementContext) queryContext.getSqlStatementContext(), queryContext.getParameters(), queryContext.getHintValueContext());
    }
    
    private static ShadowRouteEngine createShadowDeleteStatementRouteEngine(final QueryContext queryContext) {
        return new ShadowDeleteStatementRouteEngine((DeleteStatementContext) queryContext.getSqlStatementContext(), queryContext.getParameters(), queryContext.getHintValueContext());
    }
    
    private static ShadowRouteEngine createShadowInsertStatementRouteEngine(final QueryContext queryContext) {
        return new ShadowInsertStatementRouteEngine((InsertStatementContext) queryContext.getSqlStatementContext(), queryContext.getHintValueContext());
    }
}
