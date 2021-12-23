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

package org.apache.shardingsphere.data.pipeline.core.util;

import lombok.SneakyThrows;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.shardingsphere.data.pipeline.api.config.rulealtered.JobConfiguration;
import org.apache.shardingsphere.data.pipeline.api.config.rulealtered.RuleConfiguration;
import org.apache.shardingsphere.data.pipeline.api.config.rulealtered.WorkflowConfiguration;
import org.apache.shardingsphere.infra.config.datasource.jdbc.config.JDBCDataSourceConfiguration;
import org.apache.shardingsphere.infra.config.datasource.jdbc.config.JDBCDataSourceConfigurationWrapper;
import org.apache.shardingsphere.infra.config.datasource.jdbc.config.YamlJDBCDataSourceConfiguration;
import org.apache.shardingsphere.infra.config.datasource.jdbc.config.impl.ShardingSphereJDBCDataSourceConfiguration;
import org.apache.shardingsphere.infra.config.datasource.jdbc.config.impl.StandardJDBCDataSourceConfiguration;
import org.apache.shardingsphere.sharding.yaml.config.YamlShardingRuleConfiguration;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.stream.Collectors;

/**
 * Resource util.
 */
public final class ResourceUtil {
    
    /**
     * Mock job configuration.
     *
     * @return job configuration
     */
    public static JobConfiguration mockJobConfig() {
        return mockStandardJdbcTargetJobConfig();
    }
    
    /**
     * Mock standard JDBC as target job configuration.
     *
     * @return standard JDBC as target job configuration
     */
    public static JobConfiguration mockStandardJdbcTargetJobConfig() {
        JobConfiguration result = new JobConfiguration();
        WorkflowConfiguration workflowConfig = new WorkflowConfiguration("logic_db", Collections.singletonList(YamlShardingRuleConfiguration.class.getName()), "id1");
        result.setWorkflowConfig(workflowConfig);
        RuleConfiguration ruleConfig = new RuleConfiguration();
        result.setRuleConfig(ruleConfig);
        ruleConfig.setSource(createYamlJDBCDataSourceConfiguration(new ShardingSphereJDBCDataSourceConfiguration(readFileToString("/config_sharding_sphere_jdbc_source.yaml"))));
        ruleConfig.setTarget(createYamlJDBCDataSourceConfiguration(new StandardJDBCDataSourceConfiguration(readFileToString("/config_standard_jdbc_target.yaml"))));
        result.buildHandleConfig();
        return result;
    }
    
    private static YamlJDBCDataSourceConfiguration createYamlJDBCDataSourceConfiguration(final JDBCDataSourceConfiguration jdbcDataSourceConfig) {
        JDBCDataSourceConfigurationWrapper targetWrapper = new JDBCDataSourceConfigurationWrapper(jdbcDataSourceConfig.getType(), jdbcDataSourceConfig.getParameter());
        YamlJDBCDataSourceConfiguration result = new YamlJDBCDataSourceConfiguration();
        result.setType(targetWrapper.getType());
        result.setParameter(targetWrapper.getParameter());
        return result;
    }
    
    @SneakyThrows(IOException.class)
    private static String readFileToString(final String fileName) {
        try (InputStream in = ResourceUtil.class.getResourceAsStream(fileName)) {
            return IOUtils.toString(in, StandardCharsets.UTF_8);
        }
    }

    /**
     * Ignore comments to read configuration from YAML.
     * 
     * @param fileName YAML file name.
     * @return YAML configuration.
     */
    @SneakyThrows({IOException.class, URISyntaxException.class})
    public static String readFileAndIgnoreComments(final String fileName) {
        return Files.readAllLines(Paths.get(ClassLoader.getSystemResource(fileName).toURI()))
                .stream().filter(each -> StringUtils.isNotBlank(each) && !each.startsWith("#")).map(each -> each + System.lineSeparator()).collect(Collectors.joining());
    }
}
