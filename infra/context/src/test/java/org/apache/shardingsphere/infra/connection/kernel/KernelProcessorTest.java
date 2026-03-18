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

import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.infra.binder.context.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.binder.context.statement.type.CommonSQLStatementContext;
import org.apache.shardingsphere.infra.checker.SupportedSQLCheckEngine;
import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.config.props.ConfigurationPropertyKey;
import org.apache.shardingsphere.infra.executor.sql.context.ExecutionContext;
import org.apache.shardingsphere.infra.executor.sql.log.SQLLogger;
import org.apache.shardingsphere.infra.hint.HintValueContext;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.resource.ResourceMetaData;
import org.apache.shardingsphere.infra.metadata.database.resource.unit.StorageUnit;
import org.apache.shardingsphere.infra.metadata.database.rule.RuleMetaData;
import org.apache.shardingsphere.infra.rule.ShardingSphereRule;
import org.apache.shardingsphere.infra.rule.attribute.RuleAttributes;
import org.apache.shardingsphere.infra.rule.attribute.datasource.aggregate.AggregatedDataSourceRuleAttribute;
import org.apache.shardingsphere.infra.session.connection.ConnectionContext;
import org.apache.shardingsphere.infra.session.query.QueryContext;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.infra.util.props.PropertiesBuilder;
import org.apache.shardingsphere.infra.util.props.PropertiesBuilder.Property;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.SelectStatement;
import org.apache.shardingsphere.sqltranslator.context.SQLTranslatorContext;
import org.apache.shardingsphere.sqltranslator.rule.SQLTranslatorRule;
import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;

import javax.sql.DataSource;
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;
import java.util.Properties;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class KernelProcessorTest {
    
    private final DatabaseType databaseType = TypedSPILoader.getService(DatabaseType.class, "FIXTURE");
    
    @Test
    void assertGenerateExecutionContextWithMetadataValidationAndSQLLogging() {
        QueryContext queryContext = createQueryContext(false);
        ConfigurationProperties props = createProps(true);
        try (
                MockedConstruction<SupportedSQLCheckEngine> mockedCheckEngines = mockConstruction(SupportedSQLCheckEngine.class);
                MockedStatic<SQLLogger> mockedSQLLogger = mockStatic(SQLLogger.class)) {
            ExecutionContext actual = new KernelProcessor().generateExecutionContext(queryContext, new RuleMetaData(Arrays.asList(mockSQLTranslatorRule(), mockAggregatedDataSourceRule())), props);
            assertThat(actual.getExecutionUnits().size(), is(1));
            assertThat(mockedCheckEngines.constructed().size(), is(1));
            verify(mockedCheckEngines.constructed().iterator().next()).checkSQL(any(), any(), any());
            mockedSQLLogger.verify(() -> SQLLogger.logSQL(queryContext, false, actual));
        }
    }
    
    @Test
    void assertGenerateExecutionContextWithoutMetadataValidationAndSQLLogging() {
        QueryContext queryContext = createQueryContext(true);
        ConfigurationProperties props = createProps(false);
        try (
                MockedConstruction<SupportedSQLCheckEngine> mockedCheckEngines = mockConstruction(SupportedSQLCheckEngine.class);
                MockedStatic<SQLLogger> mockedSQLLogger = mockStatic(SQLLogger.class)) {
            ExecutionContext actual = new KernelProcessor().generateExecutionContext(queryContext, new RuleMetaData(Arrays.asList(mockSQLTranslatorRule(), mockAggregatedDataSourceRule())), props);
            assertThat(actual.getExecutionUnits().size(), is(1));
            assertTrue(mockedCheckEngines.constructed().isEmpty());
            mockedSQLLogger.verifyNoInteractions();
        }
    }
    
    private SQLTranslatorRule mockSQLTranslatorRule() {
        SQLTranslatorRule result = mock(SQLTranslatorRule.class);
        when(result.getAttributes()).thenReturn(new RuleAttributes());
        when(result.translate(any(), any(), any(), any(), any(), any())).thenReturn(Optional.of(new SQLTranslatorContext("", Collections.emptyList())));
        return result;
    }
    
    private ShardingSphereRule mockAggregatedDataSourceRule() {
        ShardingSphereRule result = mock(ShardingSphereRule.class, RETURNS_DEEP_STUBS);
        AggregatedDataSourceRuleAttribute ruleAttribute = mock(AggregatedDataSourceRuleAttribute.class);
        when(ruleAttribute.getAggregatedDataSources()).thenReturn(Collections.singletonMap("ds_0", mock(DataSource.class)));
        when(result.getAttributes().findAttribute(AggregatedDataSourceRuleAttribute.class)).thenReturn(Optional.of(ruleAttribute));
        return result;
    }
    
    private QueryContext createQueryContext(final boolean skipMetadataValidate) {
        HintValueContext hintValueContext = new HintValueContext();
        hintValueContext.setSkipMetadataValidate(skipMetadataValidate);
        ConnectionContext connectionContext = mock(ConnectionContext.class);
        when(connectionContext.getCurrentDatabaseName()).thenReturn(Optional.of("foo_db"));
        ShardingSphereMetaData metaData = mock(ShardingSphereMetaData.class);
        ResourceMetaData resourceMetaData = mock(ResourceMetaData.class, RETURNS_DEEP_STUBS);
        when(resourceMetaData.getStorageUnits()).thenReturn(Collections.singletonMap("ds_0", mock(StorageUnit.class, RETURNS_DEEP_STUBS)));
        when(metaData.containsDatabase("foo_db")).thenReturn(true);
        ShardingSphereDatabase database = new ShardingSphereDatabase(
                "foo_db", databaseType, resourceMetaData, new RuleMetaData(Arrays.asList(mockSQLTranslatorRule(), mockAggregatedDataSourceRule())), Collections.emptyList());
        when(metaData.getDatabase("foo_db")).thenReturn(database);
        when(metaData.getProps()).thenReturn(new ConfigurationProperties(new Properties()));
        SQLStatementContext sqlStatementContext = new CommonSQLStatementContext(SelectStatement.builder().databaseType(databaseType).build());
        return new QueryContext(sqlStatementContext, "SELECT * FROM tbl", Collections.emptyList(), hintValueContext, connectionContext, metaData);
    }
    
    private ConfigurationProperties createProps(final boolean sqlShow) {
        return new ConfigurationProperties(PropertiesBuilder.build(new Property(ConfigurationPropertyKey.SQL_SHOW.getKey(), Boolean.toString(sqlShow))));
    }
}
