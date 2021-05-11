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

package org.apache.shardingsphere.test.integration.junit.container.adapter;

import com.google.common.io.ByteStreams;
import lombok.Getter;
import lombok.SneakyThrows;
import org.apache.shardingsphere.authority.yaml.config.YamlAuthorityRuleConfiguration;
import org.apache.shardingsphere.infra.metadata.user.yaml.config.YamlUserConfiguration;
import org.apache.shardingsphere.infra.metadata.user.yaml.config.YamlUsersConfigurationConverter;
import org.apache.shardingsphere.infra.yaml.config.YamlRuleConfiguration;
import org.apache.shardingsphere.infra.yaml.engine.YamlEngine;
import org.apache.shardingsphere.proxy.config.yaml.YamlProxyServerConfiguration;
import org.apache.shardingsphere.test.integration.junit.container.ShardingSphereContainer;
import org.apache.shardingsphere.test.integration.junit.param.model.ParameterizedArray;

import javax.sql.DataSource;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;

/**
 * ShardingSphere adapter container.
 */
public abstract class ShardingSphereAdapterContainer extends ShardingSphereContainer {
    
    @Getter
    private final YamlUserConfiguration authentication;
    
    public ShardingSphereAdapterContainer(final String dockerName, final String dockerImageName, final ParameterizedArray parameterizedArray) {
        this(dockerName, dockerImageName, false, parameterizedArray);
    }
    
    @SneakyThrows
    public ShardingSphereAdapterContainer(final String dockerName, final String dockerImageName, final boolean isFakeContainer, final ParameterizedArray parameterizedArray) {
        super(dockerName, dockerImageName, isFakeContainer, parameterizedArray);
        this.authentication = loadAuthentication(parameterizedArray);
    }
    
    private YamlUserConfiguration loadAuthentication(final ParameterizedArray parameterizedArray) throws IOException {
        YamlProxyServerConfiguration configuration = YamlEngine.unmarshal(
                ByteStreams.toByteArray(this.getClass().getResourceAsStream("/docker/" + parameterizedArray.getScenario() + "/proxy/conf/server.yaml")),
                YamlProxyServerConfiguration.class
        );
        return YamlUsersConfigurationConverter.convertYamlUserConfiguration(getUsersFromConfiguration(configuration))
                .stream()
                .filter(each -> "root".equals(each.getUsername()))
                .findFirst()
                .orElse(new YamlUserConfiguration());
    }
    
    /**
     * Get DataSource.
     *
     * @return DataSource
     */
    public abstract DataSource getDataSource();

    private Collection<String> getUsersFromConfiguration(final YamlProxyServerConfiguration serverConfig) {
        for (YamlRuleConfiguration config : serverConfig.getRules()) {
            if (config instanceof YamlAuthorityRuleConfiguration) {
                YamlAuthorityRuleConfiguration authorityRuleConfig = (YamlAuthorityRuleConfiguration) config;
                return authorityRuleConfig.getUsers();
            }
        }
        return Collections.emptyList();
    }
    
}
