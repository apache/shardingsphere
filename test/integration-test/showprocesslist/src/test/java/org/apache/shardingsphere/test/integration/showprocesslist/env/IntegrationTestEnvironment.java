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

package org.apache.shardingsphere.test.integration.showprocesslist.env;

import com.google.common.base.Splitter;
import lombok.Getter;
import lombok.SneakyThrows;
import org.apache.shardingsphere.test.integration.env.runtime.scenario.path.ScenarioCommonPath;
import org.apache.shardingsphere.test.integration.showprocesslist.env.enums.ITEnvTypeEnum;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Properties;

@Getter
public final class IntegrationTestEnvironment {
    
    private static final IntegrationTestEnvironment INSTANCE = new IntegrationTestEnvironment();
    
    private final Properties props;
    
    private final ITEnvTypeEnum itEnvType;
    
    private final Collection<String> scenarios;
    
    private IntegrationTestEnvironment() {
        props = loadProperties();
        itEnvType = ITEnvTypeEnum.valueOf(props.getProperty("it.env.type", ITEnvTypeEnum.NONE.name()).toUpperCase());
        scenarios = getScenarios(props);
    }
    
    /**
     * Get instance.
     *
     * @return singleton instance
     */
    public static IntegrationTestEnvironment getInstance() {
        return INSTANCE;
    }
    
    @SneakyThrows(IOException.class)
    private Properties loadProperties() {
        Properties result = new Properties();
        try (InputStream inputStream = IntegrationTestEnvironment.class.getClassLoader().getResourceAsStream("env/it-env.properties")) {
            result.load(inputStream);
        }
        for (String each : System.getProperties().stringPropertyNames()) {
            result.setProperty(each, System.getProperty(each));
        }
        return result;
    }
    
    private Collection<String> getScenarios(final Properties props) {
        Collection<String> result = Splitter.on(",").trimResults().splitToList(props.getProperty("it.scenarios"));
        for (String each : result) {
            new ScenarioCommonPath(each).checkFolderExist();
        }
        return result;
    }
}
