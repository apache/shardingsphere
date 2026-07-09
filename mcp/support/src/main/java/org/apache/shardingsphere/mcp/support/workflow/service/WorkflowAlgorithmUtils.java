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

package org.apache.shardingsphere.mcp.support.workflow.service;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.infra.exception.external.ShardingSphereExternalException;
import org.apache.shardingsphere.infra.spi.ShardingSphereServiceLoader;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPI;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;

import java.util.Collection;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

/**
 * Workflow algorithm utilities.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class WorkflowAlgorithmUtils {
    
    private static final String SECRET_REFERENCE_PREFIX = "secret_reference:";
    
    private static final String ALGORITHM_TYPE_KEY = "type";
    
    /**
     * Normalize algorithm type.
     *
     * @param algorithmType algorithm type
     * @return normalized algorithm type
     */
    public static String normalizeAlgorithmType(final String algorithmType) {
        return Objects.toString(algorithmType, "").trim().toUpperCase(Locale.ENGLISH);
    }
    
    /**
     * Get algorithm type from algorithm row.
     *
     * @param algorithmRow algorithm row
     * @param typeKeys algorithm type keys in fallback order
     * @return normalized algorithm type
     */
    public static String getAlgorithmType(final Map<String, Object> algorithmRow, final String... typeKeys) {
        for (String each : typeKeys.length > 0 ? typeKeys : new String[]{ALGORITHM_TYPE_KEY}) {
            if (algorithmRow.containsKey(each)) {
                return normalizeAlgorithmType(Objects.toString(algorithmRow.get(each), ""));
            }
        }
        return "";
    }
    
    /**
     * Check whether algorithm rows contain algorithm type.
     *
     * @param algorithmRows algorithm rows
     * @param algorithmType algorithm type
     * @param typeKeys algorithm type keys in fallback order
     * @return whether algorithm rows contain algorithm type
     */
    public static boolean containsAlgorithm(final Collection<Map<String, Object>> algorithmRows, final String algorithmType, final String... typeKeys) {
        String actualAlgorithmType = normalizeAlgorithmType(algorithmType);
        return algorithmRows.stream().map(each -> getAlgorithmType(each, typeKeys)).anyMatch(actualAlgorithmType::equals);
    }
    
    /**
     * Check whether an algorithm service can be used by workflow generated artifacts.
     *
     * @param serviceInterface typed SPI service interface
     * @param algorithmType algorithm type
     * @param properties algorithm properties
     * @param <T> SPI class type
     * @return whether the algorithm service is available
     */
    public static <T extends TypedSPI> boolean isAlgorithmServiceAvailable(final Class<T> serviceInterface, final String algorithmType, final Map<String, String> properties) {
        String actualAlgorithmType = Objects.toString(algorithmType, "").trim();
        if (actualAlgorithmType.isEmpty()) {
            return true;
        }
        if (hasSecretReference(properties)) {
            return containsServiceType(serviceInterface, actualAlgorithmType);
        }
        try {
            TypedSPILoader.checkService(serviceInterface, actualAlgorithmType, WorkflowSQLUtils.createProperties(properties));
            return true;
        } catch (final ShardingSphereExternalException | IllegalArgumentException ignored) {
            return false;
        }
    }
    
    private static boolean hasSecretReference(final Map<String, String> properties) {
        return properties.values().stream().anyMatch(each -> Objects.toString(each, "").startsWith(SECRET_REFERENCE_PREFIX));
    }
    
    private static <T extends TypedSPI> boolean containsServiceType(final Class<T> serviceInterface, final String algorithmType) {
        for (T each : ShardingSphereServiceLoader.getServiceInstances(serviceInterface)) {
            if (matchesType(algorithmType, each)) {
                return true;
            }
        }
        return false;
    }
    
    private static boolean matchesType(final String type, final TypedSPI instance) {
        Object instanceType = instance.getType();
        if (null == instanceType) {
            return false;
        }
        if (instanceType instanceof String) {
            return instanceType.toString().equalsIgnoreCase(type) || instance.getTypeAliases().contains(type);
        }
        return instanceType.equals(type) || instance.getTypeAliases().contains(type);
    }
}
