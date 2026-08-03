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

package org.apache.shardingsphere.infra.yaml.data.swapper;

import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereColumn;
import org.apache.shardingsphere.infra.metadata.statistics.RowStatistics;
import org.apache.shardingsphere.infra.yaml.data.pojo.YamlRowStatistics;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.sql.Types;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.is;

class YamlRowStatisticsSwapperTest {
    
    @Test
    void assertSwapToYamlConfigurationWithNullRows() {
        YamlRowStatisticsSwapper swapper = new YamlRowStatisticsSwapper(Collections.emptyList());
        YamlRowStatistics actual = swapper.swapToYamlConfiguration(new RowStatistics("foo_unique_key", null));
        assertThat(actual.getRows(), is(empty()));
        assertThat(actual.getUniqueKey(), is("foo_unique_key"));
    }
    
    @Test
    void assertConvertSpecialTypesWhenSwappingToYaml() {
        List<ShardingSphereColumn> columns = Arrays.asList(
                new ShardingSphereColumn("decimal_col", Types.DECIMAL, false, false, false, true, false, true),
                new ShardingSphereColumn("numeric_col", Types.NUMERIC, false, false, false, true, false, true),
                new ShardingSphereColumn("decimal_null", Types.DECIMAL, false, false, false, true, false, true),
                new ShardingSphereColumn("bigint_col", Types.BIGINT, false, false, false, true, false, true),
                new ShardingSphereColumn("varchar_col", Types.VARCHAR, false, false, false, true, false, true));
        List<Object> rows = Arrays.asList(new BigDecimal("7.5"), new BigDecimal("8.25"), null, 5L, "foo_value");
        YamlRowStatisticsSwapper swapper = new YamlRowStatisticsSwapper(columns);
        YamlRowStatistics actual = swapper.swapToYamlConfiguration(new RowStatistics("foo_unique_key", rows));
        assertThat(actual.getRows(), contains("7.5", "8.25", null, 5L, "foo_value"));
    }
    
    @Test
    void assertSwapToObjectWithNullRows() {
        YamlRowStatistics yamlConfig = new YamlRowStatistics();
        yamlConfig.setRows(null);
        assertThat(new YamlRowStatisticsSwapper(Collections.emptyList()).swapToObject(yamlConfig).getRows(), is(empty()));
    }
    
    @Test
    void assertConvertDataTypesWhenSwappingToObject() {
        List<ShardingSphereColumn> columns = Arrays.asList(
                new ShardingSphereColumn("decimal_col", Types.DECIMAL, false, false, false, true, false, true),
                new ShardingSphereColumn("numeric_col", Types.NUMERIC, false, false, false, true, false, true),
                new ShardingSphereColumn("bigint_integer_col", Types.BIGINT, false, false, false, true, false, true),
                new ShardingSphereColumn("bigint_long_col", Types.BIGINT, false, false, false, true, false, true),
                new ShardingSphereColumn("real_double_col", Types.REAL, false, false, false, true, false, true),
                new ShardingSphereColumn("real_float_col", Types.REAL, false, false, false, true, false, true),
                new ShardingSphereColumn("float_col", Types.FLOAT, false, false, false, true, false, true),
                new ShardingSphereColumn("double_col", Types.DOUBLE, false, false, false, true, false, true),
                new ShardingSphereColumn("varchar_col", Types.VARCHAR, false, false, false, true, false, true),
                new ShardingSphereColumn("decimal_null", Types.DECIMAL, false, false, false, true, false, true));
        YamlRowStatistics yamlConfig = new YamlRowStatistics();
        yamlConfig.setUniqueKey("foo_unique_key");
        yamlConfig.setRows(Arrays.asList("1.5", new BigDecimal("2.5"), 3, 4L, 5.5D, 6.5F, 7.5D, "8.5", "foo_text", null));
        YamlRowStatisticsSwapper swapper = new YamlRowStatisticsSwapper(columns);
        RowStatistics actual = swapper.swapToObject(yamlConfig);
        assertThat(actual.getUniqueKey(), is("foo_unique_key"));
        assertThat(actual.getRows(), contains(new BigDecimal("1.5"), new BigDecimal("2.5"), 3L, 4L, 5.5F, 6.5F, 7.5D, 8.5D, "foo_text", null));
    }
}
