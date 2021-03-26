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

package org.apache.shardingsphere.test.integration.junit.compose;

import com.google.common.collect.Maps;
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.test.integration.env.EnvironmentType;
import org.apache.shardingsphere.test.integration.env.IntegrationTestEnvironment;
import org.apache.shardingsphere.test.integration.env.database.DatabaseEnvironmentManager;
import org.apache.shardingsphere.test.integration.junit.param.model.ParameterizedArray;
import org.junit.rules.ExternalResource;

import java.util.Map;

@RequiredArgsConstructor
public class ComposeManagement extends ExternalResource {
    
    private final Map<String, ContainerCompose> composeMap = Maps.newHashMap();
    
    private final String suiteName;
    
    /**
     * Create or get container compose.
     *
     * @param parameterizedArray parameterized array
     * @return container compose
     */
    public ContainerCompose getOrCreateCompose(final ParameterizedArray parameterizedArray) {
        String key = generateKey(parameterizedArray);
        if (composeMap.containsKey(key)) {
            return composeMap.get(key);
        }
        ContainerCompose compose = new ContainerCompose(suiteName, parameterizedArray);
        composeMap.put(key, compose);
        return compose;
    }
    
    private String generateKey(final ParameterizedArray parameter) {
        return new StringBuffer(suiteName)
                .append('-')
                .append(parameter.getScenario())
                .append('-')
                .append(parameter.getAdapter())
                .append('-')
                .append(parameter.getDatabaseType().getName())
                .toString();
    }
    
    @Override
    protected void before() throws Throwable {
        if (EnvironmentType.DOCKER != IntegrationTestEnvironment.getInstance().getEnvType()) {
            DatabaseEnvironmentManager.executeInitSQLs();
        } else {
            composeMap.values().forEach(e -> {
                e.start();
                e.waitUntilReady();
            });
        }
    }
    
    @Override
    protected void after() {
        composeMap.values().forEach(ContainerCompose::close);
    }
}
