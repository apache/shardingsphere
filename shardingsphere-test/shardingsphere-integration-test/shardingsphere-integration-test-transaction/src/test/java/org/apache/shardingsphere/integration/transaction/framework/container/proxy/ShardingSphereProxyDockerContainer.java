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

package org.apache.shardingsphere.integration.transaction.framework.container.proxy;

import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.apache.shardingsphere.integration.transaction.env.IntegrationTestEnvironment;
import org.apache.shardingsphere.integration.transaction.env.enums.TransactionITEnvTypeEnum;
import org.apache.shardingsphere.integration.transaction.util.DatabaseTypeUtil;
import org.apache.shardingsphere.test.integration.env.container.atomic.DockerITContainer;
import org.apache.shardingsphere.test.integration.env.container.wait.JDBCConnectionWaitStrategy;
import org.apache.shardingsphere.test.integration.env.runtime.DataSourceEnvironment;
import org.testcontainers.containers.BindMode;

import java.sql.DriverManager;

/**
 * ShardingSphere proxy container.
 */
@Slf4j
public final class ShardingSphereProxyDockerContainer extends DockerITContainer {
    
    private final DatabaseType databaseType;
    
    public ShardingSphereProxyDockerContainer(final DatabaseType databaseType) {
        super("Transaction-Proxy", "apache/shardingsphere-proxy-test");
        this.databaseType = databaseType;
    }
    
    @Override
    protected void configure() {
        withExposedPorts(3307);
        mapConfigurationFiles();
        if (DatabaseTypeUtil.isPostgreSQL(databaseType) || DatabaseTypeUtil.isOpenGauss(databaseType)) {
            setWaitStrategy(new JDBCConnectionWaitStrategy(() -> DriverManager.getConnection(DataSourceEnvironment.getURL(databaseType, getHost(), getMappedPort(3307), "postgres"), "root", "root")));
        } else {
            setWaitStrategy(new JDBCConnectionWaitStrategy(() -> DriverManager.getConnection(DataSourceEnvironment.getURL(databaseType, getHost(), getMappedPort(3307), ""), "root", "root")));
        }
    }
    
    private void mapConfigurationFiles() {
        withClasspathResourceMapping(String.format("/env/%s/server.yaml", databaseType.getType().toLowerCase()), "/opt/shardingsphere-proxy/conf/server.yaml", BindMode.READ_ONLY);
        withClasspathResourceMapping("/logback-test.xml", "/opt/shardingsphere-proxy/conf/logback-test.xml", BindMode.READ_ONLY);
        if (TransactionITEnvTypeEnum.NATIVE == IntegrationTestEnvironment.getInstance().getItEnvType()) {
            addFixedExposedPort(3307, 3307);
            addFixedExposedPort(5005, 3308);
        }
    }
    
    @Override
    public String getAbbreviation() {
        return "proxy";
    }
}
