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

import org.apache.shardingsphere.sharding.route.engine.type.standard.assertion.ShardingRouteAssert;
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

class SQLRouteTest {
    
    @ParameterizedTest(name = "{0}")
    @ArgumentsSource(TestCaseArgumentsProvider.class)
    void assertRoute(final String name, final String sql, final List<Object> params) {
        // TODO add assertion for ShardingRouteAssert.assertRoute
        ShardingRouteAssert.assertRoute(sql, params);
    }
    
    private static final class TestCaseArgumentsProvider implements ArgumentsProvider {
        
        @Override
        public Stream<? extends Arguments> provideArguments(final ParameterDeclarations parameters, final ExtensionContext context) {
            return Stream.of(
                    Arguments.of("noTableUnicastRandomDataSource", "SELECT 1, 1 + 2", Collections.singletonList(1)),
                    Arguments.of("withBroadcastTable", "SELECT a.user_id, status from t_order_item a join t_product b on a.product_id = b.product_id where a.user_id = ?",
                            Collections.singletonList(1)),
                    Arguments.of("allBindingWithBroadcastTable",
                            "SELECT a.user_id, a.status from t_order a join t_order_item b on a.order_id = b.order_id join t_product c on b.product_id = c.product_id where a.user_id = ?",
                            Collections.singletonList(1)),
                    Arguments.of("complexTableWithBroadcastTable",
                            "SELECT a.user_id, status from t_order a join t_user b on a.user_id = b.user_id join t_product c on a.product_id = c.product_id where a.user_id = ? and b.user_id =?",
                            Arrays.asList(1, 1)),
                    Arguments.of("insertTable", "INSERT INTO t_order (order_id, user_id) VALUES (?, ?)", Arrays.asList(1, 1)));
        }
    }
}
