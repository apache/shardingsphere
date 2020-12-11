package org.apache.shardingsphere.sharding.algorithm.sharding.hint;

import com.google.common.collect.Lists;
import org.apache.shardingsphere.sharding.api.sharding.hint.HintShardingValue;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertTrue;

/**
 * Hint Inline sharding algorithm test.
 *
 * @author likly
 * @version 5.0.0
 * @since 5.0.0
 */
public final class HintInlineShardingAlgorithmTest {

    private HintInlineShardingAlgorithm hintInlineShardingAlgorithm;

    private HintInlineShardingAlgorithm hintInlineShardingAlgorithmDefault;

    @Before
    public void setUp() {
        initHintInlineShardingAlgorithm();
        initHintInlineShardingAlgorithmDefault();
    }

    private void initHintInlineShardingAlgorithm() {
        hintInlineShardingAlgorithm = new HintInlineShardingAlgorithm();
        hintInlineShardingAlgorithm.getProps().setProperty("algorithm-expression", "t_order_$->{value % 4}");
        hintInlineShardingAlgorithm.init();
    }

    private void initHintInlineShardingAlgorithmDefault() {
        hintInlineShardingAlgorithmDefault = new HintInlineShardingAlgorithm();
        hintInlineShardingAlgorithmDefault.init();
    }

    @Test
    public void assertDoShardingWithSingleValueOfDefault() {
        List<String> availableTargetNames = Lists.newArrayList("t_order_0", "t_order_1", "t_order_2", "t_order_3");
        HintShardingValue<Comparable<?>> shardingValue = new HintShardingValue<>("t_order", "order_id", Collections.singleton("t_order_0"));
        Collection<String> actual = hintInlineShardingAlgorithmDefault.doSharding(availableTargetNames, shardingValue);
        assertTrue(actual.contains("t_order_0"));
    }

    @Test
    public void assertDoShardingWithSingleValue() {
        List<String> availableTargetNames = Lists.newArrayList("t_order_0", "t_order_1", "t_order_2", "t_order_3");
        HintShardingValue<Comparable<?>> shardingValue = new HintShardingValue<>("t_order", "order_id", Collections.singleton(4));
        Collection<String> actual = hintInlineShardingAlgorithm.doSharding(availableTargetNames, shardingValue);
        assertTrue(actual.contains("t_order_0"));
    }

    @Test
    public void assertDoShardingWithMultiValues() {
        List<String> availableTargetNames = Lists.newArrayList("t_order_0", "t_order_1", "t_order_2", "t_order_3");
        HintShardingValue<Comparable<?>> shardingValue = new HintShardingValue<>("t_order", "order_id", Arrays.asList(1, 2, 3, 4));
        Collection<String> actual = hintInlineShardingAlgorithm.doSharding(availableTargetNames, shardingValue);
        assertTrue(actual.containsAll(availableTargetNames));
    }
}
