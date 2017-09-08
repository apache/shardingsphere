package com.dangdang.ddframe.rdb.sharding.util;

import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsCollectionContaining.hasItems;
import static org.junit.Assert.assertThat;

public final class InlineExpressionParserTest {
    
    @Test
    public void assertSplit() {
        assertThat(new InlineExpressionParser(" 1 ").split(), is(Collections.singletonList("1")));
        assertThat(new InlineExpressionParser(" 1 , 2 ").split(), is(Arrays.asList("1", "2")));
    }
    
    @Test
    public void assertEvaluate() {
        assertThat(new InlineExpressionParser(" 1 , 2 ").evaluate(), hasItems("1", "2"));
        assertThat(new InlineExpressionParser(" 1 , t_order_${[0, 1, 2]} ").evaluate(), hasItems("1", "t_order_0", "t_order_1", "t_order_2"));
        assertThat(new InlineExpressionParser(" 1 , t_order_${0..2} ").evaluate(), hasItems("1", "t_order_0", "t_order_1", "t_order_2"));
        assertThat(new InlineExpressionParser(" 1 , t_order_${null} ").evaluate(), hasItems("1", "t_order_"));
        assertThat(new InlineExpressionParser(" 1 , t_order_${'xx'} ").evaluate(), hasItems("1", "t_order_xx"));
        assertThat(new InlineExpressionParser(" t_${['new','old']}_order_${1..2} ").evaluate(), hasItems("t_new_order_1", "t_new_order_2", "t_old_order_1", "t_old_order_2"));
        assertThat(new InlineExpressionParser(" t_${[\"new${1+2}\",'old']}_order_${1..2} ").evaluate(), hasItems("t_new3_order_1", "t_new3_order_2", "t_old_order_1", "t_old_order_2"));
    }
    
    @Test
    public void assertSplitWithInlineExpressionForOneRegularValue() {
        assertThat(new InlineExpressionParser("order").splitWithInlineExpression(), is(Collections.singletonList("order")));
    }
    
    @Test
    public void assertSplitWithInlineExpressionForOneInlineValue() {
        assertThat(new InlineExpressionParser("order_${0..7}").splitWithInlineExpression(), is(Collections.singletonList("order_${0..7}")));
    }
    
    @Test
    public void assertSplitWithInlineExpressionForRegularValuesWithSpace() {
        assertThat(new InlineExpressionParser(" order , order_item ").splitWithInlineExpression(), is(Arrays.asList("order", "order_item")));
    }
    
    @Test
    public void assertSplitWithInlineExpressionForMixedValuesWithSpace() {
        assertThat(new InlineExpressionParser(" order , order_item_${0..7} ").splitWithInlineExpression(), is(Arrays.asList("order", "order_item_${0..7}")));
    }
    
    @Test
    public void assertSplitWithInlineExpressionForMixedValuesAndWithCommaAtInlineExpression() {
        assertThat(new InlineExpressionParser(" order , order_item_${0, 2, 4} ").splitWithInlineExpression(), is(Arrays.asList("order", "order_item_${0, 2, 4}")));
    }
    
    @Test
    public void assertSplitWithInlineExpressionForMixedValuesAndNestedInlineExpression() {
        assertThat(new InlineExpressionParser(" order , order_item_${0, ${1..7}} ").splitWithInlineExpression(), is(Arrays.asList("order", "order_item_${0, ${1..7}}")));
    }
    
    @Test
    public void assertSplitWithInlineExpressionForValuesNotInlineExpression() {
        assertThat(new InlineExpressionParser(" order , order_item_$ {0, 1} ").splitWithInlineExpression(), is(Arrays.asList("order", "order_item_$ {0", "1}")));
    }
}
