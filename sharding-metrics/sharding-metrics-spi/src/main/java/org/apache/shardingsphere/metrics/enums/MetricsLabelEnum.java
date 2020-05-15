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

package org.apache.shardingsphere.metrics.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Metrics label enum.
 */
@RequiredArgsConstructor
@Getter
public enum MetricsLabelEnum {
    
    /**
     * Request total metrics label.
     */
    REQUEST_TOTAL("request_total"),
    
    /**
     * SQL statement count metrics label.
     */
    SQL_STATEMENT_COUNT("sql_statement_count"),
    
    /**
     * Channel count metrics label.
     */
    CHANNEL_COUNT("channel_count"),
    
    /**
     * Request latency metrics label.
     */
    REQUEST_LATENCY("request_latency"),
  
    /**
     * Sharding table metrics label.
     */
    SHARDING_TABLE("sharding_table"),
    
    /**
     * Sharding datasource metrics label.
     */
    SHARDING_DATASOURCE("sharding_datasource"),
    
    /**
     * Transaction metrics label.
     */
    TRANSACTION("transaction"),
    
    /**
     * Shadow hit total label.
     */
    SHADOW_HIT_TOTAL("shadow_hit_total");
    
    private final String name;
}
