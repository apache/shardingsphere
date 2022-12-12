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
import org.apache.shardingsphere.infra.binder.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.binder.statement.dal.ExplainStatementContext;
import org.apache.shardingsphere.infra.binder.statement.dml.InsertStatementContext;
import org.apache.shardingsphere.infra.binder.statement.dml.SelectStatementContext;
import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.merge.engine.ResultProcessEngineFactory;
import org.apache.shardingsphere.infra.merge.engine.decorator.ResultDecorator;
import org.apache.shardingsphere.infra.merge.engine.decorator.impl.TransparentResultDecorator;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dal.ExplainStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dal.MySQLExplainStatement;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Collections;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class EncryptResultDecoratorEngineTest {
    
    @Mock
    private EncryptRule rule;
    
    @Mock
    private ShardingSphereDatabase database;
    
    @Test
    public void assertNewInstanceWithSelectStatement() {
        EncryptResultDecoratorEngine engine = (EncryptResultDecoratorEngine) ResultProcessEngineFactory.getInstances(Collections.singleton(rule)).get(rule);
        ResultDecorator<?> actual = engine.newInstance(database, rule, mock(ConfigurationProperties.class), mock(SelectStatementContext.class, RETURNS_DEEP_STUBS));
        assertThat(actual, instanceOf(EncryptDQLResultDecorator.class));
    }
    
    @Test
    public void assertNewInstanceWithDALStatement() {
        SQLStatementContext<ExplainStatement> sqlStatementContext = mock(ExplainStatementContext.class);
        when(sqlStatementContext.getSqlStatement()).thenReturn(mock(MySQLExplainStatement.class));
        EncryptResultDecoratorEngine engine = (EncryptResultDecoratorEngine) ResultProcessEngineFactory.getInstances(Collections.singleton(rule)).get(rule);
        ResultDecorator<?> actual = engine.newInstance(database, rule, mock(ConfigurationProperties.class), sqlStatementContext);
        assertThat(actual, instanceOf(EncryptDALResultDecorator.class));
    }
    
    @Test
    public void assertNewInstanceWithOtherStatement() {
        EncryptResultDecoratorEngine engine = (EncryptResultDecoratorEngine) ResultProcessEngineFactory.getInstances(Collections.singleton(rule)).get(rule);
        ResultDecorator<?> actual = engine.newInstance(database, rule, mock(ConfigurationProperties.class), mock(InsertStatementContext.class));
        assertThat(actual, instanceOf(TransparentResultDecorator.class));
    }
}
