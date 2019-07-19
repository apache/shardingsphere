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

package org.apache.shardingsphere.shardingjdbc.orchestration.api;

import org.apache.shardingsphere.api.config.encrypt.EncryptRuleConfiguration;
import org.apache.shardingsphere.orchestration.config.OrchestrationConfiguration;
import org.apache.shardingsphere.shardingjdbc.jdbc.core.datasource.EncryptDataSource;
import org.apache.shardingsphere.shardingjdbc.orchestration.internal.datasource.OrchestrationEncryptDataSource;

import javax.sql.DataSource;
import java.util.Properties;

/**
 * Orchestration encrypt data source factory.
 *
 * @author yangyi
 */
public final class OrchestrationEncryptDataSourceFactory {
    
    /**
     * Create orchestration encrypt data source.
     *
     * @param dataSource data source
     * @param encryptRuleConfig encrypt rule configuration
     * @param props properties
     * @param orchestrationConfig orchestration configuration
     * @return orchestration encrypt data source
     */
    public static DataSource createDataSource(final DataSource dataSource, 
                                              final EncryptRuleConfiguration encryptRuleConfig, final Properties props, final OrchestrationConfiguration orchestrationConfig) {
        if (null == encryptRuleConfig || encryptRuleConfig.getEncryptors().isEmpty()) {
            return createDataSource(orchestrationConfig);
        }
        return new OrchestrationEncryptDataSource(new EncryptDataSource(dataSource, encryptRuleConfig, props), orchestrationConfig);
    }
    
    /**
     * Create orchestration encrypt data source.
     *
     * @param orchestrationConfig orchestration configuration
     * @return orchestration encrypt data source
     */
    public static DataSource createDataSource(final OrchestrationConfiguration orchestrationConfig) {
        return new OrchestrationEncryptDataSource(orchestrationConfig);
    }
}
