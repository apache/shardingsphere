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

package org.apache.shardingsphere.proxy;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.db.protocol.mysql.constant.MySQLServerInfo;
import org.apache.shardingsphere.infra.config.properties.ConfigurationPropertyKey;
import org.apache.shardingsphere.kernel.context.SchemaContext;
import org.apache.shardingsphere.kernel.context.SchemaContexts;
import org.apache.shardingsphere.kernel.context.SchemaContextsBuilder;
import org.apache.shardingsphere.orchestration.core.facade.OrchestrationFacade;
import org.apache.shardingsphere.orchestration.core.transaction.OrchestrationTransactionManagerEngineContexts;
import org.apache.shardingsphere.proxy.arg.BootstrapArguments;
import org.apache.shardingsphere.proxy.backend.schema.ProxyDataSourceContext;
import org.apache.shardingsphere.proxy.backend.schema.ProxySchemaContexts;
import org.apache.shardingsphere.proxy.config.ProxyConfiguration;
import org.apache.shardingsphere.proxy.config.ProxyConfigurationLoader;
import org.apache.shardingsphere.proxy.config.YamlProxyConfiguration;
import org.apache.shardingsphere.proxy.config.yaml.swapper.YamlProxyConfigurationSwapper;
import org.apache.shardingsphere.proxy.db.DatabaseServerInfo;
import org.apache.shardingsphere.proxy.frontend.bootstrap.ShardingSphereProxy;
import org.apache.shardingsphere.proxy.orchestration.OrchestrationBootstrap;
import org.apache.shardingsphere.proxy.orchestration.schema.ProxyOrchestrationSchemaContexts;
import org.apache.shardingsphere.tracing.opentracing.OpenTracingTracer;
import org.apache.shardingsphere.transaction.ShardingTransactionManagerEngine;
import org.apache.shardingsphere.transaction.context.TransactionManagerEngineContexts;
import org.apache.shardingsphere.transaction.context.impl.StandardTransactionManagerEngineContexts;

import javax.sql.DataSource;
import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;

/**
 * ShardingSphere-Proxy Bootstrap.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Slf4j
public final class Bootstrap {
    
    /**
     * Main entrance.
     *
     * @param args startup arguments
     * @throws IOException IO exception
     * @throws SQLException SQL exception
     */
    public static void main(final String[] args) throws IOException, SQLException {
        BootstrapArguments bootstrapArgs = new BootstrapArguments(args);
        int port = bootstrapArgs.getPort();
        YamlProxyConfiguration yamlConfig = ProxyConfigurationLoader.load(bootstrapArgs.getConfigurationPath());
        if (null == yamlConfig.getServerConfiguration().getOrchestration()) {
            init(new YamlProxyConfigurationSwapper().swap(yamlConfig), port, false);
        } else {
            try (OrchestrationFacade orchestrationFacade = OrchestrationFacade.getInstance()) {
                init(new OrchestrationBootstrap(orchestrationFacade).init(yamlConfig), port, true);
            }
        }
    }
    
    private static void init(final ProxyConfiguration proxyConfig, final int port, final boolean orchestrationEnabled) throws SQLException {
        initSchemaContexts(proxyConfig, orchestrationEnabled);
        initOpenTracing();
        setDatabaseServerInfo();
        ShardingSphereProxy.getInstance().start(port);
    }
    
    private static void initSchemaContexts(final ProxyConfiguration proxyConfig, final boolean orchestrationEnabled) throws SQLException {
        ProxyDataSourceContext dataSourceContext = new ProxyDataSourceContext(proxyConfig.getSchemaDataSources());
        SchemaContextsBuilder schemaContextsBuilder = new SchemaContextsBuilder(
                dataSourceContext.getDatabaseType(), dataSourceContext.getDataSourcesMap(), proxyConfig.getSchemaRules(), proxyConfig.getAuthentication(), proxyConfig.getProps());
        SchemaContexts schemaContexts = createSchemaContexts(schemaContextsBuilder.build(), orchestrationEnabled);
        TransactionManagerEngineContexts transactionContexts = createTransactionManagerEngineContexts(schemaContexts, orchestrationEnabled);
        ProxySchemaContexts.getInstance().init(schemaContexts, transactionContexts);
    }
    
    private static SchemaContexts createSchemaContexts(final SchemaContexts schemaContexts, final boolean orchestrationEnabled) {
        return orchestrationEnabled ? new ProxyOrchestrationSchemaContexts(schemaContexts, OrchestrationFacade.getInstance()) : schemaContexts;
    }
    
    private static TransactionManagerEngineContexts createTransactionManagerEngineContexts(final SchemaContexts schemaContexts, final boolean orchestrationEnabled) {
        Map<String, ShardingTransactionManagerEngine> transactionManagerEngines = new HashMap<>(schemaContexts.getSchemaContexts().size(), 1);
        for (Entry<String, SchemaContext> entry : schemaContexts.getSchemaContexts().entrySet()) {
            ShardingTransactionManagerEngine engine = new ShardingTransactionManagerEngine();
            engine.init(schemaContexts.getDatabaseType(), entry.getValue().getSchema().getDataSources());
            transactionManagerEngines.put(entry.getKey(), engine);
        }
        TransactionManagerEngineContexts contexts = new StandardTransactionManagerEngineContexts(transactionManagerEngines);
        return orchestrationEnabled ? new OrchestrationTransactionManagerEngineContexts(contexts) : contexts;
    }
    
    private static void initOpenTracing() {
        if (ProxySchemaContexts.getInstance().getSchemaContexts().getProps().<Boolean>getValue(ConfigurationPropertyKey.PROXY_OPENTRACING_ENABLED)) {
            OpenTracingTracer.init();
        }
    }
    
    private static void setDatabaseServerInfo() {
        Optional<DataSource> dataSourceSample = ProxySchemaContexts.getInstance().getDataSourceSample();
        if (dataSourceSample.isPresent()) {
            DatabaseServerInfo databaseServerInfo = new DatabaseServerInfo(dataSourceSample.get());
            log.info(databaseServerInfo.toString());
            MySQLServerInfo.setServerVersion(databaseServerInfo.getDatabaseVersion());
        }
    }
}
