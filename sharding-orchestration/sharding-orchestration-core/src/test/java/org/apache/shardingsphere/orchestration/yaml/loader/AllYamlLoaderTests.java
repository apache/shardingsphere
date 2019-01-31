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

package org.apache.shardingsphere.orchestration.yaml.loader;

import org.apache.shardingsphere.orchestration.yaml.loader.impl.AuthenticationYamlLoaderTest;
import org.apache.shardingsphere.orchestration.yaml.loader.impl.ConfigMapYamlLoaderTest;
import org.apache.shardingsphere.orchestration.yaml.loader.impl.DataSourceConfigurationsYamlLoaderTest;
import org.apache.shardingsphere.orchestration.yaml.loader.impl.MasterSlaveRuleConfigurationYamlLoaderTest;
import org.apache.shardingsphere.orchestration.yaml.loader.impl.PropertiesYamlLoaderTest;
import org.apache.shardingsphere.orchestration.yaml.loader.impl.ShardingRuleConfigurationYamlLoaderTest;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({
        DataSourceConfigurationsYamlLoaderTest.class, 
        ShardingRuleConfigurationYamlLoaderTest.class, 
        MasterSlaveRuleConfigurationYamlLoaderTest.class, 
        AuthenticationYamlLoaderTest.class, 
        ConfigMapYamlLoaderTest.class, 
        PropertiesYamlLoaderTest.class
})
public final class AllYamlLoaderTests {
}
