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
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.math.BigDecimal;
import java.sql.Types;
import java.util.Collections;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertTrue;

class YamlRowStatisticsSwapperTest {
    
    @Test
    void assertSwapToYamlConfigurationWithNullRows() {
        YamlRowStatisticsSwapper swapper = new YamlRowStatisticsSwapper(Collections.emptyList());
        YamlRowStatistics actual = swapper.swapToYamlConfiguration(new RowStatistics("foo_unique_key", null));
        assertTrue(actual.getRows().isEmpty());
        assertThat(actual.getUniqueKey(), is("foo_unique_key"));
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("getSwapToYamlConfigurationArguments")
    void assertSwapToYamlConfigurationWithDataType(final String name, final int dataType, final Object input, final Object expected) {
        ShardingSphereColumn column = new ShardingSphereColumn("foo_col", dataType, false, false, false, true, false, true);
        YamlRowStatistics actual = new YamlRowStatisticsSwapper(Collections.singletonList(column))
                .swapToYamlConfiguration(new RowStatistics("foo_unique_key", Collections.singletonList(input)));
        assertThat(actual.getRows(), contains(expected));
    }
    
    private static Stream<Arguments> getSwapToYamlConfigurationArguments() {
        return Stream.of(
                Arguments.of("decimal", Types.DECIMAL, new BigDecimal("7.5"), "7.5"),
                Arguments.of("numeric", Types.NUMERIC, new BigDecimal("8.25"), "8.25"),
                Arguments.of("null decimal", Types.DECIMAL, null, null),
                Arguments.of("bigint", Types.BIGINT, 5L, 5L),
                Arguments.of("varchar", Types.VARCHAR, "foo_value", "foo_value"));
    }
    
    @Test
    void assertSwapToObjectWithNullRows() {
        YamlRowStatistics yamlConfig = new YamlRowStatistics();
        yamlConfig.setUniqueKey("foo_unique_key");
        yamlConfig.setRows(null);
        RowStatistics actual = new YamlRowStatisticsSwapper(Collections.emptyList()).swapToObject(yamlConfig);
        assertTrue(actual.getRows().isEmpty());
        assertThat(actual.getUniqueKey(), is("foo_unique_key"));
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("getSwapToObjectArguments")
    void assertSwapToObjectWithDataType(final String name, final int dataType, final Object input, final Object expected) {
        ShardingSphereColumn column = new ShardingSphereColumn("foo_col", dataType, false, false, false, true, false, true);
        YamlRowStatistics yamlConfig = new YamlRowStatistics();
        yamlConfig.setRows(Collections.singletonList(input));
        RowStatistics actual = new YamlRowStatisticsSwapper(Collections.singletonList(column)).swapToObject(yamlConfig);
        assertThat(actual.getRows(), contains(expected));
    }
    
    private static Stream<Arguments> getSwapToObjectArguments() {
        return Stream.of(
                Arguments.of("decimal string", Types.DECIMAL, "1.5", new BigDecimal("1.5")),
                Arguments.of("numeric string", Types.NUMERIC, "2.5", new BigDecimal("2.5")),
                Arguments.of("numeric BigDecimal", Types.NUMERIC, new BigDecimal("3.5"), new BigDecimal("3.5")),
                Arguments.of("bigint integer", Types.BIGINT, 3, 3L),
                Arguments.of("bigint long", Types.BIGINT, 4L, 4L),
                Arguments.of("real double", Types.REAL, 5.5D, 5.5F),
                Arguments.of("real float", Types.REAL, 6.5F, 6.5F),
                Arguments.of("float double", Types.FLOAT, 7.5D, 7.5D),
                Arguments.of("double string", Types.DOUBLE, "8.5", 8.5D),
                Arguments.of("varchar", Types.VARCHAR, "foo_text", "foo_text"),
                Arguments.of("null decimal", Types.DECIMAL, null, null));
    }
}
