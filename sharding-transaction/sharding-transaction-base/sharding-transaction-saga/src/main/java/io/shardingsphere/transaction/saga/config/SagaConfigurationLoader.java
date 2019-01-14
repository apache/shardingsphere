/*
 * Copyright 2016-2018 shardingsphere.io.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package io.shardingsphere.transaction.saga.config;

import com.google.common.base.Optional;
import com.google.common.base.Strings;
import io.shardingsphere.core.exception.ShardingException;
import io.shardingsphere.transaction.saga.constant.SagaRecoveryPolicy;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.util.Properties;

/**
 * Saga configuration loader.
 *
 * @author yangyi
 */
@Slf4j
public class SagaConfigurationLoader {
    
    private static final String CONFIGURATION_FILE = "saga.properties";
    
    private static final String PREFIX = "saga.actuator.";
    
    private static final String EXECUTOR_SIZE = PREFIX + "executorSize";
    
    private static final String TRANSACTION_MAX_RERTIES = PREFIX + "transactionMaxRetries";
    
    private static final String COMPENSATION_MAX_RERTIES = PREFIX + "compensationMaxRetries";
    
    private static final String TRANSACTION_RERTY_DELAY = PREFIX + "transactionRetryDelay";
    
    private static final String COMPENSATION_RERTY_DELAY = PREFIX + "compensationRetryDelay";
    
    private static final String RECOVERY_POLICY = PREFIX + "recoveryPolicy";
    
    /**
     * Load saga configuration from properties file.
     *
     * @return saga configuration
     */
    public static SagaConfiguration load() {
        SagaConfiguration result = new SagaConfiguration();
        Optional<File> propertiesFile = lookingForSagaPropertiesFile();
        if (propertiesFile.isPresent()) {
            try {
                initSagaConfiguration(result, propertiesFile.get());
            } catch (IOException ex) {
                throw new ShardingException("load saga properties failed.", ex);
            }
        }
        return result;
    }
    
    private static Optional<File> lookingForSagaPropertiesFile() {
        URL propertiesFileUrl = SagaConfigurationLoader.class.getClassLoader().getResource(CONFIGURATION_FILE);
        if (null == propertiesFileUrl) {
            log.warn("{} not found at your root classpath, will use default saga configuration", CONFIGURATION_FILE);
            return Optional.absent();
        }
        return Optional.of(new File(propertiesFileUrl.getFile()));
    }
    
    private static void initSagaConfiguration(final SagaConfiguration sagaConfiguration, final File propertiesFile) throws IOException {
        Properties sagaProperties = new Properties();
        sagaProperties.load(new FileInputStream(propertiesFile));
        String executorSize = sagaProperties.getProperty(EXECUTOR_SIZE);
        if (!Strings.isNullOrEmpty(executorSize)) {
            sagaConfiguration.setExecutorSize(Integer.parseInt(executorSize));
        }
        String transactionMaxRetries = sagaProperties.getProperty(TRANSACTION_MAX_RERTIES);
        if (!Strings.isNullOrEmpty(transactionMaxRetries)) {
            sagaConfiguration.setTransactionMaxRetries(Integer.parseInt(transactionMaxRetries));
        }
        String compensationMaxRetries = sagaProperties.getProperty(COMPENSATION_MAX_RERTIES);
        if (!Strings.isNullOrEmpty(transactionMaxRetries)) {
            sagaConfiguration.setCompensationMaxRetries(Integer.parseInt(compensationMaxRetries));
        }
        String transactionRetryDelay = sagaProperties.getProperty(TRANSACTION_RERTY_DELAY);
        if (!Strings.isNullOrEmpty(transactionMaxRetries)) {
            sagaConfiguration.setTransactionRetryDelay(Integer.parseInt(transactionRetryDelay));
        }
        String compensationRetryDelay = sagaProperties.getProperty(COMPENSATION_RERTY_DELAY);
        if (!Strings.isNullOrEmpty(transactionMaxRetries)) {
            sagaConfiguration.setCompensationRetryDelay(Integer.parseInt(compensationRetryDelay));
        }
        String recoveryPolicy = sagaProperties.getProperty(RECOVERY_POLICY);
        if (!Strings.isNullOrEmpty(recoveryPolicy)) {
            sagaConfiguration.setRecoveryPolicy(SagaRecoveryPolicy.find(recoveryPolicy));
        }
    }
}
