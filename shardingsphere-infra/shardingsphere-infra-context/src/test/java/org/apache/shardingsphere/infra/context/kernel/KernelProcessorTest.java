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

package org.apache.shardingsphere.infra.context.kernel;

import org.apache.shardingsphere.infra.binder.QueryContext;
import org.apache.shardingsphere.infra.binder.statement.CommonSQLStatementContext;
import org.apache.shardingsphere.infra.binder.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.config.props.ConfigurationPropertyKey;
import org.apache.shardingsphere.infra.context.ConnectionContext;
import org.apache.shardingsphere.infra.database.DefaultDatabase;
import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.apache.shardingsphere.infra.executor.sql.context.ExecutionContext;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.resource.ShardingSphereResource;
import org.apache.shardingsphere.infra.metadata.database.rule.ShardingSphereRuleMetaData;
import org.apache.shardingsphere.sql.parser.sql.common.statement.SQLStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dml.SelectStatement;
import org.apache.shardingsphere.sqltranslator.rule.SQLTranslatorRule;
import org.junit.Test;

import java.util.Collections;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public final class KernelProcessorTest {
    
    @SuppressWarnings("unchecked")
    @Test
    public void assertGenerateExecutionContext() {
        SQLStatementContext<SQLStatement> sqlStatementContext = mock(CommonSQLStatementContext.class);
        when(sqlStatementContext.getSqlStatement()).thenReturn(mock(SelectStatement.class));
        QueryContext queryContext = new QueryContext(sqlStatementContext, "SELECT * FROM tbl", Collections.emptyList());
        ShardingSphereDatabase database = new ShardingSphereDatabase(DefaultDatabase.LOGIC_NAME, mock(DatabaseType.class),
                mock(ShardingSphereResource.class, RETURNS_DEEP_STUBS), new ShardingSphereRuleMetaData(Collections.singleton(mock(SQLTranslatorRule.class))), Collections.emptyMap());
        ConfigurationProperties props = new ConfigurationProperties(createProperties());
        ExecutionContext actual = new KernelProcessor().generateExecutionContext(queryContext, database, new ShardingSphereRuleMetaData(Collections.singleton(mock(SQLTranslatorRule.class))), props,
                mock(ConnectionContext.class));
        assertThat(actual.getExecutionUnits().size(), is(1));
    }
    
    private Properties createProperties() {
        Properties result = new Properties();
        result.setProperty(ConfigurationPropertyKey.SQL_SHOW.getKey(), Boolean.TRUE.toString());
        return result;
    }
}
