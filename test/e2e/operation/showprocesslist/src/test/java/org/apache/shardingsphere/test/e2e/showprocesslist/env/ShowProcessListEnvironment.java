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

package org.apache.shardingsphere.test.e2e.showprocesslist.env;

import com.google.common.base.Splitter;
import lombok.Getter;
import lombok.SneakyThrows;
import org.apache.shardingsphere.test.e2e.env.runtime.scenario.path.ScenarioCommonPath;
import org.apache.shardingsphere.test.e2e.showprocesslist.env.enums.ShowProcessListEnvTypeEnum;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Properties;

@Getter
public final class ShowProcessListEnvironment {
    
    private static final ShowProcessListEnvironment INSTANCE = new ShowProcessListEnvironment();
    
    private final Collection<String> runModes;
    
    private final Properties props;
    
    private final ShowProcessListEnvTypeEnum itEnvType;
    
    private final Collection<String> scenarios;
    
    private final Collection<String> governanceCenters;
    
    private ShowProcessListEnvironment() {
        props = loadProperties();
        runModes = Splitter.on(",").trimResults().splitToList(props.getProperty("showprocesslist.it.run.modes", "Standalone,Cluster"));
        itEnvType = ShowProcessListEnvTypeEnum.valueOf(props.getProperty("showprocesslist.it.env.type", ShowProcessListEnvTypeEnum.NONE.name()).toUpperCase());
        scenarios = getScenarios(props);
        governanceCenters = Splitter.on(",").trimResults().splitToList(props.getProperty("showprocesslist.it.governance.centers"));
    }
    
    /**
     * Get instance.
     *
     * @return singleton instance
     */
    public static ShowProcessListEnvironment getInstance() {
        return INSTANCE;
    }
    
    @SneakyThrows(IOException.class)
    private Properties loadProperties() {
        Properties result = new Properties();
        try (InputStream inputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream("env/it-env.properties")) {
            result.load(inputStream);
        }
        for (String each : System.getProperties().stringPropertyNames()) {
            result.setProperty(each, System.getProperty(each));
        }
        return result;
    }
    
    private Collection<String> getScenarios(final Properties props) {
        Collection<String> result = Splitter.on(",").trimResults().splitToList(props.getProperty("showprocesslist.it.scenarios", "cluster_jdbc_proxy"));
        for (String each : result) {
            new ScenarioCommonPath(each).checkFolderExist();
        }
        return result;
    }
}
