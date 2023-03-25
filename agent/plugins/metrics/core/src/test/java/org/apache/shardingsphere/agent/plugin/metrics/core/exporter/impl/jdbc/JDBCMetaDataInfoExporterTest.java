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

package org.apache.shardingsphere.agent.plugin.metrics.core.exporter.impl.jdbc;

import org.apache.shardingsphere.agent.plugin.metrics.core.collector.MetricsCollectorRegistry;
import org.apache.shardingsphere.agent.plugin.metrics.core.collector.type.GaugeMetricFamilyMetricsCollector;
import org.apache.shardingsphere.agent.plugin.metrics.core.config.MetricCollectorType;
import org.apache.shardingsphere.agent.plugin.metrics.core.config.MetricConfiguration;
import org.apache.shardingsphere.agent.plugin.metrics.core.fixture.collector.MetricsCollectorFixture;
import org.apache.shardingsphere.driver.ShardingSphereDriver;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JDBCMetaDataInfoExporterTest {
    
    @AfterEach
    void reset() {
        MetricConfiguration config = new MetricConfiguration("jdbc_meta_data_info",
                MetricCollectorType.GAUGE_METRIC_FAMILY, "Meta data information of ShardingSphere-JDBC",
                Arrays.asList("database", "type"), Collections.emptyMap());
        ((MetricsCollectorFixture) MetricsCollectorRegistry.get(config, "FIXTURE")).reset();
    }
    
    @Test
    void assertExport() throws SQLException {
        DriverManager.registerDriver(new ShardingSphereDriver());
        DriverManager.getConnection("jdbc:shardingsphere:classpath:config/driver/foo-driver-fixture.yaml");
        Optional<GaugeMetricFamilyMetricsCollector> collector = new JDBCMetaDataInfoExporter().export("FIXTURE");
        assertTrue(collector.isPresent());
        assertThat(collector.get().toString(), is("foo_driver_fixture_db=2, storage_unit_count=2"));
    }
}
