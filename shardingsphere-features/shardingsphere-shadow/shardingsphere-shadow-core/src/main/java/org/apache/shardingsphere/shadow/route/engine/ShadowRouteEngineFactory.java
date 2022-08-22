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
import org.apache.shardingsphere.infra.binder.QueryContext;
import org.apache.shardingsphere.infra.binder.statement.dml.DeleteStatementContext;
import org.apache.shardingsphere.infra.binder.statement.dml.InsertStatementContext;
import org.apache.shardingsphere.infra.binder.statement.dml.SelectStatementContext;
import org.apache.shardingsphere.infra.binder.statement.dml.UpdateStatementContext;
import org.apache.shardingsphere.shadow.route.engine.dml.ShadowDeleteStatementRoutingEngine;
import org.apache.shardingsphere.shadow.route.engine.dml.ShadowInsertStatementRoutingEngine;
import org.apache.shardingsphere.shadow.route.engine.dml.ShadowSelectStatementRoutingEngine;
import org.apache.shardingsphere.shadow.route.engine.dml.ShadowUpdateStatementRoutingEngine;
import org.apache.shardingsphere.shadow.route.engine.impl.ShadowNonDMLStatementRoutingEngine;
import org.apache.shardingsphere.sql.parser.sql.common.statement.SQLStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dml.DeleteStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dml.InsertStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dml.SelectStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dml.UpdateStatement;

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
            return createShadowInsertStatementRoutingEngine(queryContext);
        } else if (sqlStatement instanceof DeleteStatement) {
            return createShadowDeleteStatementRoutingEngine(queryContext);
        } else if (sqlStatement instanceof UpdateStatement) {
            return createShadowUpdateStatementRoutingEngine(queryContext);
        } else if (sqlStatement instanceof SelectStatement) {
            return createShadowSelectStatementRoutingEngine(queryContext);
        } else {
            return createShadowNonMDLStatementRoutingEngine(queryContext);
        }
    }
    
    private static ShadowRouteEngine createShadowNonMDLStatementRoutingEngine(final QueryContext queryContext) {
        return new ShadowNonDMLStatementRoutingEngine(queryContext.getSqlStatementContext());
    }
    
    private static ShadowRouteEngine createShadowSelectStatementRoutingEngine(final QueryContext queryContext) {
        return new ShadowSelectStatementRoutingEngine((SelectStatementContext) queryContext.getSqlStatementContext(), queryContext.getParameters());
    }
    
    private static ShadowRouteEngine createShadowUpdateStatementRoutingEngine(final QueryContext queryContext) {
        return new ShadowUpdateStatementRoutingEngine((UpdateStatementContext) queryContext.getSqlStatementContext(), queryContext.getParameters());
    }
    
    private static ShadowRouteEngine createShadowDeleteStatementRoutingEngine(final QueryContext queryContext) {
        return new ShadowDeleteStatementRoutingEngine((DeleteStatementContext) queryContext.getSqlStatementContext(), queryContext.getParameters());
    }
    
    private static ShadowRouteEngine createShadowInsertStatementRoutingEngine(final QueryContext queryContext) {
        return new ShadowInsertStatementRoutingEngine((InsertStatementContext) queryContext.getSqlStatementContext());
    }
}
