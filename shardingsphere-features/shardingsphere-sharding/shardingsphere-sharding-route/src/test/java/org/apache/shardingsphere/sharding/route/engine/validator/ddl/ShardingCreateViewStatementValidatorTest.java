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

package org.apache.shardingsphere.sharding.route.engine.validator.ddl;

import org.apache.shardingsphere.infra.binder.statement.CommonSQLStatementContext;
import org.apache.shardingsphere.infra.binder.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.exception.ShardingSphereException;
import org.apache.shardingsphere.infra.metadata.schema.ShardingSphereSchema;
import org.apache.shardingsphere.sharding.route.engine.validator.ddl.impl.ShardingCreateViewStatementValidator;
import org.apache.shardingsphere.sharding.rule.ShardingRule;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.sql.common.statement.ddl.CreateViewStatement;
import org.apache.shardingsphere.sql.parser.sql.common.value.identifier.IdentifierValue;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.ddl.MySQLCreateViewStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dml.MySQLSelectStatement;
import org.junit.Test;
import org.mockito.Mock;

import java.util.Collections;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public final class ShardingCreateViewStatementValidatorTest {
    
    @Mock
    private ShardingRule shardingRule;
    
    @Test
    public void assertValidateCreateViewForMySQL() {
        MySQLSelectStatement selectStatement = new MySQLSelectStatement();
        selectStatement.setFrom(new SimpleTableSegment(0, 0, new IdentifierValue("t_order_item")));
        MySQLCreateViewStatement sqlStatement = new MySQLCreateViewStatement();
        sqlStatement.setSelect(selectStatement);
        SQLStatementContext<CreateViewStatement> sqlStatementContext = new CommonSQLStatementContext<>(sqlStatement);
        new ShardingCreateViewStatementValidator().preValidate(shardingRule, sqlStatementContext, Collections.emptyList(), mock(ShardingSphereSchema.class));
    }
    
    @Test(expected = ShardingSphereException.class)
    public void assertValidateCreateViewWithShardingTableForMySQL() {
        MySQLSelectStatement selectStatement = new MySQLSelectStatement();
        selectStatement.setFrom(new SimpleTableSegment(0, 0, new IdentifierValue("t_order")));
        MySQLCreateViewStatement sqlStatement = new MySQLCreateViewStatement();
        sqlStatement.setSelect(selectStatement);
        ShardingSphereSchema schema = mock(ShardingSphereSchema.class);
        when(schema.getAllTableNames()).thenReturn(Collections.singleton("t_order"));
        SQLStatementContext<CreateViewStatement> sqlStatementContext = new CommonSQLStatementContext<>(sqlStatement);
        new ShardingCreateViewStatementValidator().preValidate(shardingRule, sqlStatementContext, Collections.emptyList(), schema);
    }
}
