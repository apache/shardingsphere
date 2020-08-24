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

import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.schema.RuleSchemaMetaData;
import org.apache.shardingsphere.infra.route.context.RouteContext;
import org.apache.shardingsphere.infra.route.context.RouteResult;
import org.apache.shardingsphere.sharding.route.engine.exception.TableExistsException;
import org.apache.shardingsphere.sharding.rule.ShardingRule;
import org.apache.shardingsphere.sql.parser.binder.statement.SQLStatementContext;
import org.apache.shardingsphere.sql.parser.binder.statement.ddl.CreateTableStatementContext;
import org.apache.shardingsphere.sql.parser.sql.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.sql.statement.ddl.CreateTableStatement;
import org.apache.shardingsphere.sql.parser.sql.value.identifier.IdentifierValue;
import org.junit.Test;
import org.mockito.Mock;

import java.util.Collections;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public final class ShardingCreateTableStatementValidatorTest {

    @Mock
    private ShardingRule shardingRule;

    @Test(expected = TableExistsException.class)
    public void assertValidateCreateTable() {
        CreateTableStatement sqlStatement = new CreateTableStatement(new SimpleTableSegment(1, 2, new IdentifierValue("t_order")));
        SQLStatementContext<CreateTableStatement> sqlStatementContext = new CreateTableStatementContext(sqlStatement);
        RouteContext routeContext = new RouteContext(sqlStatementContext, Collections.emptyList(), new RouteResult());
        ShardingSphereMetaData metaData = mock(ShardingSphereMetaData.class);
        RuleSchemaMetaData ruleSchemaMetaData = mock(RuleSchemaMetaData.class);
        when(ruleSchemaMetaData.getAllTableNames()).thenReturn(Collections.singleton("t_order"));
        when(metaData.getSchema()).thenReturn(ruleSchemaMetaData);
        new ShardingCreateTableStatementValidator().preValidate(shardingRule, routeContext, metaData);
    }
}
