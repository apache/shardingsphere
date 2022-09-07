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

/**
 * Data source state.
 */
public enum DataSourceState {
    
    DISABLED, ENABLED;
    
    /**
     * Data source disable or enable.
     *
     * @param status data source state
     * @return disable or enable
     */
    public static boolean isDisable(final String status) {
        return DISABLED.name().toLowerCase().equals(status);
    }
    
    /**
     * Data source disable or enable.
     *
     * @param status storage node status
     * @return disable or enable
     */
    public static boolean isEnable(final String status) {
        return ENABLED.name().toLowerCase().equals(status);
    }
    
    /**
     * Get data source state by state name.
     *
     * @param state data source state name
     * @return data source state
     */
    public static DataSourceState getDataSourceState(final String state) {
        for (DataSourceState each : values()) {
            if (each.name().equalsIgnoreCase(state)) {
                return each;
            }
        }
        throw new IllegalArgumentException("Illegal data source state: " + state);
    }
}
