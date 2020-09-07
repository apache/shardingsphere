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

package org.apache.shardingsphere.proxy.init.impl;

import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.db.protocol.mysql.constant.MySQLServerInfo;
import org.apache.shardingsphere.infra.config.properties.ConfigurationPropertyKey;
import org.apache.shardingsphere.infra.context.SchemaContext;
import org.apache.shardingsphere.infra.context.SchemaContexts;
import org.apache.shardingsphere.infra.context.SchemaContextsBuilder;
import org.apache.shardingsphere.proxy.backend.schema.ProxyDataSourceContext;
import org.apache.shardingsphere.proxy.backend.schema.ProxySchemaContexts;
import org.apache.shardingsphere.proxy.config.ProxyConfiguration;
import org.apache.shardingsphere.proxy.config.YamlProxyConfiguration;
import org.apache.shardingsphere.proxy.db.DatabaseServerInfo;
import org.apache.shardingsphere.proxy.frontend.bootstrap.ShardingSphereProxy;
import org.apache.shardingsphere.proxy.init.BootstrapInitializer;
import org.apache.shardingsphere.tracing.opentracing.OpenTracingTracer;
import org.apache.shardingsphere.transaction.ShardingTransactionManagerEngine;
import org.apache.shardingsphere.transaction.context.TransactionContexts;
import org.apache.shardingsphere.transaction.context.impl.StandardTransactionContexts;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;

/**
 * Abstract bootstrap initializer.
 */
@Slf4j
public abstract class AbstractBootstrapInitializer implements BootstrapInitializer {
    
    @Override
    public final void init(final YamlProxyConfiguration yamlConfig, final int port) throws SQLException {
        ProxyConfiguration proxyConfig = getProxyConfiguration(yamlConfig);
        SchemaContexts schemaContexts = decorateSchemaContexts(createSchemaContexts(proxyConfig));
        TransactionContexts transactionContexts = decorateTransactionContexts(createTransactionContexts(schemaContexts));
        ProxySchemaContexts.getInstance().init(schemaContexts, transactionContexts);
        initOpenTracing();
        setDatabaseServerInfo();
        ShardingSphereProxy.getInstance().start(port);
    }
    
    private SchemaContexts createSchemaContexts(final ProxyConfiguration proxyConfig) throws SQLException {
        ProxyDataSourceContext dataSourceContext = new ProxyDataSourceContext(proxyConfig.getSchemaDataSources());
        SchemaContextsBuilder schemaContextsBuilder = new SchemaContextsBuilder(
                dataSourceContext.getDatabaseType(), dataSourceContext.getDataSourcesMap(), proxyConfig.getSchemaRules(), proxyConfig.getAuthentication(), proxyConfig.getProps());
        return schemaContextsBuilder.build();
    }
    
    private TransactionContexts createTransactionContexts(final SchemaContexts schemaContexts) {
        Map<String, ShardingTransactionManagerEngine> transactionManagerEngines = new HashMap<>(schemaContexts.getSchemaContexts().size(), 1);
        for (Entry<String, SchemaContext> entry : schemaContexts.getSchemaContexts().entrySet()) {
            ShardingTransactionManagerEngine engine = new ShardingTransactionManagerEngine();
            engine.init(schemaContexts.getDatabaseType(), entry.getValue().getSchema().getDataSources());
            transactionManagerEngines.put(entry.getKey(), engine);
        }
        return new StandardTransactionContexts(transactionManagerEngines);
    }
    
    private void initOpenTracing() {
        if (ProxySchemaContexts.getInstance().getSchemaContexts().getProps().<Boolean>getValue(ConfigurationPropertyKey.PROXY_OPENTRACING_ENABLED)) {
            OpenTracingTracer.init();
        }
    }
    
    private void setDatabaseServerInfo() {
        Optional<DataSource> dataSourceSample = ProxySchemaContexts.getInstance().getDataSourceSample();
        if (dataSourceSample.isPresent()) {
            DatabaseServerInfo databaseServerInfo = new DatabaseServerInfo(dataSourceSample.get());
            log.info(databaseServerInfo.toString());
            MySQLServerInfo.setServerVersion(databaseServerInfo.getDatabaseVersion());
        }
    }
    
    protected abstract ProxyConfiguration getProxyConfiguration(YamlProxyConfiguration yamlConfig);
    
    protected abstract SchemaContexts decorateSchemaContexts(SchemaContexts schemaContexts);
    
    protected abstract TransactionContexts decorateTransactionContexts(TransactionContexts transactionContexts);
}
