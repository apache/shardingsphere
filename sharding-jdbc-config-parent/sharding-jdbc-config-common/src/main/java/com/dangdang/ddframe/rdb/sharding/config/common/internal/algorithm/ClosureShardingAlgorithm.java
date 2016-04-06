/*
 * Copyright 1999-2015 dangdang.com.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package com.dangdang.ddframe.rdb.sharding.config.common.internal.algorithm;

import com.dangdang.ddframe.rdb.sharding.api.ShardingValue;
import com.dangdang.ddframe.rdb.sharding.api.strategy.common.MultipleKeysShardingAlgorithm;
import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Sets;
import groovy.lang.Binding;
import groovy.lang.Closure;
import groovy.lang.GroovyShell;
import groovy.util.Expando;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * 基于闭包的数据源划分算法.
 * 
 * @author gaohongtao
 */
public class ClosureShardingAlgorithm implements MultipleKeysShardingAlgorithm {
    
    private final Closure<?> closureTemplate;
    
    public ClosureShardingAlgorithm(final String expression, final String logRoot) {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(expression));
        Preconditions.checkArgument(!Strings.isNullOrEmpty(logRoot));
        Binding binding = new Binding();
        binding.setVariable("log", LoggerFactory.getLogger(Joiner.on(".").join("com.dangdang.ddframe.rdb.sharding.configFile", logRoot.trim())));
        closureTemplate = (Closure) new GroovyShell(binding).evaluate(Joiner.on("").join("{it -> \"", expression.trim(), "\"}"));
    }
    
    @Override
    public Collection<String> doSharding(final Collection<String> availableTargetNames, final Collection<ShardingValue<?>> shardingValues) {
        List<Set<Comparable>> valuesDim = new ArrayList<>();
        List<String> columnNames = new ArrayList<>(shardingValues.size());
        for (ShardingValue<?> each : shardingValues) {
            columnNames.add(each.getColumnName());
            switch (each.getType()) {
                case SINGLE:
                    valuesDim.add(Sets.newHashSet((Comparable) each.getValue()));
                    break;
                case LIST:
                    valuesDim.add(Sets.<Comparable>newHashSet(each.getValues()));
                    break;
                case RANGE:
                    throw new UnsupportedOperationException("Inline expression does not support BETWEEN, please use Java API Config");
                default:
                    throw new UnsupportedOperationException(each.getType().name());
            }
        }
        Set<List<Comparable>> cartesianValues = Sets.cartesianProduct(valuesDim);
        List<String> result = new ArrayList<>(cartesianValues.size());
        for (List<Comparable> each : cartesianValues) {
            result.add(cloneClosure(columnNames, each).call().toString());
        }
        return result;
    }
    
    private Closure<?> cloneClosure(final List<String> columnNames, final List<Comparable> values) {
        Closure<?> result = closureTemplate.rehydrate(new Expando(), null, null);
        result.setResolveStrategy(Closure.DELEGATE_ONLY);
        result.setProperty("log", closureTemplate.getProperty("log"));
        for (int i = 0; i < values.size(); i++) {
            result.setProperty(columnNames.get(i), new ShardingValueWrapper(values.get(i)));
        }
        return result;
    }
}
