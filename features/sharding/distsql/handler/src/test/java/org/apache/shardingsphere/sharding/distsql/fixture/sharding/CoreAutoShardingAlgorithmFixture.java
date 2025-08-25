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

package org.apache.shardingsphere.sharding.distsql.fixture.sharding;

import com.google.common.base.Preconditions;
import org.apache.shardingsphere.infra.algorithm.core.exception.AlgorithmInitializationException;
import org.apache.shardingsphere.infra.exception.ShardingSpherePreconditions;
import org.apache.shardingsphere.sharding.algorithm.sharding.ShardingAutoTableAlgorithmUtils;
import org.apache.shardingsphere.sharding.api.sharding.ShardingAutoTableAlgorithm;
import org.apache.shardingsphere.sharding.api.sharding.standard.PreciseShardingValue;
import org.apache.shardingsphere.sharding.api.sharding.standard.RangeShardingValue;
import org.apache.shardingsphere.sharding.api.sharding.standard.StandardShardingAlgorithm;

import java.math.BigInteger;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Properties;

public final class CoreAutoShardingAlgorithmFixture implements StandardShardingAlgorithm<Comparable<?>>, ShardingAutoTableAlgorithm {
    
    private static final String SHARDING_COUNT_KEY = "sharding-count";
    
    private static final String START_OFFSET_INDEX_KEY = "start-offset";
    
    private static final String STOP_OFFSET_INDEX_KEY = "stop-offset";
    
    private static final String ZERO_PADDING_KEY = "zero-padding";
    
    private int shardingCount;
    
    private int startOffset;
    
    private int stopOffset;
    
    private boolean zeroPadding;
    
    private int maxPaddingSize;
    
    @Override
    public void init(final Properties props) {
        shardingCount = getShardingCount(props);
        startOffset = getStartOffset(props);
        stopOffset = getStopOffset(props);
        zeroPadding = isZeroPadding(props);
        maxPaddingSize = calculateMaxPaddingSize();
    }
    
    private int getShardingCount(final Properties props) {
        Preconditions.checkArgument(props.containsKey(SHARDING_COUNT_KEY), "Sharding count can not be null.");
        int result = Integer.parseInt(String.valueOf(props.getProperty(SHARDING_COUNT_KEY)));
        ShardingSpherePreconditions.checkState(result > 0, () -> new AlgorithmInitializationException(this, "Sharding count must be a positive integer."));
        return result;
    }
    
    private int getStartOffset(final Properties props) {
        int result = Integer.parseInt(String.valueOf(props.getProperty(START_OFFSET_INDEX_KEY, "0")));
        ShardingSpherePreconditions.checkState(result >= 0, () -> new AlgorithmInitializationException(this, "Start offset can not be less than 0."));
        return result;
    }
    
    private int getStopOffset(final Properties props) {
        int result = Integer.parseInt(String.valueOf(props.getProperty(STOP_OFFSET_INDEX_KEY, "0")));
        ShardingSpherePreconditions.checkState(result >= 0, () -> new AlgorithmInitializationException(this, "Stop offset can not be less than 0."));
        return result;
    }
    
    private boolean isZeroPadding(final Properties props) {
        return Boolean.parseBoolean(String.valueOf(props.getProperty(ZERO_PADDING_KEY, Boolean.FALSE.toString())));
    }
    
    private int calculateMaxPaddingSize() {
        int result = 0;
        int calculatingShardingCount = shardingCount - 1;
        while (0 != calculatingShardingCount) {
            result++;
            calculatingShardingCount = calculatingShardingCount / 10;
        }
        return Math.max(result, 1);
    }
    
    @Override
    public String doSharding(final Collection<String> availableTargetNames, final PreciseShardingValue<Comparable<?>> shardingValue) {
        String shardingResultSuffix = getShardingResultSuffix(cutShardingValue(shardingValue.getValue()).mod(new BigInteger(String.valueOf(shardingCount))).toString());
        return ShardingAutoTableAlgorithmUtils.findMatchedTargetName(availableTargetNames, shardingResultSuffix, shardingValue.getDataNodeInfo()).orElse(null);
    }
    
    @Override
    public Collection<String> doSharding(final Collection<String> availableTargetNames, final RangeShardingValue<Comparable<?>> shardingValue) {
        return containsAllTargets(shardingValue) ? availableTargetNames : getAvailableTargetNames(availableTargetNames, shardingValue);
    }
    
    private boolean containsAllTargets(final RangeShardingValue<Comparable<?>> shardingValue) {
        return !shardingValue.getValueRange().hasUpperBound() || shardingValue.getValueRange().hasLowerBound()
                && getBigInteger(shardingValue.getValueRange().upperEndpoint()).subtract(getBigInteger(shardingValue.getValueRange().lowerEndpoint())).intValue() >= shardingCount - 1;
    }
    
    private Collection<String> getAvailableTargetNames(final Collection<String> availableTargetNames, final RangeShardingValue<Comparable<?>> shardingValue) {
        Collection<String> result = new LinkedHashSet<>(availableTargetNames.size(), 1F);
        BigInteger lower = new BigInteger(shardingValue.getValueRange().lowerEndpoint().toString());
        BigInteger upper = new BigInteger(shardingValue.getValueRange().upperEndpoint().toString());
        BigInteger shardingCountBigInter = new BigInteger(String.valueOf(shardingCount));
        for (BigInteger i = lower; i.compareTo(upper) <= 0; i = i.add(new BigInteger("1"))) {
            String shardingResultSuffix = getShardingResultSuffix(String.valueOf(i.mod(shardingCountBigInter)));
            ShardingAutoTableAlgorithmUtils.findMatchedTargetName(availableTargetNames, shardingResultSuffix, shardingValue.getDataNodeInfo()).ifPresent(result::add);
        }
        return result;
    }
    
    private String getShardingResultSuffix(final String shardingResultSuffix) {
        return zeroPadding ? fillZero(shardingResultSuffix) : shardingResultSuffix;
    }
    
    private String fillZero(final String value) {
        return String.format("%0" + maxPaddingSize + "d", Integer.parseInt(value));
    }
    
    private BigInteger cutShardingValue(final Comparable<?> shardingValue) {
        checkOffsetArgument(shardingValue);
        return 0 == startOffset && 0 == stopOffset ? getBigInteger(shardingValue) : new BigInteger(shardingValue.toString().substring(startOffset, shardingValue.toString().length() - stopOffset));
    }
    
    private void checkOffsetArgument(final Comparable<?> shardingValue) {
        Preconditions.checkArgument(startOffset >= 0, "Start offset can not be less than 0.");
        Preconditions.checkArgument(stopOffset >= 0, "Stop offset can not be less than 0.");
        Preconditions.checkArgument(shardingValue.toString().length() - stopOffset > startOffset, "Sharding value subtract stop offset can not be less than start offset.");
    }
    
    private BigInteger getBigInteger(final Comparable<?> value) {
        return value instanceof Number ? BigInteger.valueOf(((Number) value).longValue()) : new BigInteger(value.toString());
    }
    
    @Override
    public int getAutoTablesAmount() {
        return shardingCount;
    }
    
    @Override
    public String getType() {
        return "CORE.AUTO.FIXTURE";
    }
}
