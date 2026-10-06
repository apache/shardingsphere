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

package org.apache.shardingsphere.proxy.backend.config.validator.constraint;

import org.apache.shardingsphere.proxy.backend.config.ProxyConfigurationLoadResult;
import org.apache.shardingsphere.proxy.backend.config.yaml.YamlProxyDatabaseConfiguration;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

/**
 * No conflicting data source names validator.
 */
public final class NoConflictingDataSourceNamesValidator implements ConstraintValidator<NoConflictingDataSourceNames, ProxyConfigurationLoadResult> {
    
    @Override
    public boolean isValid(final ProxyConfigurationLoadResult value, final ConstraintValidatorContext context) {
        Collection<String> globalDataSourceNames = value.getServerConfiguration().getDataSources().keySet();
        for (Entry<String, YamlProxyDatabaseConfiguration> entry : value.getDatabaseConfigurations().entrySet()) {
            Collection<String> duplicatedDataSourceNames = globalDataSourceNames.stream().filter(entry.getValue().getDataSources().keySet()::contains).collect(Collectors.toSet());
            if (!duplicatedDataSourceNames.isEmpty()) {
                context.disableDefaultConstraintViolation();
                addViolations(entry.getKey(), duplicatedDataSourceNames, context);
                return false;
            }
        }
        return true;
    }
    
    private void addViolations(final String databaseKey, final Collection<String> dataSourceNames, final ConstraintValidatorContext context) {
        for (String each : dataSourceNames) {
            context.buildConstraintViolationWithTemplate(context.getDefaultConstraintMessageTemplate())
                    .addPropertyNode("databaseConfigurations")
                    .addPropertyNode("dataSources")
                    .inContainer(Map.class, 1)
                    .inIterable()
                    .atKey(databaseKey)
                    .addBeanNode()
                    .inContainer(Map.class, 1)
                    .inIterable()
                    .atKey(each)
                    .addConstraintViolation();
        }
    }
}
