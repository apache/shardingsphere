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
import org.apache.shardingsphere.infra.config.properties.ConfigurationProperties;
import org.apache.shardingsphere.infra.config.properties.ConfigurationPropertyKey;
import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.apache.shardingsphere.infra.merge.engine.ResultProcessEngine;
import org.apache.shardingsphere.infra.merge.engine.decorator.ResultDecorator;
import org.apache.shardingsphere.infra.merge.engine.decorator.impl.TransparentResultDecorator;
import org.apache.shardingsphere.infra.spi.ShardingSphereServiceLoader;
import org.apache.shardingsphere.infra.spi.ordered.OrderedSPIRegistry;
import org.apache.shardingsphere.sql.parser.binder.metadata.schema.SchemaMetaData;
import org.apache.shardingsphere.sql.parser.binder.statement.SQLStatementContext;
import org.apache.shardingsphere.sql.parser.binder.statement.dal.DescribeStatementContext;
import org.apache.shardingsphere.sql.parser.binder.statement.dml.InsertStatementContext;
import org.apache.shardingsphere.sql.parser.binder.statement.dml.SelectStatementContext;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dal.MySQLDescribeStatement;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Collections;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class EncryptResultDecoratorEngineTest {
    
    static {
        ShardingSphereServiceLoader.register(ResultProcessEngine.class);
    }
    
    @Mock
    private EncryptRule rule;
    
    @Mock
    private DatabaseType databaseType;
    
    @Mock
    private SchemaMetaData schemaMetaData;
    
    @Mock
    private ConfigurationProperties props;
    
    @Before
    public void setUp() {
        when(props.getValue(ConfigurationPropertyKey.QUERY_WITH_CIPHER_COLUMN)).thenReturn(true);
    }
    
    @Test
    public void assertNewInstanceWithSelectStatement() {
        EncryptResultDecoratorEngine engine = (EncryptResultDecoratorEngine) OrderedSPIRegistry.getRegisteredServices(Collections.singleton(rule), ResultProcessEngine.class).get(rule);
        ResultDecorator actual = engine.newInstance(databaseType, schemaMetaData, rule, props, mock(SelectStatementContext.class));
        assertThat(actual, instanceOf(EncryptDQLResultDecorator.class));
    }
    
    @Test
    public void assertNewInstanceWithDALStatement() {
        SQLStatementContext<MySQLDescribeStatement> sqlStatementContext = mock(DescribeStatementContext.class);
        when(sqlStatementContext.getSqlStatement()).thenReturn(mock(MySQLDescribeStatement.class));
        EncryptResultDecoratorEngine engine = (EncryptResultDecoratorEngine) OrderedSPIRegistry.getRegisteredServices(Collections.singleton(rule), ResultProcessEngine.class).get(rule);
        ResultDecorator actual = engine.newInstance(databaseType, schemaMetaData, rule, props, sqlStatementContext);
        assertThat(actual, instanceOf(EncryptDALResultDecorator.class));
    }
    
    @Test
    public void assertNewInstanceWithOtherStatement() {
        EncryptResultDecoratorEngine engine = (EncryptResultDecoratorEngine) OrderedSPIRegistry.getRegisteredServices(Collections.singleton(rule), ResultProcessEngine.class).get(rule);
        ResultDecorator actual = engine.newInstance(databaseType, schemaMetaData, rule, props, mock(InsertStatementContext.class));
        assertThat(actual, instanceOf(TransparentResultDecorator.class));
    }
}
