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
import com.dangdang.ddframe.rdb.sharding.config.common.internal.ConfigUtil;
import com.dangdang.ddframe.rdb.sharding.exception.SQLParserException;
import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import groovy.lang.Closure;
import groovy.util.Expando;
import lombok.RequiredArgsConstructor;
import org.slf4j.LoggerFactory;

/**
 * 基于闭包的数据源划分算法.
 * 
 * @author gaohongtao
 */
@RequiredArgsConstructor
public class ClosureShardingAlgorithm implements MultipleKeysShardingAlgorithm {
    
    private final Closure<String> closure;
    
    @Override
    public Collection<String> doSharding(final Collection<String> availableTargetNames, final Collection<ShardingValue<?>> shardingValues) {
        List<List<Comparable>> parametersDim = new ArrayList<>();
        List<String> columnNameList = new ArrayList<>(shardingValues.size());
        for (ShardingValue<?> each : shardingValues) {
            columnNameList.add(each.getColumnName());
            switch (each.getType()) {
                case SINGLE:
                    parametersDim.add(Lists.newArrayList((Comparable) each.getValue()));
                    break;
                case LIST:
                    parametersDim.add(new ArrayList<Comparable>(each.getValues()));
                    break;
                case RANGE:
                    throw new UnsupportedOperationException("Config file does not support BETWEEN, please use Java API Config");
                default:
                    throw new UnsupportedOperationException();
            }
        }
        
        List<List<Comparable>> paramScenario = ConfigUtil.descartes(parametersDim);
        
        List<String> result = new ArrayList<>();
        Set<String> availableTargetNameSet = new HashSet<>(availableTargetNames);
        for (List<Comparable> each : paramScenario) {
            Closure newClosure = closure.rehydrate(new Expando(), null, null);
            newClosure.setResolveStrategy(Closure.DELEGATE_ONLY);
            newClosure.setProperty("log", LoggerFactory.getLogger(Joiner.on(".").join("com.dangdang.ddframe.rdb.sharding.configFile", each)));
            for (int i = 0; i < each.size(); i++) {
                newClosure.setProperty(columnNameList.get(i), new ShardingValueWrapper(each.get(i)));
            }
            //jvm动态调用指令将会忽略Closure返回值的泛型,故此处采用两行代码进行数据转换.
            Object algorithmResult = newClosure.call();
            if (null == algorithmResult) {
                throw new SQLParserException("No table route");
            }
            
            if (algorithmResult instanceof ArrayList) {
                for (Object innerResult : (ArrayList) algorithmResult) {
                    result.add(filterResult(innerResult, availableTargetNameSet));
                }
            } else {
                result.add(filterResult(algorithmResult, availableTargetNameSet));
            }
    
        }
        return result;
    }
    
    private String filterResult(final Object algorithmResult, final Set<String> availableTargetNameSet) {
        String stringAlgorithmResult = algorithmResult.toString();
        if (!availableTargetNameSet.contains(stringAlgorithmResult)) {
            throw new SQLParserException("Routing target %s does not contain in availableTargetNames %s", stringAlgorithmResult, availableTargetNameSet);
        }
        return stringAlgorithmResult;
    }
    
}
