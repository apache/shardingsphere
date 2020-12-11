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

package org.apache.shardingsphere.sharding.algorithm.sharding.complex;

import com.google.common.base.Preconditions;
import groovy.lang.Closure;
import groovy.util.Expando;
import org.apache.shardingsphere.sharding.algorithm.sharding.inline.InlineExpressionParser;
import org.apache.shardingsphere.sharding.api.sharding.complex.ComplexKeysShardingAlgorithm;
import org.apache.shardingsphere.sharding.api.sharding.complex.ComplexKeysShardingValue;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;

/**
 * Complex Inline sharding algorithm.
 *
 * @author likly
 * @version 5.0.0
 * @since 5.0.0
 */
public class ComplexInlineShardingAlgorithm implements ComplexKeysShardingAlgorithm<Comparable<?>> {

    private static final String ALGORITHM_EXPRESSION_KEY = "algorithm-expression";

    private static final String SHARING_COLUMNS_KEY = "sharding-columns";

    private static final String ALLOW_RANGE_QUERY_KEY = "allow-range-query-with-inline-sharding";

    private boolean allowRangeQuery;

    private String[] shardingColumns;

    private String algorithmExpression;

    private final Properties props = new Properties();

    @Override
    public Collection<String> doSharding(final Collection<String> availableTargetNames, final ComplexKeysShardingValue<Comparable<?>> shardingValue) {

        if (!shardingValue.getColumnNameAndRangeValuesMap().isEmpty()) {
            if (isAllowRangeQuery()) {
                return availableTargetNames;
            }

            throw new UnsupportedOperationException("Since the property of `" + ALLOW_RANGE_QUERY_KEY + "` is false, inline sharding algorithm can not tackle with range query.");
        }

        Map<String, Collection<Comparable<?>>> columnNameAndShardingValuesMap = shardingValue.getColumnNameAndShardingValuesMap();

        if (shardingColumns.length > 0 && shardingColumns.length != columnNameAndShardingValuesMap.size()) {
            throw new IllegalArgumentException("complex inline need " + shardingColumns.length + " sharing columns, but only found " + columnNameAndShardingValuesMap.size());
        }

        Collection<Map<String, Comparable<?>>> combine = combine(columnNameAndShardingValuesMap);

        return combine.stream()
                .map(this::doSharding)
                .collect(Collectors.toList());

    }

    private String doSharding(final Map<String, Comparable<?>> shardingValues) {
        Closure<?> closure = createClosure();
        for (Map.Entry<String, Comparable<?>> entry : shardingValues.entrySet()) {
            closure.setProperty(entry.getKey(), entry.getValue());
        }
        return closure.call().toString();
    }

    @Override
    public void init() {
        String expression = props.getProperty(ALGORITHM_EXPRESSION_KEY);
        Preconditions.checkNotNull(expression, "Inline sharding algorithm expression cannot be null.");
        algorithmExpression = InlineExpressionParser.handlePlaceHolder(expression.trim());
        initShardingColumns(props.getProperty(SHARING_COLUMNS_KEY, ""));
        allowRangeQuery = Boolean.parseBoolean(props.getOrDefault(ALLOW_RANGE_QUERY_KEY, Boolean.FALSE.toString()).toString());
    }

    private void initShardingColumns(final String shardingColumns) {
        if (shardingColumns.length() == 0) {
            this.shardingColumns = new String[0];
            return;
        }
        this.shardingColumns = shardingColumns.split(",");
    }

    private boolean isAllowRangeQuery() {
        return allowRangeQuery;
    }

    private Closure<?> createClosure() {
        Closure<?> result = new InlineExpressionParser(algorithmExpression).evaluateClosure().rehydrate(new Expando(), null, null);
        result.setResolveStrategy(Closure.DELEGATE_ONLY);
        return result;
    }

    @Override
    public String getType() {
        return "COMPLEX_INLINE";
    }

    @Override
    public Properties getProps() {
        return props;
    }

    @Override
    public void setProps(final Properties props) {
        this.props.clear();
        this.props.putAll(props);
    }

    private static <K, V> Collection<Map<K, V>> combine(final Map<K, Collection<V>> map) {
        Collection<Map<K, V>> result = new ArrayList<>();
        for (Map.Entry<K, Collection<V>> entry : map.entrySet()) {
            if (result.isEmpty()) {
                for (V value : entry.getValue()) {
                    Map<K, V> item = new HashMap<>();
                    item.put(entry.getKey(), value);
                    result.add(item);
                }
            } else {
                Collection<Map<K, V>> list = new ArrayList<>();
                for (Map<K, V> loop : result) {
                    for (V value : entry.getValue()) {
                        Map<K, V> item = new HashMap<>();
                        item.put(entry.getKey(), value);
                        item.putAll(loop);
                        list.add(item);
                    }
                }
                result = list;
            }
        }
        return result;
    }

}
