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

package org.apache.shardingsphere.integration.data.pipline.container.compose;

import com.zaxxer.hikari.HikariDataSource;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;
import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.apache.shardingsphere.integration.data.pipline.container.proxy.ShardingSphereProxyDockerContainer;
import org.apache.shardingsphere.test.integration.env.DataSourceEnvironment;
import org.apache.shardingsphere.test.integration.util.NetworkAliasUtil;

import javax.sql.DataSource;

/**
 * Composed container, include governance container and database container.
 */
public final class DockerComposedContainer extends BaseComposedContainer {
    
    @Getter
    private final ShardingSphereProxyDockerContainer proxyContainer;
    
    public DockerComposedContainer(final DatabaseType databaseType) {
        super(databaseType);
        ShardingSphereProxyDockerContainer proxyContainer = new ShardingSphereProxyDockerContainer(databaseType);
        proxyContainer.addExposedPort(3307);
        proxyContainer.dependsOn(getGovernanceContainer(), getDatabaseContainer());
        this.proxyContainer = getContainers().registerContainer(proxyContainer, NetworkAliasUtil.getNetworkAlias("sharding-proxy"));
    }
    
    @Override
    public void stop() {
        super.stop();
        proxyContainer.stop();
    }
    
    @Override
    public DataSource getProxyDataSource(final String databaseName) {
        HikariDataSource result = new HikariDataSource();
        result.setDriverClassName(DataSourceEnvironment.getDriverClassName(getDatabaseContainer().getDatabaseType()));
        String jdbcUrl = StringUtils.appendIfMissing(DataSourceEnvironment.getURL(getDatabaseContainer().getDatabaseType(), getProxyContainer().getHost(),
                getProxyContainer().getFirstMappedPort(), databaseName), "&rewriteBatchedStatements=true");
        result.setJdbcUrl(jdbcUrl);
        result.setUsername("root");
        result.setPassword("root");
        result.setMaximumPoolSize(2);
        result.setTransactionIsolation("TRANSACTION_READ_COMMITTED");
        return result;
    }
}
