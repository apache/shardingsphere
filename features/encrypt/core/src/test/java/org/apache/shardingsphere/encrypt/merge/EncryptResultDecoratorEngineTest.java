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

package org.apache.shardingsphere.encrypt.merge;

import org.apache.shardingsphere.encrypt.merge.dal.EncryptDALResultDecorator;
import org.apache.shardingsphere.encrypt.merge.dql.EncryptDQLResultDecorator;
import org.apache.shardingsphere.encrypt.rule.EncryptRule;
import org.apache.shardingsphere.infra.binder.context.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.binder.context.statement.type.dal.ExplainStatementContext;
import org.apache.shardingsphere.infra.binder.context.statement.type.dml.InsertStatementContext;
import org.apache.shardingsphere.infra.binder.context.statement.type.dml.SelectStatementContext;
import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.merge.engine.ResultProcessEngine;
import org.apache.shardingsphere.infra.merge.engine.decorator.ResultDecorator;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.spi.type.ordered.OrderedSPILoader;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dal.ExplainStatement;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.isA;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EncryptResultDecoratorEngineTest {
    
    @Mock
    private EncryptRule rule;
    
    @Mock
    private ShardingSphereDatabase database;
    
    @Test
    void assertNewInstanceWithSelectStatement() {
        EncryptResultDecoratorEngine engine = (EncryptResultDecoratorEngine) OrderedSPILoader.getServices(ResultProcessEngine.class, Collections.singleton(rule)).get(rule);
        Optional<ResultDecorator> actual =
                engine.newInstance(mock(ShardingSphereMetaData.class), database, mock(ConfigurationProperties.class), mock(SelectStatementContext.class, RETURNS_DEEP_STUBS));
        assertTrue(actual.isPresent());
        assertThat(actual.get(), isA(EncryptDQLResultDecorator.class));
    }
    
    @Test
    void assertNewInstanceWithDALStatement() {
        SQLStatementContext sqlStatementContext = mock(ExplainStatementContext.class);
        when(sqlStatementContext.getSqlStatement()).thenReturn(mock(ExplainStatement.class));
        EncryptResultDecoratorEngine engine = (EncryptResultDecoratorEngine) OrderedSPILoader.getServices(ResultProcessEngine.class, Collections.singleton(rule)).get(rule);
        Optional<ResultDecorator> actual = engine.newInstance(mock(ShardingSphereMetaData.class), database, mock(ConfigurationProperties.class), sqlStatementContext);
        assertTrue(actual.isPresent());
        assertThat(actual.get(), isA(EncryptDALResultDecorator.class));
    }
    
    @Test
    void assertNewInstanceWithOtherStatement() {
        EncryptResultDecoratorEngine engine = (EncryptResultDecoratorEngine) OrderedSPILoader.getServices(ResultProcessEngine.class, Collections.singleton(rule)).get(rule);
        assertFalse(engine.newInstance(mock(ShardingSphereMetaData.class), database, mock(ConfigurationProperties.class), mock(InsertStatementContext.class)).isPresent());
    }
}
