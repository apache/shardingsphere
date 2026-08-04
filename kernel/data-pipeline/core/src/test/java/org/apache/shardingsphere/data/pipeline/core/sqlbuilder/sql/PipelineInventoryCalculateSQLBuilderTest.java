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

package org.apache.shardingsphere.data.pipeline.core.sqlbuilder.sql;

import org.apache.shardingsphere.data.pipeline.core.ingest.dumper.inventory.query.Range;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.infra.metadata.database.schema.QualifiedTable;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

class PipelineInventoryCalculateSQLBuilderTest {
    
    private static final Collection<String> COLUMN_NAMES = Arrays.asList("order_id", "user_id", "status");
    
    private static final List<String> UNIQUE_KEYS = Arrays.asList("order_id", "status");
    
    private static final List<String> SHARDING_COLUMNS_NAMES = Collections.singletonList("user_id");
    
    private final PipelineInventoryCalculateSQLBuilder sqlBuilder = new PipelineInventoryCalculateSQLBuilder(TypedSPILoader.getService(DatabaseType.class, "FIXTURE"));
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("assertBuildRangeQueryOrderingSQLArguments")
    void assertBuildRangeQueryOrderingSQL(final String name, final Range<Integer> range, final boolean pageQuery, final String expectedSQL) {
        assertThat(name, sqlBuilder.buildRangeQueryOrderingSQL(new QualifiedTable(null, "t_order"), COLUMN_NAMES, UNIQUE_KEYS, range, pageQuery, SHARDING_COLUMNS_NAMES), is(expectedSQL));
    }
    
    private static Stream<Arguments> assertBuildRangeQueryOrderingSQLArguments() {
        return Stream.of(
                Arguments.of("closed range with page query", Range.closed(1, 5), true,
                        "SELECT order_id,user_id,status FROM t_order WHERE order_id>=? AND order_id<=? ORDER BY order_id ASC, status ASC, user_id ASC LIMIT ?"),
                Arguments.of("open-closed range with page query", Range.openClosed(1, 5), true,
                        "SELECT order_id,user_id,status FROM t_order WHERE order_id>? AND order_id<=? ORDER BY order_id ASC, status ASC, user_id ASC LIMIT ?"),
                Arguments.of("lower bound only with page query", Range.openClosed(1, null), true,
                        "SELECT order_id,user_id,status FROM t_order WHERE order_id>? ORDER BY order_id ASC, status ASC, user_id ASC LIMIT ?"),
                Arguments.of("upper bound only with page query", Range.openClosed(null, 5), true,
                        "SELECT order_id,user_id,status FROM t_order WHERE order_id<=? ORDER BY order_id ASC, status ASC, user_id ASC LIMIT ?"),
                Arguments.of("unbounded range with page query", Range.openClosed(null, null), true,
                        "SELECT order_id,user_id,status FROM t_order ORDER BY order_id ASC, status ASC, user_id ASC LIMIT ?"),
                Arguments.of("closed range without page query", Range.closed(1, 5), false,
                        "SELECT order_id,user_id,status FROM t_order WHERE order_id>=? AND order_id<=? ORDER BY order_id ASC, status ASC, user_id ASC"),
                Arguments.of("open-closed range without page query", Range.openClosed(1, 5), false,
                        "SELECT order_id,user_id,status FROM t_order WHERE order_id>? AND order_id<=? ORDER BY order_id ASC, status ASC, user_id ASC"),
                Arguments.of("lower bound only without page query", Range.openClosed(1, null), false,
                        "SELECT order_id,user_id,status FROM t_order WHERE order_id>? ORDER BY order_id ASC, status ASC, user_id ASC"),
                Arguments.of("upper bound only without page query", Range.openClosed(null, 5), false,
                        "SELECT order_id,user_id,status FROM t_order WHERE order_id<=? ORDER BY order_id ASC, status ASC, user_id ASC"),
                Arguments.of("unbounded range without page query", Range.openClosed(null, null), false,
                        "SELECT order_id,user_id,status FROM t_order ORDER BY order_id ASC, status ASC, user_id ASC"));
    }
    
    @Test
    void assertBuildPointQuerySQLWithoutShardingColumns() {
        assertThat(sqlBuilder.buildPointQuerySQL(new QualifiedTable(null, "t_order"), COLUMN_NAMES, UNIQUE_KEYS, Collections.emptyList()),
                is("SELECT order_id,user_id,status FROM t_order WHERE order_id=? AND status=?"));
    }
    
    @Test
    void assertBuildPointQuerySQLWithShardingColumns() {
        assertThat(sqlBuilder.buildPointQuerySQL(new QualifiedTable(null, "t_order"), COLUMN_NAMES, UNIQUE_KEYS, Collections.singletonList("user_id")),
                is("SELECT order_id,user_id,status FROM t_order WHERE order_id=? AND status=? AND user_id=?"));
    }
}
