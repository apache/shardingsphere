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

package org.apache.shardingsphere.test.e2e.operation.pipeline.env;

import com.google.common.base.Strings;
import lombok.Getter;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.test.e2e.env.runtime.E2ETestEnvironment;
import org.apache.shardingsphere.test.e2e.env.runtime.EnvironmentPropertiesLoader;
import org.apache.shardingsphere.test.e2e.env.runtime.type.RunEnvironment;
import org.apache.shardingsphere.test.e2e.operation.pipeline.env.enums.PipelineProxyTypeEnum;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;

@Getter
public final class PipelineE2EEnvironment {
    
    private static final PipelineE2EEnvironment INSTANCE = new PipelineE2EEnvironment();
    
    private final Properties props;
    
    private final PipelineProxyTypeEnum itProxyType;
    
    private PipelineE2EEnvironment() {
        props = EnvironmentPropertiesLoader.loadProperties();
        itProxyType = PipelineProxyTypeEnum.valueOf(props.getProperty("pipeline.e2e.proxy.type", PipelineProxyTypeEnum.NONE.name()).toUpperCase());
    }
    
    /**
     * Get native database type.
     *
     * @return native database type
     */
    public String getNativeDatabaseType() {
        return String.valueOf(props.getProperty("pipeline.e2e.native.database"));
    }
    
    /**
     * Get instance.
     *
     * @return singleton instance
     */
    public static PipelineE2EEnvironment getInstance() {
        return INSTANCE;
    }
    
    /**
     * List storage container images.
     *
     * @param databaseType database type
     * @return database storage container images
     */
    public List<String> listStorageContainerImages(final DatabaseType databaseType) {
        // Native mode needn't use docker image, just return a list which contain one item
        if (RunEnvironment.Type.NATIVE == E2ETestEnvironment.getInstance().getRunEnvironment().getType()) {
            return databaseType.getType().equalsIgnoreCase(getNativeDatabaseType()) ? Collections.singletonList("") : Collections.emptyList();
        }
        return Arrays.stream(props.getOrDefault(String.format("e2e.artifact.database.%s.image", databaseType.getType().toLowerCase()), "").toString()
                .split(",")).filter(each -> !Strings.isNullOrEmpty(each)).collect(Collectors.toList());
    }
}
