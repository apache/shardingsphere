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

package org.apache.shardingsphere.sharding.route.engine.type.standard;

import org.apache.shardingsphere.infra.hint.HintManager;
import org.apache.shardingsphere.sharding.route.engine.type.standard.assertion.ShardingRouteAssert;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.provider.ArgumentsSource;
import org.junit.jupiter.params.support.ParameterDeclarations;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

// TODO add assertion for ShardingRouteAssert.assertRoute
class SubqueryRouteTest {
    
    @ParameterizedTest(name = "{0}")
    @ArgumentsSource(TestCaseArgumentsProvider.class)
    void assertRoute(final String name, final String sql, final List<Object> params) {
        ShardingRouteAssert.assertRoute(sql, params);
    }
    
    @Test
    void assertRouteWithHint() {
        HintManager hintManager = HintManager.getInstance();
        hintManager.addDatabaseShardingValue("t_hint_test", 1);
        hintManager.addTableShardingValue("t_hint_test", 1);
        String sql = "SELECT COUNT(*) FROM t_hint_test WHERE user_id = (SELECT user_id FROM t_hint_test WHERE user_id IN (?,?,?)) ";
        ShardingRouteAssert.assertRoute(sql, Arrays.asList(1, 3, 5));
        hintManager.close();
    }
    
    private static final class TestCaseArgumentsProvider implements ArgumentsProvider {
        
        @Override
        public Stream<? extends Arguments> provideArguments(final ParameterDeclarations parameters, final ExtensionContext context) {
            return Stream.of(
                    Arguments.of("oneTableDifferentConditionWithFederation",
                            "SELECT (SELECT MAX(id) FROM t_order b WHERE b.user_id =? ) FROM t_order a WHERE user_id = ? ", Arrays.asList(3, 2)),
                    Arguments.of("oneTableSameConditionWithFederation",
                            "SELECT (SELECT MAX(id) FROM t_order b WHERE b.user_id = ? AND b.user_id = a.user_id) FROM t_order a WHERE user_id = ? ", Arrays.asList(1, 1)),
                    Arguments.of("bindingTableWithFederation",
                            "SELECT (SELECT MAX(id) FROM t_order_item b WHERE b.user_id = ?) FROM t_order a WHERE user_id = ? ", Arrays.asList(1, 1)),
                    Arguments.of("notShardingTable",
                            "SELECT (SELECT MAX(id) FROM t_category b WHERE b.id = ?) FROM t_category a WHERE id = ? ", Arrays.asList(1, 1)),
                    Arguments.of("bindingTableWithDifferentValueWithFederation",
                            "SELECT (SELECT MAX(id) FROM t_order_item b WHERE b.user_id = ? ) FROM t_order a WHERE user_id = ? ", Arrays.asList(2, 3)),
                    Arguments.of("twoTableWithDifferentOperatorWithFederation",
                            "SELECT (SELECT MAX(id) FROM t_order_item b WHERE b.user_id in(?,?)) FROM t_order a WHERE user_id = ? ", Arrays.asList(1, 2, 1)),
                    Arguments.of("twoTableWithInWithFederation",
                            "SELECT (SELECT MAX(id) FROM t_order_item b WHERE b.user_id in(?,?)) FROM t_order a WHERE user_id in(?,?) ", Arrays.asList(1, 2, 1, 3)),
                    Arguments.of("subqueryInSubqueryError",
                            "SELECT (SELECT status FROM t_order b WHERE b.user_id =? AND status = (SELECT status FROM t_order b WHERE b.user_id =?)) as c FROM t_order a "
                                    + "WHERE status = (SELECT status FROM t_order b WHERE b.user_id =? AND status = (SELECT status FROM t_order b WHERE b.user_id =?))",
                            Arrays.asList(11, 2, 1, 1)),
                    Arguments.of("subqueryInSubquery",
                            "SELECT (SELECT status FROM t_order b WHERE b.user_id =? AND status = (SELECT status FROM t_order b WHERE b.user_id =?)) as c FROM t_order a "
                                    + "WHERE status = (SELECT status FROM t_order b WHERE b.user_id =? AND status = (SELECT status FROM t_order b WHERE b.user_id =?))",
                            Arrays.asList(1, 1, 1, 1)),
                    Arguments.of("subqueryInFromError",
                            "SELECT b.status FROM t_order b join (SELECT user_id,status FROM t_order b WHERE b.user_id =?) c ON b.user_id = c.user_id WHERE b.user_id =? ", Arrays.asList(11, 1)),
                    Arguments.of("subqueryInFrom",
                            "SELECT b.status FROM t_order b join (SELECT user_id,status FROM t_order b WHERE b.user_id =?) c ON b.user_id = c.user_id WHERE b.user_id =? ", Arrays.asList(1, 1)),
                    Arguments.of("subqueryForAggregation",
                            "SELECT count(*) FROM t_order WHERE user_id = (SELECT user_id FROM t_order WHERE user_id =?) ", Collections.singletonList(1)),
                    Arguments.of("subqueryForBinding",
                            "SELECT count(*) FROM t_order WHERE user_id = (SELECT user_id FROM t_order_item WHERE user_id =?) ", Collections.singletonList(1)),
                    Arguments.of("subqueryWithOneInstance", "SELECT COUNT(*) FROM t_order WHERE user_id =?", Collections.singletonList(1)));
        }
    }
}
