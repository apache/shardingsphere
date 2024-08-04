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

package org.apache.shardingsphere.infra.connection.kernel;

import org.apache.shardingsphere.infra.binder.context.statement.CommonSQLStatementContext;
import org.apache.shardingsphere.infra.binder.context.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.config.props.ConfigurationPropertyKey;
import org.apache.shardingsphere.infra.database.core.DefaultDatabase;
import org.apache.shardingsphere.infra.database.core.type.DatabaseType;
import org.apache.shardingsphere.infra.executor.sql.context.ExecutionContext;
import org.apache.shardingsphere.infra.hint.HintValueContext;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.resource.ResourceMetaData;
import org.apache.shardingsphere.infra.metadata.database.rule.RuleMetaData;
import org.apache.shardingsphere.infra.rule.ShardingSphereRule;
import org.apache.shardingsphere.infra.session.connection.ConnectionContext;
import org.apache.shardingsphere.infra.session.query.QueryContext;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.sql.parser.statement.core.statement.dml.SelectStatement;
import org.apache.shardingsphere.sqltranslator.context.SQLTranslatorContext;
import org.apache.shardingsphere.sqltranslator.rule.SQLTranslatorRule;
import org.apache.shardingsphere.test.util.PropertiesBuilder;
import org.apache.shardingsphere.test.util.PropertiesBuilder.Property;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class KernelProcessorTest {
    
    @Test
    void assertGenerateExecutionContext() {
        DatabaseType databaseType = TypedSPILoader.getService(DatabaseType.class, "FIXTURE");
        SQLStatementContext sqlStatementContext = mock(CommonSQLStatementContext.class);
        when(sqlStatementContext.getSqlStatement()).thenReturn(mock(SelectStatement.class));
        when(sqlStatementContext.getDatabaseType()).thenReturn(databaseType);
        ConnectionContext connectionContext = mock(ConnectionContext.class);
        when(connectionContext.getCurrentDatabaseName()).thenReturn(Optional.of(DefaultDatabase.LOGIC_NAME));
        ShardingSphereMetaData metaData = mock(ShardingSphereMetaData.class);
        ResourceMetaData resourceMetaData = mock(ResourceMetaData.class, RETURNS_DEEP_STUBS);
        when(resourceMetaData.getStorageUnits()).thenReturn(Collections.emptyMap());
        ShardingSphereDatabase database = new ShardingSphereDatabase(DefaultDatabase.LOGIC_NAME, databaseType, resourceMetaData, new RuleMetaData(mockShardingSphereRule()), Collections.emptyMap());
        when(metaData.containsDatabase(DefaultDatabase.LOGIC_NAME)).thenReturn(true);
        when(metaData.getDatabase(DefaultDatabase.LOGIC_NAME)).thenReturn(database);
        QueryContext queryContext = new QueryContext(sqlStatementContext, "SELECT * FROM tbl", Collections.emptyList(), new HintValueContext(), connectionContext, metaData);
        ConfigurationProperties props = new ConfigurationProperties(PropertiesBuilder.build(new Property(ConfigurationPropertyKey.SQL_SHOW.getKey(), Boolean.TRUE.toString())));
        ExecutionContext actual = new KernelProcessor().generateExecutionContext(queryContext, new RuleMetaData(mockShardingSphereRule()), props, connectionContext);
        assertThat(actual.getExecutionUnits().size(), is(1));
    }
    
    private Collection<ShardingSphereRule> mockShardingSphereRule() {
        Collection<ShardingSphereRule> result = new LinkedList<>();
        SQLTranslatorRule sqlTranslatorRule = mock(SQLTranslatorRule.class);
        when(sqlTranslatorRule.translate(any(), any(), any(), any(), any(), any())).thenReturn(new SQLTranslatorContext("", Collections.emptyList()));
        result.add(sqlTranslatorRule);
        return result;
    }
}
