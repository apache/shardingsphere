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

package org.apache.shardingsphere.infra.datasource.state;

import com.google.common.base.Strings;

import java.util.HashMap;
import java.util.Map;

/**
 * Data source state.
 */
public enum DataSourceState {
    
    DISABLED, ENABLED;
    
    private static final Map<String, DataSourceState> DATA_SOURCE_STATES = new HashMap<>(2, 1);
    
    static {
        DATA_SOURCE_STATES.put(DISABLED.name().toLowerCase(), DISABLED);
        DATA_SOURCE_STATES.put(ENABLED.name().toLowerCase(), ENABLED);
    }
    
    /**
     * Data source disable or enable.
     *
     * @param state data source state
     * @return disable or enable
     */
    public static boolean isDisable(final String state) {
        return DISABLED.name().equalsIgnoreCase(state);
    }
    
    /**
     * Data source disable or enable.
     *
     * @param state data source state
     * @return disable or enable
     */
    public static boolean isEnable(final String state) {
        return ENABLED.name().equalsIgnoreCase(state);
    }
    
    /**
     * Get data source state by state name.
     *
     * @param state data source state name
     * @return data source state
     */
    public static DataSourceState getDataSourceState(final String state) {
        if (!Strings.isNullOrEmpty(state) && DATA_SOURCE_STATES.containsKey(state)) {
            return DATA_SOURCE_STATES.get(state);
        }
        throw new IllegalArgumentException("Illegal data source state: " + state);
    }
}
