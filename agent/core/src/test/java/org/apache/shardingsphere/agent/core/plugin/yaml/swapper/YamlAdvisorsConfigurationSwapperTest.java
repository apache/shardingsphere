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

package org.apache.shardingsphere.agent.core.plugin.yaml.swapper;

import org.apache.shardingsphere.agent.core.plugin.yaml.entity.YamlAdvisorConfiguration;
import org.apache.shardingsphere.agent.core.plugin.yaml.entity.YamlAdvisorsConfiguration;
import org.apache.shardingsphere.agent.core.plugin.yaml.entity.YamlPointcutConfiguration;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public final class YamlAdvisorsConfigurationSwapperTest {
    
    @Test
    public void assertUnmarshal() {
        YamlAdvisorsConfiguration actual = new YamlAdvisorsConfigurationSwapper().unmarshal(getClass().getResourceAsStream("/advisors.yaml"));
        assertThat(actual.getAdvisors().size(), is(5));
        List<YamlAdvisorConfiguration> actualYamlAdvisorConfigs = new ArrayList<>(actual.getAdvisors());
        assertYamlAdvisorConfiguration(actualYamlAdvisorConfigs.get(0), createExpectedYamlAdvisorConfiguration("org.apache.shardingsphere.proxy.frontend.command.CommandExecutorTask",
                "org.apache.shardingsphere.agent.metrics.api.advice.CommandExecutorTaskAdvice",
                Arrays.asList(createExpectedYamlPointcutConfiguration("run", "instance"), createExpectedYamlPointcutConfiguration("processException", "instance"))));
        assertYamlAdvisorConfiguration(actualYamlAdvisorConfigs.get(1), createExpectedYamlAdvisorConfiguration("org.apache.shardingsphere.proxy.frontend.netty.FrontendChannelInboundHandler",
                "org.apache.shardingsphere.agent.metrics.api.advice.ChannelHandlerAdvice",
                Arrays.asList(createExpectedYamlPointcutConfiguration("channelActive", "instance"),
                        createExpectedYamlPointcutConfiguration("channelRead", "instance"), createExpectedYamlPointcutConfiguration("channelInactive", "instance"))));
        assertYamlAdvisorConfiguration(actualYamlAdvisorConfigs.get(2), createExpectedYamlAdvisorConfiguration("org.apache.shardingsphere.infra.route.engine.SQLRouteEngine",
                "org.apache.shardingsphere.agent.metrics.api.advice.SQLRouteEngineAdvice", Collections.singleton(createExpectedYamlPointcutConfiguration("route", "instance"))));
        assertYamlAdvisorConfiguration(actualYamlAdvisorConfigs.get(3), createExpectedYamlAdvisorConfiguration(
                "org.apache.shardingsphere.proxy.backend.communication.jdbc.transaction.BackendTransactionManager",
                "org.apache.shardingsphere.agent.metrics.api.advice.TransactionAdvice",
                Arrays.asList(createExpectedYamlPointcutConfiguration("commit", "instance"), createExpectedYamlPointcutConfiguration("rollback", "instance"))));
        assertYamlAdvisorConfiguration(actualYamlAdvisorConfigs.get(4), createExpectedYamlAdvisorConfiguration("org.apache.shardingsphere.infra.config.datasource.JDBCParameterDecoratorHelper",
                "org.apache.shardingsphere.agent.metrics.api.advice.DataSourceAdvice", Collections.singleton(createExpectedYamlPointcutConfiguration("decorate", "static"))));
    }
    
    private void assertYamlAdvisorConfiguration(final YamlAdvisorConfiguration actual, final YamlAdvisorConfiguration expected) {
        assertThat(actual.getTarget(), is(expected.getTarget()));
        assertThat(actual.getAdvice(), is(expected.getAdvice()));
        assertThat(actual.getPointcuts().isEmpty(), is(expected.getPointcuts().isEmpty()));
        Iterator<YamlPointcutConfiguration> expectedYamlPointcutConfigs = expected.getPointcuts().iterator();
        for (YamlPointcutConfiguration each : actual.getPointcuts()) {
            YamlPointcutConfiguration expectedYamlPointcutConfig = expectedYamlPointcutConfigs.next();
            assertThat(each.getName(), is(expectedYamlPointcutConfig.getName()));
            assertThat(each.getType(), is(expectedYamlPointcutConfig.getType()));
        }
    }
    
    private YamlAdvisorConfiguration createExpectedYamlAdvisorConfiguration(final String target, final String advice, final Collection<YamlPointcutConfiguration> yamlPointcutConfigs) {
        YamlAdvisorConfiguration result = new YamlAdvisorConfiguration();
        result.setTarget(target);
        result.setAdvice(advice);
        result.setPointcuts(yamlPointcutConfigs);
        return result;
    }
    
    private YamlPointcutConfiguration createExpectedYamlPointcutConfiguration(final String name, final String type) {
        YamlPointcutConfiguration result = new YamlPointcutConfiguration();
        result.setName(name);
        result.setType(type);
        return result;
    }
}
