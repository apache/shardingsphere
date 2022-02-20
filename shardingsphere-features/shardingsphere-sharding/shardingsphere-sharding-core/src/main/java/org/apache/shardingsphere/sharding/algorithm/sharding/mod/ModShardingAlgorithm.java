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

package org.apache.shardingsphere.sharding.algorithm.sharding.mod;

import com.google.common.base.Preconditions;
import com.google.common.primitives.Longs;
import lombok.Getter;
import lombok.Setter;
import org.apache.shardingsphere.sharding.api.sharding.ShardingAutoTableAlgorithm;
import org.apache.shardingsphere.sharding.api.sharding.standard.PreciseShardingValue;
import org.apache.shardingsphere.sharding.api.sharding.standard.RangeShardingValue;
import org.apache.shardingsphere.sharding.api.sharding.standard.StandardShardingAlgorithm;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Modulo sharding algorithm.
 */
@Getter
@Setter
public final class ModShardingAlgorithm implements StandardShardingAlgorithm<Comparable<?>>, ShardingAutoTableAlgorithm {
    
    private static final String SHARDING_COUNT_KEY = "sharding-count";
    
    private static final Pattern SUFFIX_PATTERN = Pattern.compile(".*\\D+(\\d+)$");
    
    private Properties props = new Properties();
    
    private int shardingCount;
    
    @Override
    public void init() {
        shardingCount = getShardingCount();
    }
    
    private int getShardingCount() {
        Preconditions.checkArgument(props.containsKey(SHARDING_COUNT_KEY), "Sharding count cannot be null.");
        return Integer.parseInt(props.get(SHARDING_COUNT_KEY).toString());
    }
    
    @Override
    public String doSharding(final Collection<String> availableTargetNames, final PreciseShardingValue<Comparable<?>> shardingValue) {
        long suffix = getLongValue(shardingValue.getValue()) % shardingCount;
        for (String each : availableTargetNames) {
            Optional<String> matchedTargetName = findMatchedTargetName(suffix, each);
            if (matchedTargetName.isPresent()) {
                return matchedTargetName.get();
            }
        }
        return null;
    }
    
    @Override
    public Collection<String> doSharding(final Collection<String> availableTargetNames, final RangeShardingValue<Comparable<?>> shardingValue) {
        return isContainAllTargets(shardingValue) ? availableTargetNames : getAvailableTargetNames(availableTargetNames, shardingValue);
    }
    
    private boolean isContainAllTargets(final RangeShardingValue<Comparable<?>> shardingValue) {
        return !shardingValue.getValueRange().hasUpperBound() || shardingValue.getValueRange().hasLowerBound()
                && getLongValue(shardingValue.getValueRange().upperEndpoint()) - getLongValue(shardingValue.getValueRange().lowerEndpoint()) >= shardingCount - 1;
    }
    
    private Collection<String> getAvailableTargetNames(final Collection<String> availableTargetNames, final RangeShardingValue<Comparable<?>> shardingValue) {
        Collection<String> result = new LinkedHashSet<>(availableTargetNames.size());
        for (long i = getLongValue(shardingValue.getValueRange().lowerEndpoint()); i <= getLongValue(shardingValue.getValueRange().upperEndpoint()); i++) {
            long suffix = i % shardingCount;
            for (String each : availableTargetNames) {
                findMatchedTargetName(suffix, each).ifPresent(result::add);
            }
        }
        return result;
    }
    
    private Optional<String> findMatchedTargetName(final long suffix, final String each) {
        Matcher matcher = SUFFIX_PATTERN.matcher(each);
        Long targetNameSuffix = matcher.matches() ? Longs.tryParse(matcher.group(1)) : null;
        if (null != targetNameSuffix && targetNameSuffix.equals(suffix)) {
            return Optional.of(each);
        }
        return Optional.empty();
    }
    
    private long getLongValue(final Comparable<?> value) {
        return value instanceof Number ? ((Number) value).longValue() : Long.parseLong(value.toString());
    }
    
    @Override
    public int getAutoTablesAmount() {
        return shardingCount;
    }
    
    @Override
    public String getType() {
        return "MOD";
    }
    
    @Override
    public Collection<String> getAllPropertyKeys() {
        return Collections.singletonList(SHARDING_COUNT_KEY);
    }
}
