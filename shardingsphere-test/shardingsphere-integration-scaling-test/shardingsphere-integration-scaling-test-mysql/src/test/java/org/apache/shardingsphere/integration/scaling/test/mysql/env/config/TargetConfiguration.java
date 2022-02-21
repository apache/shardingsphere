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

package org.apache.shardingsphere.integration.scaling.test.mysql.env.config;

import com.zaxxer.hikari.HikariDataSource;
import org.apache.shardingsphere.integration.scaling.test.mysql.env.IntegrationTestEnvironment;
import org.apache.shardingsphere.data.pipeline.api.datasource.config.impl.StandardPipelineDataSourceConfiguration;

import javax.sql.DataSource;
import java.util.Properties;

/**
 * Target standard jdbc configuration.
 */
public final class TargetConfiguration {
    
    private static final String TARGET_JDBC_URL = "jdbc:mysql://%s/ds_dst?useSSL=false";
    
    private static final Properties ENGINE_ENV_PROPS = IntegrationTestEnvironment.getInstance().getEngineEnvProps();
    
    /**
     * Get docker standard pipeline configuration.
     *
     * @return standard pipeline configuration
     */
    public static StandardPipelineDataSourceConfiguration getDockerConfiguration() {
        return getConfiguration(ENGINE_ENV_PROPS.getProperty("db.host.docker"));
    }
    
    /**
     * Get host standard pipeline configuration.
     *
     * @return standard pipeline configuration
     */
    public static StandardPipelineDataSourceConfiguration getHostConfiguration() {
        return getConfiguration(ENGINE_ENV_PROPS.getProperty("db.host.host"));
    }
    
    private static StandardPipelineDataSourceConfiguration getConfiguration(final String host) {
        return new StandardPipelineDataSourceConfiguration(String.format(TARGET_JDBC_URL, host), ENGINE_ENV_PROPS.getProperty("db.username"), ENGINE_ENV_PROPS.getProperty("db.password"));
    }
    
    /**
     * Create host standard pipeline data source.
     *
     * @return data source
     */
    public static DataSource createHostDataSource() {
        return new HikariDataSource(TargetConfiguration.getHostConfiguration().getHikariConfig());
    }
}
