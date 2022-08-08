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

package org.apache.shardingsphere.integration.data.pipeline.framework.container.proxy;

import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.apache.shardingsphere.test.integration.env.container.atomic.DockerITContainer;
import org.apache.shardingsphere.test.integration.env.container.atomic.util.DatabaseTypeUtil;
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
    
    private final String dockerImageName;
    
    public ShardingSphereProxyDockerContainer(final DatabaseType databaseType, final String dockerImageName) {
        super("Scaling-Proxy", "apache/shardingsphere-proxy-test");
        this.databaseType = databaseType;
        this.dockerImageName = dockerImageName;
    }
    
    @Override
    protected void configure() {
        withExposedPorts(3307);
        mapConfigurationFiles();
        // TODO openGauss can't use this wait strategy now.
        if (DatabaseTypeUtil.isPostgreSQL(databaseType)) {
            setWaitStrategy(
                    new JDBCConnectionWaitStrategy(() -> DriverManager.getConnection(DataSourceEnvironment.getURL(databaseType, getHost(), getMappedPort(3307), "postgres"), "root", "Root@123")));
        } else if (DatabaseTypeUtil.isMySQL(databaseType)) {
            setWaitStrategy(new JDBCConnectionWaitStrategy(() -> DriverManager.getConnection(DataSourceEnvironment.getURL(databaseType, getHost(), getMappedPort(3307), ""), "root", "Root@123")));
        }
    }
    
    private void mapConfigurationFiles() {
        if (DatabaseTypeUtil.isMySQL(databaseType)) {
            String majorVersion = DatabaseTypeUtil.parseMajorVersion(dockerImageName);
            withClasspathResourceMapping(String.format("/env/%s/server-%s.yaml", databaseType.getType().toLowerCase(), majorVersion), "/opt/shardingsphere-proxy/conf/server.yaml", BindMode.READ_ONLY);
        } else {
            withClasspathResourceMapping(String.format("/env/%s/server.yaml", databaseType.getType().toLowerCase()), "/opt/shardingsphere-proxy/conf/server.yaml", BindMode.READ_ONLY);
        }
        withClasspathResourceMapping("/env/logback.xml", "/opt/shardingsphere-proxy/conf/logback.xml", BindMode.READ_ONLY);
    }
    
    @Override
    public String getAbbreviation() {
        return "proxy";
    }
}
