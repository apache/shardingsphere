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
import org.apache.shardingsphere.infra.util.props.PropertiesUtils;

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
                : String.format(ALGORITHM_TYPE_WITH_PROPS, type, PropertiesUtils.toString(algorithmConfig.getProps())));
        return result.toString();
    }
}
