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
     * Counter increase by value.
     *
     * @param value value
     */
    default void counterInc(long value) {
    }
    
    /**
     * Counter increase by 1L.
     */
    default void counterInc() {
        counterInc(1L);
    }
    
    /**
     * Counter increase by value with labels.
     *
     * @param value  value
     * @param labels counter labels
     */
    default void counterInc(long value, String... labels) {
    }
    
    /**
     * Counter increase by 1L with labels.
     *
     * @param labels counter labels
     */
    default void counterInc(String... labels) {
        counterInc(1, labels);
    }
    
    /**
     * Gauge increase by 1L.
     */
    default void gaugeInc() {
        gaugeInc(1L);
    }
    
    /**
     * Gauge increase by value.
     *
     * @param value value
     */
    default void gaugeInc(double value) {
    }
    
    /**
     * Gauge decrease by 1L.
     */
    default void gaugeDec() {
        gaugeDec(1L);
    }
    
    /**
     * Gauge decrease by value.
     *
     * @param value value
     */
    default void gaugeDec(double value) {
    }
    
    /**
     * Histogram observed by value.
     *
     * @param value value
     */
    default void histogramObserve(double value) {
    }
    
    /**
     * Summary observed by value.
     *
     * @param value value
     */
    default void summaryObserve(double value) {
    }
    
    /**
     * Delegated with object.
     *
     * @param object object
     */
    default void delegate(Object object) {
    }
}
