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

import org.apache.shardingsphere.infra.binder.statement.dml.CopyStatementContext;
import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.sharding.exception.syntax.UnsupportedShardingOperationException;
import org.apache.shardingsphere.sharding.route.engine.validator.dml.impl.ShardingCopyStatementValidator;
import org.apache.shardingsphere.sharding.rule.ShardingRule;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.TableNameSegment;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dml.CopyStatement;
import org.apache.shardingsphere.sql.parser.sql.common.value.identifier.IdentifierValue;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.opengauss.dml.OpenGaussCopyStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.postgresql.dml.PostgreSQLCopyStatement;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ShardingCopyStatementValidatorTest {
    
    @Mock
    private ShardingRule shardingRule;
    
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ShardingSphereDatabase database;
    
    @Test
    void assertPreValidateWhenTableSegmentForPostgreSQL() {
        PostgreSQLCopyStatement sqlStatement = new PostgreSQLCopyStatement();
        sqlStatement.setTableSegment(new SimpleTableSegment(new TableNameSegment(0, 0, new IdentifierValue("t_order"))));
        assertDoesNotThrow(() -> new ShardingCopyStatementValidator().preValidate(
                shardingRule, new CopyStatementContext(sqlStatement), Collections.emptyList(), database, mock(ConfigurationProperties.class)));
    }
    
    @Test
    void assertPreValidateWhenTableSegmentForOpenGauss() {
        OpenGaussCopyStatement sqlStatement = new OpenGaussCopyStatement();
        sqlStatement.setTableSegment(new SimpleTableSegment(new TableNameSegment(0, 0, new IdentifierValue("t_order"))));
        assertDoesNotThrow(() -> new ShardingCopyStatementValidator().preValidate(
                shardingRule, new CopyStatementContext(sqlStatement), Collections.emptyList(), database, mock(ConfigurationProperties.class)));
    }
    
    @Test
    void assertPreValidateCopyWithShardingTableForPostgreSQL() {
        assertThrows(UnsupportedShardingOperationException.class, () -> assertPreValidateCopyTable(new PostgreSQLCopyStatement()));
    }
    
    @Test
    void assertPreValidateCopyWithShardingTableForOpenGauss() {
        assertThrows(UnsupportedShardingOperationException.class, () -> assertPreValidateCopyTable(new OpenGaussCopyStatement()));
    }
    
    private void assertPreValidateCopyTable(final CopyStatement sqlStatement) {
        sqlStatement.setTableSegment(new SimpleTableSegment(new TableNameSegment(0, 0, new IdentifierValue("t_order"))));
        CopyStatementContext sqlStatementContext = new CopyStatementContext(sqlStatement);
        String tableName = "t_order";
        when(shardingRule.isShardingTable(tableName)).thenReturn(true);
        new ShardingCopyStatementValidator().preValidate(
                shardingRule, sqlStatementContext, Collections.emptyList(), mock(ShardingSphereDatabase.class), mock(ConfigurationProperties.class));
    }
}
