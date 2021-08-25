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

package org.apache.shardingsphere.shadow.algorithm.shadow;

import java.util.Optional;

/**
 * Shadow algorithm type.
 */
public enum ShadowAlgorithmType {
    
    /**
     * Shadow algorithm use column match with regular expression.
     */
    COLUMN_REGEX_MATCH,
    
    /**
     * Shadow algorithm use simple sql note.
     */
    SIMPLE_NOTE;
    
    /**
     * Create shadow algorithm type.
     *
     * @param shadowAlgorithmType shadow algorithm type
     * @return shadow operation type
     */
    public static Optional<ShadowAlgorithmType> newInstance(final String shadowAlgorithmType) {
        if (ShadowAlgorithmType.COLUMN_REGEX_MATCH.name().equals(shadowAlgorithmType)) {
            return Optional.of(ShadowAlgorithmType.COLUMN_REGEX_MATCH);
        }
        if (ShadowAlgorithmType.SIMPLE_NOTE.name().equals(shadowAlgorithmType)) {
            return Optional.of(ShadowAlgorithmType.SIMPLE_NOTE);
        }
        return Optional.empty();
    }
}
