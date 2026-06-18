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

package org.apache.shardingsphere.proxy.backend.handler.distsql.ral.updatable.variable;

import org.apache.shardingsphere.distsql.handler.engine.update.DistSQLUpdateExecutor;
import org.apache.shardingsphere.distsql.statement.type.ral.updatable.SetDistVariableStatement;
import org.apache.shardingsphere.infra.config.mode.ModeConfiguration;
import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.config.props.ConfigurationPropertyKey;
import org.apache.shardingsphere.infra.config.props.temporary.TemporaryConfigurationPropertyKey;
import org.apache.shardingsphere.infra.exception.kernel.syntax.InvalidVariableValueException;
import org.apache.shardingsphere.infra.exception.kernel.syntax.UnsupportedVariableException;
import org.apache.shardingsphere.infra.instance.ComputeNodeInstance;
import org.apache.shardingsphere.infra.instance.ComputeNodeInstanceContext;
import org.apache.shardingsphere.infra.instance.metadata.InstanceMetaData;
import org.apache.shardingsphere.infra.instance.workerid.WorkerIdGenerator;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.statistics.ShardingSphereStatistics;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.infra.util.eventbus.EventBusContext;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.mode.metadata.MetaDataContexts;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Collections;
import java.util.Properties;
import java.util.function.Consumer;
import java.util.stream.Stream;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;

class SetDistVariableExecutorTest {
    
    private final SetDistVariableExecutor executor = (SetDistVariableExecutor) TypedSPILoader.getService(DistSQLUpdateExecutor.class, SetDistVariableStatement.class);
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("assertExecuteUpdateSuccessArguments")
    void assertExecuteUpdateSuccess(final String caseName, final SetDistVariableStatement statement, final Consumer<ContextManager> assertion) {
        ContextManager contextManager = mockContextManager();
        executor.executeUpdate(statement, contextManager);
        assertDoesNotThrow(() -> assertion.accept(contextManager));
    }
    
    private static Stream<Arguments> assertExecuteUpdateSuccessArguments() {
        return Stream.of(
                Arguments.of("configuration key", new SetDistVariableStatement("proxy_frontend_flush_threshold", "1024"),
                        (Consumer<ContextManager>) contextManager -> assertThat(contextManager.getMetaDataContexts().getMetaData().getProps()
                                .getValue(ConfigurationPropertyKey.PROXY_FRONTEND_FLUSH_THRESHOLD), is(1024))),
                Arguments.of("temporary configuration key", new SetDistVariableStatement("proxy_meta_data_collector_enabled", "false"),
                        (Consumer<ContextManager>) contextManager -> assertFalse(
                                (Boolean) contextManager.getMetaDataContexts().getMetaData().getTemporaryProps().getValue(TemporaryConfigurationPropertyKey.PROXY_META_DATA_COLLECTOR_ENABLED))),
                Arguments.of("typed spi property", new SetDistVariableStatement("proxy_frontend_database_protocol_type", "Fixture"),
                        (Consumer<ContextManager>) contextManager -> assertThat(contextManager.getMetaDataContexts().getMetaData().getProps().getProps()
                                .getProperty("proxy-frontend-database-protocol-type"), is("FIXTURE"))),
                Arguments.of("valid cron property", new SetDistVariableStatement("proxy_meta_data_collector_cron", "0 0/5 * * * ?"),
                        (Consumer<ContextManager>) contextManager -> assertThat(contextManager.getMetaDataContexts().getMetaData().getTemporaryProps().getProps()
                                .getProperty("proxy-meta-data-collector-cron"), is("0 0/5 * * * ?"))));
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("assertExecuteUpdateFailureArguments")
    void assertExecuteUpdateFailure(final String caseName, final SetDistVariableStatement statement, final Class<? extends Throwable> expectedExceptionType) {
        assertThrows(expectedExceptionType, () -> executor.executeUpdate(statement, mockContextManager()));
    }
    
    private ContextManager mockContextManager() {
        ComputeNodeInstanceContext computeNodeInstanceContext = new ComputeNodeInstanceContext(
                new ComputeNodeInstance(mock(InstanceMetaData.class)), new ModeConfiguration("Standalone", null), new EventBusContext());
        computeNodeInstanceContext.init(mock(WorkerIdGenerator.class));
        return new ContextManager(new MetaDataContexts(
                new ShardingSphereMetaData(Collections.emptyList(), mock(), mock(), new ConfigurationProperties(new Properties())), new ShardingSphereStatistics()),
                computeNodeInstanceContext, mock(), mock());
    }
    
    private static Stream<Arguments> assertExecuteUpdateFailureArguments() {
        return Stream.of(
                Arguments.of("unsupported variable", new SetDistVariableStatement("unknown", "1"), UnsupportedVariableException.class),
                Arguments.of("invalid numeric value", new SetDistVariableStatement("proxy_frontend_flush_threshold", "invalid"), InvalidVariableValueException.class),
                Arguments.of("invalid cron expression", new SetDistVariableStatement("proxy_meta_data_collector_cron", "invalid"), InvalidVariableValueException.class),
                Arguments.of("empty cron expression", new SetDistVariableStatement("proxy_meta_data_collector_cron", ""), InvalidVariableValueException.class));
    }
}
