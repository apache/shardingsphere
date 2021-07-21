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

package org.apache.shardingsphere.driver.api.yaml;

import org.apache.shardingsphere.authority.api.config.PasswordEncryptRuleConfiguration;
import org.apache.shardingsphere.encrypt.security.SimplePasswordEncrypt;
import org.apache.shardingsphere.infra.config.RuleConfiguration;
import org.apache.shardingsphere.infra.security.PasswordEncryptFactory;
import org.apache.shardingsphere.infra.yaml.config.pojo.YamlRootRuleConfigurations;
import org.apache.shardingsphere.infra.yaml.config.pojo.YamlRuleConfiguration;
import org.apache.shardingsphere.infra.yaml.config.swapper.YamlRuleConfigurationSwapperEngine;

import java.util.Collection;
import java.util.stream.Collectors;

/**
 * Password encrypt util tools.
 */
public final class PasswordEncryptUtil {

    /**
     * Init PasswordEncryptFactory by YamlRootConfiguration.
     * @param rootRuleConfigurations the configuration
     */
    public static void initPasswordEncrypt(final YamlRootRuleConfigurations rootRuleConfigurations) {
        YamlRuleConfigurationSwapperEngine engine = new YamlRuleConfigurationSwapperEngine();
        Collection<YamlRuleConfiguration> yamlRuleConfigurations = rootRuleConfigurations.getRules()
            .stream()
            .filter(rule -> rule.getRuleConfigurationType() == PasswordEncryptRuleConfiguration.class)
            .collect(Collectors.toList());

        RuleConfiguration pwdCfg = engine.swapToRuleConfigurations(yamlRuleConfigurations).stream().findFirst().orElse(null);
        initPasswordEncrypt((PasswordEncryptRuleConfiguration) pwdCfg);
    }

    /**
     * Init PasswordEncryptFactory by configuration.
     * @param configuration the password encrypt rule configuration
     */
    public static void initPasswordEncrypt(final PasswordEncryptRuleConfiguration configuration) {
        SimplePasswordEncrypt passwordEncrypt = new SimplePasswordEncrypt();
        passwordEncrypt.init(configuration);
        PasswordEncryptFactory.getInstance().setEncrypt(passwordEncrypt);
    }
}
