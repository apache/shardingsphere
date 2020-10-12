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
import org.apache.shardingsphere.sharding.rule.ShardingRule;
import org.apache.shardingsphere.infra.binder.statement.dml.DeleteStatementContext;
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
import org.mockito.Mock;

import java.util.Collections;

import static org.mockito.Mockito.mock;

public final class ShardingDeleteStatementValidatorTest {
    
    @Mock
    private ShardingRule shardingRule;
    
    @Test(expected = ShardingSphereException.class)
    public void assertValidateMySQLDeleteModifyMultiTables() {
        assertValidateDeleteModifyMultiTables(new MySQLDeleteStatement());
    }
    
    @Test(expected = ShardingSphereException.class)
    public void assertValidateOracleDeleteModifyMultiTables() {
        assertValidateDeleteModifyMultiTables(new OracleDeleteStatement());
    }
    
    @Test(expected = ShardingSphereException.class)
    public void assertValidatePostgreSQLDeleteModifyMultiTables() {
        assertValidateDeleteModifyMultiTables(new PostgreSQLDeleteStatement());
    }
    
    @Test(expected = ShardingSphereException.class)
    public void assertValidateSQL92DeleteModifyMultiTables() {
        assertValidateDeleteModifyMultiTables(new SQL92DeleteStatement());
    }
    
    @Test(expected = ShardingSphereException.class)
    public void assertValidateSQLServerDeleteModifyMultiTables() {
        assertValidateDeleteModifyMultiTables(new SQLServerDeleteStatement());
    }
    
    private void assertValidateDeleteModifyMultiTables(final DeleteStatement sqlStatement) {
        DeleteMultiTableSegment tableSegment = new DeleteMultiTableSegment();
        tableSegment.getActualDeleteTables().add(new SimpleTableSegment(0, 0, new IdentifierValue("user")));
        tableSegment.getActualDeleteTables().add(new SimpleTableSegment(0, 0, new IdentifierValue("order")));
        sqlStatement.setTableSegment(tableSegment);
        new ShardingDeleteStatementValidator().preValidate(shardingRule, new DeleteStatementContext(sqlStatement), Collections.emptyList(), mock(ShardingSphereMetaData.class));
    }
}
