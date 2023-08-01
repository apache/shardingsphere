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

import org.apache.shardingsphere.infra.binder.context.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.binder.context.statement.ddl.AlterViewStatementContext;
import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.sharding.exception.syntax.UnsupportedShardingOperationException;
import org.apache.shardingsphere.sharding.route.engine.validator.ddl.impl.ShardingAlterViewStatementValidator;
import org.apache.shardingsphere.sharding.rule.ShardingRule;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.TableNameSegment;
import org.apache.shardingsphere.sql.parser.sql.common.value.identifier.IdentifierValue;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.ddl.MySQLAlterViewStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dml.MySQLSelectStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.opengauss.ddl.OpenGaussAlterViewStatement;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ShardingAlterViewStatementValidatorTest {
    
    @Mock
    private ShardingRule shardingRule;
    
    @Test
    void assertPreValidateAlterViewForMySQL() {
        MySQLSelectStatement selectStatement = new MySQLSelectStatement();
        selectStatement.setFrom(new SimpleTableSegment(new TableNameSegment(0, 0, new IdentifierValue("t_order"))));
        MySQLAlterViewStatement sqlStatement = new MySQLAlterViewStatement();
        sqlStatement.setSelect(selectStatement);
        SQLStatementContext sqlStatementContext = new AlterViewStatementContext(sqlStatement);
        ShardingSphereDatabase database = mock(ShardingSphereDatabase.class);
        when(shardingRule.isShardingTable("t_order")).thenReturn(false);
        assertDoesNotThrow(() -> new ShardingAlterViewStatementValidator().preValidate(shardingRule, sqlStatementContext, Collections.emptyList(), database, mock(ConfigurationProperties.class)));
    }
    
    @Test
    void assertPreValidateAlterViewWithShardingTableForMySQL() {
        MySQLSelectStatement selectStatement = new MySQLSelectStatement();
        selectStatement.setFrom(new SimpleTableSegment(new TableNameSegment(0, 0, new IdentifierValue("t_order"))));
        MySQLAlterViewStatement sqlStatement = new MySQLAlterViewStatement();
        sqlStatement.setSelect(selectStatement);
        ShardingSphereDatabase database = mock(ShardingSphereDatabase.class);
        SQLStatementContext sqlStatementContext = new AlterViewStatementContext(sqlStatement);
        when(shardingRule.isShardingTable("t_order")).thenReturn(true);
        assertThrows(UnsupportedShardingOperationException.class,
                () -> new ShardingAlterViewStatementValidator().preValidate(shardingRule, sqlStatementContext, Collections.emptyList(), database, mock(ConfigurationProperties.class)));
    }
    
    @Test
    void assertPreValidateAlterRenamedView() {
        OpenGaussAlterViewStatement selectStatement = new OpenGaussAlterViewStatement();
        selectStatement.setView(new SimpleTableSegment(new TableNameSegment(0, 0, new IdentifierValue("t_order"))));
        selectStatement.setRenameView(new SimpleTableSegment(new TableNameSegment(0, 0, new IdentifierValue("t_order_new"))));
        SQLStatementContext sqlStatementContext = new AlterViewStatementContext(selectStatement);
        ShardingSphereDatabase database = mock(ShardingSphereDatabase.class);
        assertDoesNotThrow(() -> new ShardingAlterViewStatementValidator().preValidate(shardingRule, sqlStatementContext, Collections.emptyList(), database, mock(ConfigurationProperties.class)));
    }
}
