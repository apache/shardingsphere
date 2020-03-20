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

package org.apache.shardingsphere.underlying.route;

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.sql.parser.SQLParserEngine;
import org.apache.shardingsphere.sql.parser.binder.SQLStatementContextFactory;
import org.apache.shardingsphere.sql.parser.binder.statement.CommonSQLStatementContext;
import org.apache.shardingsphere.sql.parser.binder.statement.SQLStatementContext;
import org.apache.shardingsphere.sql.parser.sql.statement.SQLStatement;
import org.apache.shardingsphere.underlying.common.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.underlying.route.context.RouteContext;
import org.apache.shardingsphere.underlying.route.context.RouteResult;

import java.util.List;

/**
 * Data node router.
 */
@RequiredArgsConstructor
public final class DateNodeRouter {
    
    private final ShardingSphereMetaData metaData;
    
    private final SQLParserEngine parserEngine;
    
    /**
     * Route SQL.
     *
     * @param sql SQL
     * @param parameters SQL parameters
     * @param useCache whether cache SQL parse result
     * @return route context
     */
    public RouteContext route(final String sql, final List<Object> parameters, final boolean useCache) {
        SQLStatement sqlStatement = parserEngine.parse(sql, useCache);
        try {
            SQLStatementContext sqlStatementContext = SQLStatementContextFactory.newInstance(metaData.getSchema(), sql, parameters, sqlStatement);
            return new RouteContext(sqlStatementContext, parameters, new RouteResult());
            // TODO should pass parameters for master-slave
        } catch (final IndexOutOfBoundsException ex) {
            return new RouteContext(new CommonSQLStatementContext(sqlStatement), parameters, new RouteResult());
        }
    }
}
