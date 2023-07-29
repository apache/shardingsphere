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
        String sql = "select count(*) from t_hint_test where user_id = (select t_hint_test from t_hint_test where user_id in (?,?,?)) ";
        ShardingRouteAssert.assertRoute(sql, Arrays.asList(1, 3, 5));
        hintManager.close();
    }
    
    private static class TestCaseArgumentsProvider implements ArgumentsProvider {
        
        @Override
        public Stream<? extends Arguments> provideArguments(final ExtensionContext extensionContext) {
            return Stream.of(
                    Arguments.of("oneTableDifferentConditionWithFederation",
                            "select (select max(id) from t_order b where b.user_id =? ) from t_order a where user_id = ? ", Arrays.asList(3, 2)),
                    Arguments.of("oneTableSameConditionWithFederation",
                            "select (select max(id) from t_order b where b.user_id = ? and b.user_id = a.user_id) from t_order a where user_id = ? ", Arrays.asList(1, 1)),
                    Arguments.of("bindingTableWithFederation",
                            "select (select max(id) from t_order_item b where b.user_id = ?) from t_order a where user_id = ? ", Arrays.asList(1, 1)),
                    Arguments.of("notShardingTable",
                            "select (select max(id) from t_category b where b.id = ?) from t_category a where id = ? ", Arrays.asList(1, 1)),
                    Arguments.of("bindingTableWithDifferentValueWithFederation",
                            "select (select max(id) from t_order_item b where b.user_id = ? ) from t_order a where user_id = ? ", Arrays.asList(2, 3)),
                    Arguments.of("twoTableWithDifferentOperatorWithFederation",
                            "select (select max(id) from t_order_item b where b.user_id in(?,?)) from t_order a where user_id = ? ", Arrays.asList(1, 2, 1)),
                    Arguments.of("twoTableWithInWithFederation",
                            "select (select max(id) from t_order_item b where b.user_id in(?,?)) from t_order a where user_id in(?,?) ", Arrays.asList(1, 2, 1, 3)),
                    Arguments.of("subqueryInSubqueryError",
                            "select (select status from t_order b where b.user_id =? and status = (select status from t_order b where b.user_id =?)) as c from t_order a "
                                    + "where status = (select status from t_order b where b.user_id =? and status = (select status from t_order b where b.user_id =?))",
                            Arrays.asList(11, 2, 1, 1)),
                    Arguments.of("subqueryInSubquery",
                            "select (select status from t_order b where b.user_id =? and status = (select status from t_order b where b.user_id =?)) as c from t_order a "
                                    + "where status = (select status from t_order b where b.user_id =? and status = (select status from t_order b where b.user_id =?))",
                            Arrays.asList(1, 1, 1, 1)),
                    Arguments.of("subqueryInFromError",
                            "select b.status from t_order b join (select user_id,status from t_order b where b.user_id =?) c on b.user_id = c.user_id where b.user_id =? ", Arrays.asList(11, 1)),
                    Arguments.of("subqueryInFrom",
                            "select b.status from t_order b join (select user_id,status from t_order b where b.user_id =?) c on b.user_id = c.user_id where b.user_id =? ", Arrays.asList(1, 1)),
                    Arguments.of("subqueryForAggregation",
                            "select count(*) from t_order where user_id = (select user_id from t_order where user_id =?) ", Collections.singletonList(1)),
                    Arguments.of("subqueryForBinding",
                            "select count(*) from t_order where user_id = (select user_id from t_order_item where user_id =?) ", Collections.singletonList(1)),
                    Arguments.of("subqueryWithOneInstance", "select count(*) from t_order where user_id =?", Collections.singletonList(1)));
        }
    }
}
