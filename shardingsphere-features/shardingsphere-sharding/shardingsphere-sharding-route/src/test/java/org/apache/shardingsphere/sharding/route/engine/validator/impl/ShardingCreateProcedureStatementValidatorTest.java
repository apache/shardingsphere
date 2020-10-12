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
import org.apache.shardingsphere.infra.metadata.schema.RuleSchemaMetaData;
import org.apache.shardingsphere.sharding.route.engine.exception.NoSuchTableException;
import org.apache.shardingsphere.sharding.route.engine.exception.TableExistsException;
import org.apache.shardingsphere.sharding.rule.ShardingRule;
import org.apache.shardingsphere.infra.metadata.database.model.schema.SchemaMetaData;
import org.apache.shardingsphere.infra.binder.statement.CommonSQLStatementContext;
import org.apache.shardingsphere.infra.binder.statement.SQLStatementContext;
import org.apache.shardingsphere.sql.parser.sql.common.segment.ddl.routine.RoutineBodySegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.ddl.routine.ValidStatementSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.sql.common.statement.ddl.CreateProcedureStatement;
import org.apache.shardingsphere.sql.parser.sql.common.value.identifier.IdentifierValue;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.ddl.MySQLCreateProcedureStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.ddl.MySQLCreateTableStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dml.MySQLSelectStatement;
import org.junit.Test;
import org.mockito.Mock;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public final class ShardingCreateProcedureStatementValidatorTest {
    
    @Mock
    private ShardingRule shardingRule;
    
    @Test
    public void assertValidateCreateProcedureForMySQL() {
        MySQLSelectStatement selectStatement = new MySQLSelectStatement();
        selectStatement.setFrom(new SimpleTableSegment(0, 0, new IdentifierValue("t_order_item")));
        MySQLCreateTableStatement createTableStatement = new MySQLCreateTableStatement();
        createTableStatement.setTable(new SimpleTableSegment(0, 0, new IdentifierValue("t_order")));
        ValidStatementSegment createTableValidStatementSegment = new ValidStatementSegment(0, 0);
        createTableValidStatementSegment.setSqlStatement(createTableStatement);
        ValidStatementSegment selectValidStatementSegment = new ValidStatementSegment(0, 0);
        selectValidStatementSegment.setSqlStatement(selectStatement);
        RoutineBodySegment routineBody = new RoutineBodySegment(0, 0);
        routineBody.getValidStatements().add(createTableValidStatementSegment);
        routineBody.getValidStatements().add(selectValidStatementSegment);
        MySQLCreateProcedureStatement sqlStatement = new MySQLCreateProcedureStatement();
        sqlStatement.setRoutineBody(routineBody);
        ShardingSphereMetaData metaData = mock(ShardingSphereMetaData.class);
        RuleSchemaMetaData ruleSchemaMetaData = mock(RuleSchemaMetaData.class);
        SchemaMetaData schemaMetaData = mock(SchemaMetaData.class);
        when(metaData.getRuleSchemaMetaData()).thenReturn(ruleSchemaMetaData);
        when(ruleSchemaMetaData.getConfiguredSchemaMetaData()).thenReturn(schemaMetaData);
        when(schemaMetaData.getAllTableNames()).thenReturn(Collections.singletonList("t_order"));
        Map<String, Collection<String>> unconfiguredSchemaMetaDataMap = new HashMap<>(1, 1);
        unconfiguredSchemaMetaDataMap.put("ds_0", Collections.singleton("t_order_item"));
        when(ruleSchemaMetaData.getUnconfiguredSchemaMetaDataMap()).thenReturn(unconfiguredSchemaMetaDataMap);
        when(ruleSchemaMetaData.getAllTableNames()).thenReturn(Collections.emptyList());
        SQLStatementContext<CreateProcedureStatement> sqlStatementContext = new CommonSQLStatementContext<>(sqlStatement);
        new ShardingCreateProcedureStatementValidator().preValidate(shardingRule, sqlStatementContext, Collections.emptyList(), metaData);
    }
    
    @Test(expected = ShardingSphereException.class)
    public void assertValidateCreateProcedureWithShardingTableForMySQL() {
        MySQLSelectStatement selectStatement = new MySQLSelectStatement();
        selectStatement.setFrom(new SimpleTableSegment(0, 0, new IdentifierValue("t_order")));
        ValidStatementSegment validStatementSegment = new ValidStatementSegment(0, 0);
        validStatementSegment.setSqlStatement(selectStatement);
        RoutineBodySegment routineBody = new RoutineBodySegment(0, 0);
        routineBody.getValidStatements().add(validStatementSegment);
        MySQLCreateProcedureStatement sqlStatement = new MySQLCreateProcedureStatement();
        sqlStatement.setRoutineBody(routineBody);
        ShardingSphereMetaData metaData = mock(ShardingSphereMetaData.class);
        RuleSchemaMetaData ruleSchemaMetaData = mock(RuleSchemaMetaData.class);
        SchemaMetaData schemaMetaData = mock(SchemaMetaData.class);
        when(metaData.getRuleSchemaMetaData()).thenReturn(ruleSchemaMetaData);
        when(ruleSchemaMetaData.getConfiguredSchemaMetaData()).thenReturn(schemaMetaData);
        when(schemaMetaData.getAllTableNames()).thenReturn(Collections.singleton("t_order"));
        SQLStatementContext<CreateProcedureStatement> sqlStatementContext = new CommonSQLStatementContext<>(sqlStatement);
        new ShardingCreateProcedureStatementValidator().preValidate(shardingRule, sqlStatementContext, Collections.emptyList(), metaData);
    }
    
    @Test(expected = NoSuchTableException.class)
    public void assertValidateCreateProcedureWithNoSuchTableForMySQL() {
        MySQLSelectStatement selectStatement = new MySQLSelectStatement();
        selectStatement.setFrom(new SimpleTableSegment(0, 0, new IdentifierValue("t_order")));
        ValidStatementSegment validStatementSegment = new ValidStatementSegment(0, 0);
        validStatementSegment.setSqlStatement(selectStatement);
        RoutineBodySegment routineBody = new RoutineBodySegment(0, 0);
        routineBody.getValidStatements().add(validStatementSegment);
        MySQLCreateProcedureStatement sqlStatement = new MySQLCreateProcedureStatement();
        sqlStatement.setRoutineBody(routineBody);
        ShardingSphereMetaData metaData = mock(ShardingSphereMetaData.class);
        RuleSchemaMetaData ruleSchemaMetaData = mock(RuleSchemaMetaData.class);
        SchemaMetaData schemaMetaData = mock(SchemaMetaData.class);
        when(metaData.getRuleSchemaMetaData()).thenReturn(ruleSchemaMetaData);
        when(ruleSchemaMetaData.getConfiguredSchemaMetaData()).thenReturn(schemaMetaData);
        when(schemaMetaData.getAllTableNames()).thenReturn(Collections.emptyList());
        Map<String, Collection<String>> unconfiguredSchemaMetaDataMap = new HashMap<>(1, 1);
        unconfiguredSchemaMetaDataMap.put("ds_0", Collections.emptyList());
        when(ruleSchemaMetaData.getUnconfiguredSchemaMetaDataMap()).thenReturn(unconfiguredSchemaMetaDataMap);
        SQLStatementContext<CreateProcedureStatement> sqlStatementContext = new CommonSQLStatementContext<>(sqlStatement);
        new ShardingCreateProcedureStatementValidator().preValidate(shardingRule, sqlStatementContext, Collections.emptyList(), metaData);
    }
    
    @Test(expected = TableExistsException.class)
    public void assertValidateCreateProcedureWithTableExistsForMySQL() {
        MySQLCreateTableStatement createTableStatement = new MySQLCreateTableStatement();
        createTableStatement.setTable(new SimpleTableSegment(0, 0, new IdentifierValue("t_order")));
        ValidStatementSegment validStatementSegment = new ValidStatementSegment(0, 0);
        validStatementSegment.setSqlStatement(createTableStatement);
        RoutineBodySegment routineBody = new RoutineBodySegment(0, 0);
        routineBody.getValidStatements().add(validStatementSegment);
        MySQLCreateProcedureStatement sqlStatement = new MySQLCreateProcedureStatement();
        sqlStatement.setRoutineBody(routineBody);
        ShardingSphereMetaData metaData = mock(ShardingSphereMetaData.class);
        RuleSchemaMetaData ruleSchemaMetaData = mock(RuleSchemaMetaData.class);
        when(metaData.getRuleSchemaMetaData()).thenReturn(ruleSchemaMetaData);
        when(ruleSchemaMetaData.getAllTableNames()).thenReturn(Collections.singletonList("t_order"));
        SQLStatementContext<CreateProcedureStatement> sqlStatementContext = new CommonSQLStatementContext<>(sqlStatement);
        new ShardingCreateProcedureStatementValidator().preValidate(shardingRule, sqlStatementContext, Collections.emptyList(), metaData);
    }
}
