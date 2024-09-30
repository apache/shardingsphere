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

package org.apache.shardingsphere.authority.yaml;

import org.apache.shardingsphere.authority.config.AuthorityRuleConfiguration;
import org.apache.shardingsphere.authority.config.UserConfiguration;
import org.apache.shardingsphere.infra.algorithm.core.config.AlgorithmConfiguration;
import org.apache.shardingsphere.test.it.yaml.YamlRuleConfigurationIT;

import java.util.Arrays;
import java.util.Collections;
import java.util.Properties;

class AuthorityRuleConfigurationYamlIT extends YamlRuleConfigurationIT {
    
    AuthorityRuleConfigurationYamlIT() {
        super("yaml/authority-rule.yaml", getExpectedRuleConfiguration());
    }
    
    private static AuthorityRuleConfiguration getExpectedRuleConfiguration() {
        return new AuthorityRuleConfiguration(
                Arrays.asList(new UserConfiguration("root", "root", "%", null, true), new UserConfiguration("sharding", "sharding", "%", null, false)),
                new AlgorithmConfiguration("ALL_PERMITTED", new Properties()), Collections.singletonMap("fixture", new AlgorithmConfiguration("FIXTURE", new Properties())), "fixture");
    }
}
