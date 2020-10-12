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

package org.apache.shardingsphere.sharding.route.engine.validator.impl;

import org.apache.shardingsphere.infra.exception.ShardingSphereException;
import org.apache.shardingsphere.infra.metadata.model.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.route.context.RouteContext;
import org.apache.shardingsphere.sharding.route.engine.validator.ShardingStatementValidator;
import org.apache.shardingsphere.sharding.rule.ShardingRule;
import org.apache.shardingsphere.infra.binder.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.binder.type.TableAvailable;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dml.DeleteStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.handler.dml.DeleteStatementHandler;

import java.util.List;

/**
 * Sharding delete statement validator.
 */
public final class ShardingDeleteStatementValidator implements ShardingStatementValidator<DeleteStatement> {
    
    @Override
    public void preValidate(final ShardingRule shardingRule, final SQLStatementContext<DeleteStatement> sqlStatementContext, final List<Object> parameters, final ShardingSphereMetaData metaData) {
        if (1 != ((TableAvailable) sqlStatementContext).getAllTables().size()) {
            throw new ShardingSphereException("Cannot support Multiple-Table for '%s'.", sqlStatementContext.getSqlStatement());
        }
    }
    
    @Override
    public void postValidate(final DeleteStatement sqlStatement, final RouteContext routeContext) {
        if (DeleteStatementHandler.getLimitSegment(sqlStatement).isPresent() && routeContext.getRouteUnits().size() > 1) {
            throw new ShardingSphereException("DELETE ... LIMIT can not support sharding route to multiple data nodes.");
        }
    }
}
