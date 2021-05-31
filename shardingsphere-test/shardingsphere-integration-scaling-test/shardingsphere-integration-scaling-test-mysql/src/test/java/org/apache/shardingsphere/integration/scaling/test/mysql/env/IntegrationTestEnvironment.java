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

package org.apache.shardingsphere.integration.scaling.test.mysql.env;

import lombok.Getter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.integration.scaling.test.mysql.util.ScalingUtil;
import org.apache.shardingsphere.integration.scaling.test.mysql.util.ExecuteUtil;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

@Getter
@Slf4j
public final class IntegrationTestEnvironment {
    
    private static final IntegrationTestEnvironment INSTANCE = new IntegrationTestEnvironment();
    
    private final boolean isEnvironmentPrepared;
    
    private Properties engineEnvProps;
    
    @SneakyThrows
    private IntegrationTestEnvironment() {
        engineEnvProps = loadProperties("env/engine-env.properties");
        isEnvironmentPrepared = engineEnvProps.getProperty("it.env.value").equals(engineEnvProps.getProperty("it.env.type"));
    }
    
    private Properties loadProperties(final String propsFileName) {
        Properties result = new Properties();
        try (InputStream inputStream = IntegrationTestEnvironment.class.getClassLoader().getResourceAsStream(propsFileName)) {
            result.load(inputStream);
        } catch (final IOException ex) {
            throw new RuntimeException(ex);
        }
        return result;
    }
    
    /**
     * Wait for environment ready.
     */
    public void waitForEnvironmentReady() {
        log.info("wait begin scaling environment");
        new ExecuteUtil(() -> isScalingReady(engineEnvProps), Integer.parseInt(engineEnvProps.getProperty("scaling.retry", "30")),
                Long.parseLong(engineEnvProps.getProperty("scaling.waitMs", "1000"))).execute();
    }
    
    private boolean isScalingReady(final Properties engineEnvProps) {
        try {
            ScalingUtil.getInstance().getJobList();
        } catch (final IOException ignore) {
            return false;
        }
        log.info("it scaling environment success");
        return true;
    }
    
    /**
     * Get instance.
     *
     * @return singleton instance
     */
    public static IntegrationTestEnvironment getInstance() {
        return INSTANCE;
    }
}
