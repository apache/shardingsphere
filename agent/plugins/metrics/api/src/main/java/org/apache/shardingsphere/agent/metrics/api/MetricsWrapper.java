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

package org.apache.shardingsphere.agent.metrics.api;

/**
 * Metrics wrapper.
 */
public interface MetricsWrapper {
    
    /**
     * Metric increase.
     */
    default void inc() {
        inc(1);
    }
    
    /**
     * Metric increase by value.
     *
     * @param value value
     */
    default void inc(double value) {
    }
    
    /**
     * Metric increase with labels.
     *
     * @param labels labels
     */
    default void inc(String... labels) {
        inc(1, labels);
    }
    
    /**
     * Metric increase by value with labels.
     *
     * @param value value
     * @param labels labels
     */
    default void inc(double value, String... labels) {
    }
    
    /**
     * Metric decrease.
     */
    default void dec() {
        dec(1);
    }
    
    /**
     * Metric decrease by value.
     *
     * @param value value
     */
    default void dec(double value) {
    }
    
    /**
     * Metric decrease with labels.
     *
     * @param labels counter labels
     */
    default void dec(String... labels) {
        dec(1, labels);
    }
    
    /**
     * Metric decrease by value with labels.
     *
     * @param value value
     * @param labels labels
     */
    default void dec(double value, String... labels) {
    }
    
    /**
     * Observed by value.
     *
     * @param value value
     */
    default void observe(double value) {
    }
    
    /**
     * Delegated with object.
     *
     * @param object object
     */
    default void delegate(Object object) {
    }
}
