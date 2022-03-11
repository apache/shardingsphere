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

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import org.apache.commons.lang3.StringUtils;
import org.apache.shardingsphere.data.pipeline.api.config.rulealtered.JobConfiguration;
import org.apache.shardingsphere.data.pipeline.api.config.rulealtered.PipelineConfiguration;
import org.apache.shardingsphere.data.pipeline.api.config.rulealtered.WorkflowConfiguration;
import org.apache.shardingsphere.data.pipeline.api.datasource.config.PipelineDataSourceConfiguration;
import org.apache.shardingsphere.data.pipeline.api.datasource.config.impl.ShardingSpherePipelineDataSourceConfiguration;
import org.apache.shardingsphere.data.pipeline.api.datasource.config.impl.StandardPipelineDataSourceConfiguration;
import org.apache.shardingsphere.data.pipeline.api.datasource.config.yaml.YamlPipelineDataSourceConfiguration;
import org.apache.shardingsphere.sharding.yaml.config.YamlShardingRuleConfiguration;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.stream.Collectors;

/**
 * Resource util.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ResourceUtil {
    
    /**
     * Create job configuration.
     *
     * @return created job configuration
     */
    public static JobConfiguration createJobConfiguration() {
        JobConfiguration result = new JobConfiguration();
        result.setWorkflowConfig(new WorkflowConfiguration("logic_db", Collections.singletonList(YamlShardingRuleConfiguration.class.getName()), "0"));
        PipelineConfiguration pipelineConfig = new PipelineConfiguration();
        pipelineConfig.setSource(createYamlPipelineDataSourceConfiguration(new ShardingSpherePipelineDataSourceConfiguration(readFile("config_sharding_sphere_jdbc_source.yaml"))));
        pipelineConfig.setTarget(createYamlPipelineDataSourceConfiguration(new StandardPipelineDataSourceConfiguration(readFile("config_standard_jdbc_target.yaml"))));
        result.setPipelineConfig(pipelineConfig);
        result.buildHandleConfig();
        return result;
    }
    
    private static YamlPipelineDataSourceConfiguration createYamlPipelineDataSourceConfiguration(final PipelineDataSourceConfiguration config) {
        YamlPipelineDataSourceConfiguration result = new YamlPipelineDataSourceConfiguration();
        result.setType(config.getType());
        result.setParameter(config.getParameter());
        return result;
    }
    
    /**
     * Read file content.
     *
     * @param fileName file name
     * @return file content
     */
    @SneakyThrows({IOException.class, URISyntaxException.class})
    public static String readFile(final String fileName) {
        return String.join(System.lineSeparator(), Files.readAllLines(Paths.get(ClassLoader.getSystemResource(fileName).toURI())));
    }

    /**
     * Read file and ignore comments.
     * 
     * @param fileName file name
     * @return file content without comments
     */
    @SneakyThrows({IOException.class, URISyntaxException.class})
    public static String readFileAndIgnoreComments(final String fileName) {
        return Files.readAllLines(Paths.get(ClassLoader.getSystemResource(fileName).toURI()))
                .stream().filter(each -> StringUtils.isNotBlank(each) && !each.startsWith("#")).map(each -> each + System.lineSeparator()).collect(Collectors.joining());
    }
}
