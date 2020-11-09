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

package org.apache.shardingsphere.sharding.route.engine.validator.dml;

import com.google.common.collect.Lists;
import org.apache.shardingsphere.infra.binder.statement.dml.DeleteStatementContext;
import org.apache.shardingsphere.infra.exception.ShardingSphereException;
import org.apache.shardingsphere.infra.metadata.schema.model.physical.PhysicalSchemaMetaData;
import org.apache.shardingsphere.sharding.route.engine.validator.dml.impl.ShardingDeleteStatementValidator;
import org.apache.shardingsphere.sharding.rule.ShardingRule;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.DeleteMultiTableSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dml.DeleteStatement;
import org.apache.shardingsphere.sql.parser.sql.common.value.identifier.IdentifierValue;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dml.MySQLDeleteStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.oracle.dml.OracleDeleteStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.postgresql.dml.PostgreSQLDeleteStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.sql92.dml.SQL92DeleteStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.sqlserver.dml.SQLServerDeleteStatement;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Collection;
import java.util.Collections;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class ShardingDeleteStatementValidatorTest {
    
    @Mock
    private ShardingRule shardingRule;
    
    @Test(expected = ShardingSphereException.class)
    public void assertValidateDeleteModifyMultiTablesForMySQL() {
        assertValidateDeleteModifyMultiTables(new MySQLDeleteStatement());
    }
    
    @Test(expected = ShardingSphereException.class)
    public void assertValidateDeleteModifyMultiTablesForOracle() {
        assertValidateDeleteModifyMultiTables(new OracleDeleteStatement());
    }
    
    @Test(expected = ShardingSphereException.class)
    public void assertValidateDeleteModifyMultiTablesForPostgreSQL() {
        assertValidateDeleteModifyMultiTables(new PostgreSQLDeleteStatement());
    }
    
    @Test(expected = ShardingSphereException.class)
    public void assertValidateDeleteModifyMultiTablesForSQL92() {
        assertValidateDeleteModifyMultiTables(new SQL92DeleteStatement());
    }
    
    @Test(expected = ShardingSphereException.class)
    public void assertValidateDeleteModifyMultiTablesForSQLServer() {
        assertValidateDeleteModifyMultiTables(new SQLServerDeleteStatement());
    }
    
    private void assertValidateDeleteModifyMultiTables(final DeleteStatement sqlStatement) {
        DeleteMultiTableSegment tableSegment = new DeleteMultiTableSegment();
        tableSegment.getActualDeleteTables().add(new SimpleTableSegment(0, 0, new IdentifierValue("user")));
        tableSegment.getActualDeleteTables().add(new SimpleTableSegment(0, 0, new IdentifierValue("order")));
        tableSegment.getActualDeleteTables().add(new SimpleTableSegment(0, 0, new IdentifierValue("order_item")));
        sqlStatement.setTableSegment(tableSegment);
        DeleteStatementContext sqlStatementContext = new DeleteStatementContext(sqlStatement);
        Collection<String> shardingTableNames = Lists.newArrayList("order", "order_item");
        when(shardingRule.getShardingLogicTableNames(sqlStatementContext.getTablesContext().getTableNames())).thenReturn(shardingTableNames);
        when(shardingRule.isAllBindingTables(shardingTableNames)).thenReturn(true);
        new ShardingDeleteStatementValidator().preValidate(shardingRule, sqlStatementContext, Collections.emptyList(), mock(PhysicalSchemaMetaData.class));
    }
}
