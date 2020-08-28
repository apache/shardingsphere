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
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.route.context.OriginRouteStageContext;
import org.apache.shardingsphere.infra.route.context.RouteContext;
import org.apache.shardingsphere.infra.route.context.RouteResult;
import org.apache.shardingsphere.sharding.rule.ShardingRule;
import org.apache.shardingsphere.sql.parser.binder.segment.table.TablesContext;
import org.apache.shardingsphere.sql.parser.binder.statement.SQLStatementContext;
import org.apache.shardingsphere.sql.parser.binder.statement.dml.DeleteStatementContext;
import org.apache.shardingsphere.sql.parser.sql.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.sql.statement.dml.DeleteStatement;
import org.apache.shardingsphere.sql.parser.sql.value.identifier.IdentifierValue;
import org.junit.Test;
import org.mockito.Mock;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import static org.mockito.Mockito.mock;

public final class ShardingDeleteStatementValidatorTest {

    @Mock
    private ShardingRule shardingRule;

    @Test(expected = ShardingSphereException.class)
    public void assertValidateDeleteModifyMultiTables() {
        DeleteStatement sqlStatement = new DeleteStatement();
        sqlStatement.getTables().addAll(createMultiTablesContext().getTables());
        SQLStatementContext<DeleteStatement> sqlStatementContext = new DeleteStatementContext(sqlStatement);
        RouteContext routeContext = new RouteContext(new OriginRouteStageContext("sharding_db", sqlStatementContext, Collections.emptyList(), new RouteResult()));
        new ShardingDeleteStatementValidator().preValidate(shardingRule, routeContext, mock(ShardingSphereMetaData.class));
    }

    private TablesContext createMultiTablesContext() {
        List<SimpleTableSegment> result = new LinkedList<>();
        result.add(new SimpleTableSegment(0, 0, new IdentifierValue("user")));
        result.add(new SimpleTableSegment(0, 0, new IdentifierValue("order")));
        return new TablesContext(result);
    }
}
