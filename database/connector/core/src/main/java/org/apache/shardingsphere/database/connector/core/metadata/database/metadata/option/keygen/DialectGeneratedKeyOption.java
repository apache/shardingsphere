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

package org.apache.shardingsphere.database.connector.core.metadata.database.metadata.option.keygen;

/**
 * Dialect generated key option.
 */
public interface DialectGeneratedKeyOption {
    
    /**
     * Get generated key column name.
     *
     * @return generated key column name
     */
    String getColumnName();
    
    /**
     * Check if the explicit value triggers an auto-increment key generation.
     *
     * @param value explicit insert value
     * @return whether the value triggers generated key
     */
    boolean isGeneratedKeyTriggerValue(Object value);
}
