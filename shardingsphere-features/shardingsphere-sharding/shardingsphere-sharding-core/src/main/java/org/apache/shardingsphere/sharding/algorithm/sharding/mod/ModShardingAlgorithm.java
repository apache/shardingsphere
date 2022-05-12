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
import lombok.Getter;
import org.apache.shardingsphere.sharding.api.sharding.ShardingAutoTableAlgorithm;
import org.apache.shardingsphere.sharding.api.sharding.standard.PreciseShardingValue;
import org.apache.shardingsphere.sharding.api.sharding.standard.RangeShardingValue;
import org.apache.shardingsphere.sharding.api.sharding.standard.StandardShardingAlgorithm;

import java.math.BigInteger;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Properties;

/**
 * Modulo sharding algorithm.
 */
public final class ModShardingAlgorithm implements StandardShardingAlgorithm<Comparable<?>>, ShardingAutoTableAlgorithm {
    
    private static final String SHARDING_COUNT_KEY = "sharding-count";
    
    private static final String REVERSE_START_INDEX = "reverse-start-index";
    
    private static final String ZERO_PADDING = "zero-padding";
    
    @Getter
    private Properties props;
    
    private int shardingCount;
    
    private volatile int reverseStartIndex;
    
    private volatile boolean zeroPadding;
    
    private volatile int shardingDigits;
    
    @Override
    public void init(final Properties props) {
        this.props = props;
        shardingCount = getShardingCount(props);
        reverseStartIndex = getReverseStartIndex(props);
        zeroPadding = isZeroPadding(props);
        shardingDigits = getShardingDigits();
    }
    
    private int getShardingCount(final Properties props) {
        Preconditions.checkArgument(props.containsKey(SHARDING_COUNT_KEY), "Sharding count cannot be null.");
        return Integer.parseInt(props.getProperty(SHARDING_COUNT_KEY));
    }
    
    private int getShardingDigits() {
        int digits = 0;
        // if shardingCount = 100, it must be 00-99, this is 2 digit
        int tmpCalculateShardingCount = shardingCount - 1;
        while (tmpCalculateShardingCount != 0) {
            digits++;
            tmpCalculateShardingCount = tmpCalculateShardingCount / 10;
        }
        return Math.max(digits, 1);
    }
    
    private int getReverseStartIndex(final Properties props) {
        return Integer.parseInt(String.valueOf(props.getOrDefault(REVERSE_START_INDEX, '0')));
    }
    
    private boolean isZeroPadding(final Properties props) {
        return Boolean.parseBoolean(String.valueOf(props.getOrDefault(ZERO_PADDING, "false")));
    }
    
    @Override
    public String doSharding(final Collection<String> availableTargetNames, final PreciseShardingValue<Comparable<?>> shardingValue) {
        BigInteger shardingCountBigInter = new BigInteger(String.valueOf(shardingCount));
        String suffix = getSuffix(getBigIntegerValue(shardingValue.getValue()).mod(shardingCountBigInter).toString());
        return findMatchedTargetName(availableTargetNames, suffix, shardingValue.getDataNodeInfo()).orElse(null);
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
        BigInteger lower = new BigInteger(shardingValue.getValueRange().lowerEndpoint().toString());
        BigInteger upper = new BigInteger(shardingValue.getValueRange().upperEndpoint().toString());
        BigInteger shardingCountBigInter = new BigInteger(String.valueOf(shardingCount));
        for (BigInteger i = lower; i.compareTo(upper) <= 0; i = i.add(new BigInteger("1"))) {
            String suffix = getSuffix(String.valueOf(i.mod(shardingCountBigInter)));
            findMatchedTargetName(availableTargetNames, suffix, shardingValue.getDataNodeInfo()).ifPresent(result::add);
        }
        return result;
    }
    
    private String getSuffix(final String suffix) {
        if (zeroPadding) {
            return fillString(suffix, shardingDigits);
        } else {
            return String.valueOf(suffix);
        }
    }
    
    private static String fillString(final String num, final int digit) {
        return String.format("%0" + digit + "d", Integer.parseInt(num));
    }
    
    private BigInteger getBigIntegerValue(final Comparable<?> value) {
        int endIndex = value.toString().length() - reverseStartIndex;
        return new BigInteger(value.toString().substring(0, endIndex));
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
}
