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

import org.apache.shardingsphere.infra.binder.context.statement.dml.LoadXMLStatementContext;
import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.sharding.exception.syntax.UnsupportedShardingOperationException;
import org.apache.shardingsphere.sharding.route.engine.validator.dml.impl.ShardingLoadXMLStatementValidator;
import org.apache.shardingsphere.sharding.rule.ShardingRule;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.TableNameSegment;
import org.apache.shardingsphere.sql.parser.sql.common.value.identifier.IdentifierValue;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dml.MySQLLoadXMLStatement;
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
class ShardingLoadXMLStatementValidatorTest {
    
    @Mock
    private ShardingRule shardingRule;
    
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ShardingSphereDatabase database;
    
    @Test
    void assertPreValidateLoadXMLWithSingleTable() {
        MySQLLoadXMLStatement sqlStatement = new MySQLLoadXMLStatement(new SimpleTableSegment(new TableNameSegment(0, 0, new IdentifierValue("t_order"))));
        assertDoesNotThrow(() -> new ShardingLoadXMLStatementValidator().preValidate(
                shardingRule, new LoadXMLStatementContext(sqlStatement), Collections.emptyList(), database, mock(ConfigurationProperties.class)));
    }
    
    @Test
    void assertPreValidateLoadXMLWithShardingTable() {
        MySQLLoadXMLStatement sqlStatement = new MySQLLoadXMLStatement(new SimpleTableSegment(new TableNameSegment(0, 0, new IdentifierValue("t_order"))));
        when(shardingRule.isShardingTable("t_order")).thenReturn(true);
        assertThrows(UnsupportedShardingOperationException.class, () -> new ShardingLoadXMLStatementValidator().preValidate(shardingRule, new LoadXMLStatementContext(sqlStatement),
                Collections.emptyList(), mock(ShardingSphereDatabase.class), mock(ConfigurationProperties.class)));
    }
}
