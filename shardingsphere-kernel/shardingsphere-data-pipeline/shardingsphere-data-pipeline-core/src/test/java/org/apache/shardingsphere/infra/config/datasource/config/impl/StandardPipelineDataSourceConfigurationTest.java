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

package org.apache.shardingsphere.infra.config.datasource.config.impl;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableMap;
import lombok.SneakyThrows;
import org.apache.shardingsphere.data.pipeline.api.datasource.config.impl.StandardPipelineDataSourceConfiguration;
import org.junit.Test;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.stream.Collectors;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public final class StandardPipelineDataSourceConfigurationTest {
    
    @Test
    public void assertConstructionByParameter() {
        String parameter = readFileAndIgnoreComments("config_standard_jdbc_target.yaml");
        new StandardPipelineDataSourceConfiguration(parameter);
    }
    
    /**
     * Ignore comments to read configuration from YAML.
     *
     * @param fileName YAML file name.
     * @return YAML configuration.
     */
    @SneakyThrows({IOException.class, URISyntaxException.class})
    private static String readFileAndIgnoreComments(final String fileName) {
        return Files.readAllLines(Paths.get(ClassLoader.getSystemResource(fileName).toURI()))
                .stream().filter(each -> !Strings.isNullOrEmpty(each) && !each.startsWith("#")).map(each -> each + System.lineSeparator()).collect(Collectors.joining());
    }
    
    @Test
    public void assertAppendJDBCParameters() {
        StandardPipelineDataSourceConfiguration pipelineDataSourceConfig = new StandardPipelineDataSourceConfiguration(
                "jdbc:mysql://192.168.0.1:3306/scaling?serverTimezone=UTC&useSSL=false", null, null);
        pipelineDataSourceConfig.appendJDBCParameters(ImmutableMap.<String, String>builder().put("rewriteBatchedStatements", "true").build());
        assertThat(pipelineDataSourceConfig.getHikariConfig().getJdbcUrl(), is("jdbc:mysql://192.168.0.1:3306/scaling?rewriteBatchedStatements=true&serverTimezone=UTC&useSSL=false"));
    }
}
