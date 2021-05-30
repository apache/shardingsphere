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

package org.apache.shardingsphere.integration.scaling.test.mysql.util;

import com.google.gson.Gson;
import org.apache.shardingsphere.infra.yaml.engine.YamlEngine;
import org.apache.shardingsphere.integration.scaling.test.mysql.env.IntegrationTestEnvironment;
import org.apache.shardingsphere.scaling.core.config.JobConfiguration;
import org.apache.shardingsphere.scaling.core.config.RuleConfiguration;
import org.apache.shardingsphere.scaling.core.config.datasource.ScalingDataSourceConfigurationWrap;
import org.apache.shardingsphere.scaling.core.config.datasource.ShardingSphereJDBCDataSourceConfiguration;
import org.apache.shardingsphere.scaling.core.config.datasource.StandardJDBCDataSourceConfiguration;

import java.util.Properties;

/**
 * Target data source util.
 */
public final class TargetDataSourceUtil {
    
    private static final String TARGET_JDBC_URL = "jdbc:mysql://%s/ds_dst?useSSL=false";
    
    private static final Properties ENGINE_ENV_PROPS = IntegrationTestEnvironment.getInstance().getEngineEnvProps();
    
    /**
     * Create docker scaling job configurations.
     *
     * @return scaling job configurations
     */
    public static String createDockerConfigurations() {
        JobConfiguration jobConfiguration = new JobConfiguration();
        RuleConfiguration ruleConfiguration = new RuleConfiguration();
        ruleConfiguration.setSource(new ShardingSphereJDBCDataSourceConfiguration(YamlEngine.marshal(SourceShardingSphereUtil.createDockerConfigurations())).wrap());
        ruleConfiguration.setTarget(createDockerTarget());
        jobConfiguration.setRuleConfig(ruleConfiguration);
        return new Gson().toJson(jobConfiguration);
    }
    
    private static ScalingDataSourceConfigurationWrap createDockerTarget() {
        StandardJDBCDataSourceConfiguration configuration = new StandardJDBCDataSourceConfiguration(
                String.format(TARGET_JDBC_URL, ENGINE_ENV_PROPS.getProperty("db.host.docker")),
                ENGINE_ENV_PROPS.getProperty("db.username"), ENGINE_ENV_PROPS.getProperty("db.password"));
        return configuration.wrap();
    }
}
