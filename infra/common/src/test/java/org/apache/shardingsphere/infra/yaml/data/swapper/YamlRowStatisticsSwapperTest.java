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
import static org.hamcrest.Matchers.nullValue;

class YamlRowStatisticsSwapperTest {
    
    @Test
    void assertSwapToYamlConfigurationWithNullRows() {
        YamlRowStatisticsSwapper swapper = new YamlRowStatisticsSwapper(Collections.emptyList());
        YamlRowStatistics actual = swapper.swapToYamlConfiguration(new RowStatistics("uk", null));
        assertThat(actual.getRows(), is(empty()));
        assertThat(actual.getUniqueKey(), is("uk"));
    }
    
    @Test
    void assertConvertSpecialTypesWhenSwappingToYaml() {
        List<ShardingSphereColumn> columns = Arrays.asList(
                new ShardingSphereColumn("decimal_col", Types.DECIMAL, false, false,"decimal", false, true, false, true),
                new ShardingSphereColumn("bigint_col", Types.BIGINT, false, false,"bigint", false, true, false, true),
                new ShardingSphereColumn("decimal_null", Types.DECIMAL, false, false,"decimal", false, true, false, true),
                new ShardingSphereColumn("varchar_col", Types.VARCHAR, false, false,"varchar", false, true, false, true));
        List<Object> rows = Arrays.asList(null, 5L, new BigDecimal("7.5"), "raw");
        YamlRowStatisticsSwapper swapper = new YamlRowStatisticsSwapper(columns);
        assertThat(swapper.swapToYamlConfiguration(new RowStatistics("uk", rows)).getRows(), contains(nullValue(), is("5"), is("7.5"), is("raw")));
    }
    
    @Test
    void assertSwapToObjectWithNullRows() {
        assertThat(new YamlRowStatisticsSwapper(Collections.emptyList()).swapToObject(new YamlRowStatistics()).getRows(), is(empty()));
    }
    
    @Test
    void assertSwapToObjectWithEmptyRows() {
        List<ShardingSphereColumn> columns = Collections.singletonList(new ShardingSphereColumn("col", Types.VARCHAR, false, false,"varchar", false, true, false, true));
        YamlRowStatistics yamlConfig = new YamlRowStatistics();
        yamlConfig.setRows(Collections.emptyList());
        assertThat(new YamlRowStatisticsSwapper(columns).swapToObject(yamlConfig).getRows(), is(empty()));
    }
    
    @Test
    void assertConvertDataTypesWhenSwappingToObject() {
        List<ShardingSphereColumn> columns = Arrays.asList(
                new ShardingSphereColumn("decimal_col", Types.DECIMAL, false, false,"decimal", false, true, false, true),
                new ShardingSphereColumn("bigint_col", Types.BIGINT, false, false,"bigint", false, true, false, true),
                new ShardingSphereColumn("real_col", Types.REAL, false, false,"real", false, true, false, true),
                new ShardingSphereColumn("float_col", Types.FLOAT, false, false,"float", false, true, false, true),
                new ShardingSphereColumn("varchar_col", Types.VARCHAR, false, false,"varchar", false, true, false, true),
                new ShardingSphereColumn("decimal_null", Types.DECIMAL, false, false,"decimal", false, true, false, true));
        YamlRowStatistics yamlConfig = new YamlRowStatistics();
        yamlConfig.setUniqueKey("uk");
        yamlConfig.setRows(Arrays.asList("1.5", "2", "3.3", "4.4", "text", null));
        YamlRowStatisticsSwapper swapper = new YamlRowStatisticsSwapper(columns);
        RowStatistics actual = swapper.swapToObject(yamlConfig);
        assertThat(actual.getUniqueKey(), is("uk"));
        assertThat(actual.getRows(), contains(new BigDecimal("1.5"), 2L, Float.parseFloat("3.3"), Float.parseFloat("4.4"), "text", null));
    }
}
