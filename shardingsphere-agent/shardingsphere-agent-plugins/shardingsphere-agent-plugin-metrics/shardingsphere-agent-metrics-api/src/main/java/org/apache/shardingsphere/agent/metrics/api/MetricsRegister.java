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
 * Metrics register.
 */
public interface MetricsRegister {
    
    /**
     * Register gauge.
     *
     * @param name  name
     * @param labelNames label names
     * @param doc doc
     */
    void registerGauge(String name, String[] labelNames, String doc);
    
    /**
     * Register counter.
     *
     * @param name name
     * @param labelNames label names
     * @param doc doc
     */
    void registerCounter(String name, String[] labelNames, String doc);
    
    /**
     * Register histogram.
     *
     * @param name name
     * @param labelNames label names
     * @param doc doc
     */
    void registerHistogram(String name, String[] labelNames, String doc);
    
    /**
     * Counter inc.
     *
     * @param name name
     * @param labelValues label values
     */
    void counterInc(String name, String[] labelValues);
    
    /**
     * counter inc.
     *
     * @param name name
     * @param labelValues label values
     * @param count count
     */
    void counterInc(String name, String[] labelValues, long count);
    
    /**
     * Gauge inc.
     *
     * @param name name
     * @param labelValues label values
     */
    void gaugeInc(String name, String[] labelValues);
    
    /**
     * Gauge dec.
     *
     * @param name name
     * @param labelValues label values
     */
    void gaugeDec(String name, String[] labelValues);
    
    /**
     * Record time.
     *
     * @param name  name
     * @param labelValues label values
     * @param duration duration
     */
    void recordTime(String name, String[] labelValues, long duration);
}
