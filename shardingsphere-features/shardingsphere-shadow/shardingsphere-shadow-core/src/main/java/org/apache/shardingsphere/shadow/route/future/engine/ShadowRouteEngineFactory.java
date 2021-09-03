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

package org.apache.shardingsphere.shadow.route.future.engine;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.infra.binder.LogicSQL;
import org.apache.shardingsphere.shadow.route.future.engine.dml.ShadowDeleteStatementRoutingEngine;
import org.apache.shardingsphere.shadow.route.future.engine.dml.ShadowInsertStatementRoutingEngine;
import org.apache.shardingsphere.shadow.route.future.engine.dml.ShadowSelectStatementRoutingEngine;
import org.apache.shardingsphere.shadow.route.future.engine.dml.ShadowUpdateStatementRoutingEngine;
import org.apache.shardingsphere.shadow.route.future.engine.impl.ShadowNonMDLStatementRoutingEngine;
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
     * @param logicSQL logic SQL
     * @return new instance of shadow route engine
     */
    public static ShadowRouteEngine newInstance(final LogicSQL logicSQL) {
        SQLStatement sqlStatement = logicSQL.getSqlStatementContext().getSqlStatement();
        if (sqlStatement instanceof InsertStatement) {
            return createShadowInsertStatementRoutingEngine();
        } else if (sqlStatement instanceof DeleteStatement) {
            return createShadowDeleteStatementRoutingEngine();
        } else if (sqlStatement instanceof UpdateStatement) {
            return createShadowUpdateStatementRoutingEngine();
        } else if (sqlStatement instanceof SelectStatement) {
            return createShadowSelectStatementRoutingEngine();
        } else {
            return createShadowNonMDLStatementRoutingEngine();
        }
    }
    
    private static ShadowRouteEngine createShadowNonMDLStatementRoutingEngine() {
        return new ShadowNonMDLStatementRoutingEngine();
    }
    
    private static ShadowRouteEngine createShadowSelectStatementRoutingEngine() {
        return new ShadowSelectStatementRoutingEngine();
    }
    
    private static ShadowRouteEngine createShadowUpdateStatementRoutingEngine() {
        return new ShadowUpdateStatementRoutingEngine();
    }
    
    private static ShadowRouteEngine createShadowDeleteStatementRoutingEngine() {
        return new ShadowDeleteStatementRoutingEngine();
    }
    
    private static ShadowRouteEngine createShadowInsertStatementRoutingEngine() {
        return new ShadowInsertStatementRoutingEngine();
    }
}
