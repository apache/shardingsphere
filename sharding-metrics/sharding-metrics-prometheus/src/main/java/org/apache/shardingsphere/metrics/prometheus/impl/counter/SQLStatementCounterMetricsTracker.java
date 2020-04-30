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

package org.apache.shardingsphere.metrics.prometheus.impl.counter;

import io.prometheus.client.Counter;
import org.apache.shardingsphere.metrics.api.CounterMetricsTracker;
import org.apache.shardingsphere.metrics.enums.MetricsLabelEnum;

/**
 * SQL statement counter metrics tracker.
 */
public final class SQLStatementCounterMetricsTracker implements CounterMetricsTracker {
    
    private static final Counter SQL_STATEMENT_COUNT = Counter.build()
            .name("sql_statement_count")
            .labelNames("sql_type")
            .help("proxy sql statement count")
            .register();
    
    @Override
    public void inc(final double amount, final String... labelValues) {
        SQL_STATEMENT_COUNT.labels(labelValues).inc(amount);
    }
    
    @Override
    public String metricsLabel() {
        return MetricsLabelEnum.SQL_STATEMENT_COUNT.getName();
    }
}

