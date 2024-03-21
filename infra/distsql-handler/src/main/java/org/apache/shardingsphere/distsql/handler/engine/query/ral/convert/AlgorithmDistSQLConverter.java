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

package org.apache.shardingsphere.distsql.handler.engine.query.ral.convert;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.infra.algorithm.core.config.AlgorithmConfiguration;

import java.util.Iterator;
import java.util.Properties;
import java.util.TreeMap;

/**
 * Algorithm DistSQL converter.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class AlgorithmDistSQLConverter {
    
    private static final String ALGORITHM_TYPE_WITHOUT_PROPS = "TYPE(NAME='%s')";
    
    private static final String ALGORITHM_TYPE_WITH_PROPS = "TYPE(NAME='%s', PROPERTIES(%s))";
    
    /**
     * Get algorithm type.
     *
     * @param algorithmConfig algorithm configuration
     * @return algorithm type
     */
    public static String getAlgorithmType(final AlgorithmConfiguration algorithmConfig) {
        if (null == algorithmConfig) {
            return "";
        }
        StringBuilder result = new StringBuilder();
        String type = algorithmConfig.getType().toLowerCase();
        result.append(algorithmConfig.getProps().isEmpty()
                ? String.format(ALGORITHM_TYPE_WITHOUT_PROPS, type)
                : String.format(ALGORITHM_TYPE_WITH_PROPS, type, getAlgorithmProperties(algorithmConfig.getProps())));
        return result.toString();
    }
    
    /**
     * Get algorithm properties.
     *
     * @param props properties
     * @return algorithm properties
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public static String getAlgorithmProperties(final Properties props) {
        StringBuilder result = new StringBuilder();
        Iterator<String> iterator = new TreeMap(props).keySet().iterator();
        while (iterator.hasNext()) {
            String key = iterator.next();
            Object value = props.get(key);
            if (null == value) {
                continue;
            }
            result.append(String.format(DistSQLScriptConstants.PROPERTY, key, value));
            if (iterator.hasNext()) {
                result.append(DistSQLScriptConstants.COMMA).append(' ');
            }
        }
        return result.toString();
    }
}
