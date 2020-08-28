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

package org.apache.shardingsphere.infra.rewrite.engine;

import org.apache.shardingsphere.infra.rewrite.context.SQLRewriteContext;
import org.apache.shardingsphere.infra.rewrite.engine.result.GenericSQLRewriteResult;
import org.apache.shardingsphere.infra.rewrite.engine.result.SQLRewriteUnit;
import org.apache.shardingsphere.infra.rewrite.sql.impl.DefaultSQLBuilder;
import org.apache.shardingsphere.infra.route.context.RouteMapper;
import org.apache.shardingsphere.infra.route.context.RouteUnit;
import org.apache.shardingsphere.sql.parser.binder.segment.table.TablesContext;
import org.apache.shardingsphere.sql.parser.binder.statement.SQLStatementContext;

import java.util.Collection;
import java.util.Collections;
import java.util.stream.Collectors;

/**
 * Generic SQL rewrite engine.
 */
public final class GenericSQLRewriteEngine {
    
    /**
     * Rewrite SQL and parameters.
     *
     * @param sqlRewriteContext SQL rewrite context
     * @return SQL rewrite result
     */
    public GenericSQLRewriteResult rewrite(final SQLRewriteContext sqlRewriteContext) {
        Collection<RouteMapper> tableMappers = getTableMappers(sqlRewriteContext.getSqlStatementContext());
        return new GenericSQLRewriteResult(new SQLRewriteUnit(new DefaultSQLBuilder(sqlRewriteContext).toSQL(), sqlRewriteContext.getParameterBuilder().getParameters()),
                new RouteUnit(null, tableMappers));
    }

    private Collection<RouteMapper> getTableMappers(final SQLStatementContext sqlStatementContext) {
        TablesContext tablesContext = null;
        if (null != sqlStatementContext) {
            tablesContext = sqlStatementContext.getTablesContext();
        }
        Collection<String> tableNames = null;
        if (null != tablesContext) {
            tableNames = tablesContext.getTableNames();
        }
        if (null != tableNames && !tableNames.isEmpty()) {
            return tableNames.stream().map(tableName -> new RouteMapper(tableName, tableName)).collect(Collectors.toList());
        }
        return Collections.emptyList();
    }

}
