/**
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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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

/**
 * 基于闭包的数据源划分算法.
 * 
 * @author gaohongtao
 */
public class ClosureShardingAlgorithm implements MultipleKeysShardingAlgorithm {
    
    private final Closure<String> closure;
    
    @SuppressWarnings(value = "unchecked")
    public ClosureShardingAlgorithm(final String scriptText, final String logRoot) {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(scriptText));
        Preconditions.checkArgument(!Strings.isNullOrEmpty(logRoot));
        Binding binding = new Binding();
        binding.setVariable("log", LoggerFactory.getLogger(Joiner.on(".").join("com.dangdang.ddframe.rdb.sharding.configFile", logRoot.trim())));
        closure = (Closure) new GroovyShell(binding).evaluate(Joiner.on("").join("{it -> \"", scriptText.trim(), "\"}"));
    }
    
    @Override
    public Collection<String> doSharding(final Collection<String> availableTargetNames, final Collection<ShardingValue<?>> shardingValues) {
        List<Set<Comparable>> parametersDim = new ArrayList<>();
        List<String> columnNameList = new ArrayList<>(shardingValues.size());
        for (ShardingValue<?> each : shardingValues) {
            columnNameList.add(each.getColumnName());
            switch (each.getType()) {
                case SINGLE:
                    parametersDim.add(Sets.newHashSet((Comparable) each.getValue()));
                    break;
                case LIST:
                    parametersDim.add(Sets.<Comparable>newHashSet(each.getValues()));
                    break;
                case RANGE:
                    throw new UnsupportedOperationException("Config file does not support BETWEEN, please use Java API Config");
                default:
                    throw new UnsupportedOperationException();
            }
        }
        List<String> result = new ArrayList<>();
        Set<String> availableTargetNameSet = new HashSet<>(availableTargetNames);
        for (List<Comparable> each : Sets.cartesianProduct(parametersDim)) {
            Closure<String> newClosure = closure.rehydrate(new Expando(), null, null);
            newClosure.setResolveStrategy(Closure.DELEGATE_ONLY);
            newClosure.setProperty("log", closure.getProperty("log"));
            for (int i = 0; i < each.size(); i++) {
                newClosure.setProperty(columnNameList.get(i), new ShardingValueWrapper(each.get(i)));
            }
            Object algorithmResult = newClosure.call();
            Preconditions.checkState(availableTargetNameSet.contains(algorithmResult.toString()));
            result.add(algorithmResult.toString());
        }
        return result;
    }
}
