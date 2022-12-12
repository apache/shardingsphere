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

package org.apache.shardingsphere.shadow.api.shadow;

import java.util.Optional;

/**
 * Operation types supported by shadow.
 */
public enum ShadowOperationType {
    
    /**
     * The shadow operation is insert.
     */
    INSERT,
    
    /**
     * The shadow operation is delete.
     */
    DELETE,
    
    /**
     * The shadow operation is update.
     */
    UPDATE,
    
    /**
     * The shadow operation is select.
     */
    SELECT,
    
    /**
     * The shadow operation is SQL hint match.
     */
    HINT_MATCH;
    
    /**
     * Contains operation type.
     *
     * @param operationType operation type
     * @return shadow operation type
     */
    public static Optional<ShadowOperationType> contains(final String operationType) {
        if (ShadowOperationType.INSERT.name().equalsIgnoreCase(operationType)) {
            return Optional.of(ShadowOperationType.INSERT);
        }
        if (ShadowOperationType.DELETE.name().equalsIgnoreCase(operationType)) {
            return Optional.of(ShadowOperationType.DELETE);
        }
        if (ShadowOperationType.UPDATE.name().equalsIgnoreCase(operationType)) {
            return Optional.of(ShadowOperationType.UPDATE);
        }
        if (ShadowOperationType.SELECT.name().equalsIgnoreCase(operationType)) {
            return Optional.of(ShadowOperationType.SELECT);
        }
        return Optional.empty();
    }
}
