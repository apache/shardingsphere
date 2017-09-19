package io.shardingjdbc.core.util;

import org.junit.Test;

import java.util.List;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsCollectionContaining.hasItems;
import static org.junit.Assert.assertThat;

public final class InlineExpressionParserTest {
    
    @Test
    public void assertEvaluateForSimpleString() {
        List<String> expected = new InlineExpressionParser(" t_order_0, t_order_1 ").evaluate();
        assertThat(expected.size(), is(2));
        assertThat(expected, hasItems("t_order_0", "t_order_1"));
    }
    
    @Test
    public void assertEvaluateForNull() {
        List<String> expected = new InlineExpressionParser("t_order_${null}").evaluate();
        assertThat(expected.size(), is(1));
        assertThat(expected, hasItems("t_order_"));
    }
    
    @Test
    public void assertEvaluateForLiteral() {
        List<String> expected = new InlineExpressionParser("t_order_${'xx'}").evaluate();
        assertThat(expected.size(), is(1));
        assertThat(expected, hasItems("t_order_xx"));
    }
    
    @Test
    public void assertEvaluateForArray() {
        List<String> expected = new InlineExpressionParser("t_order_${[0, 1, 2]},t_order_item_${[0, 2]}").evaluate();
        assertThat(expected.size(), is(5));
        assertThat(expected, hasItems("t_order_0", "t_order_1", "t_order_2", "t_order_item_0", "t_order_item_2"));
    }
    
    @Test
    public void assertEvaluateForRange() {
        List<String> expected = new InlineExpressionParser("t_order_${0..2},t_order_item_${0..1}").evaluate();
        assertThat(expected.size(), is(5));
        assertThat(expected, hasItems("t_order_0", "t_order_1", "t_order_2", "t_order_item_0", "t_order_item_1"));
    }
    
    @Test
    public void assertEvaluateForComplex() {
        List<String> expected = new InlineExpressionParser("t_${['new','old']}_order_${1..2}, t_config").evaluate();
        assertThat(expected.size(), is(5));
        assertThat(expected, hasItems("t_new_order_1", "t_new_order_2", "t_old_order_1", "t_old_order_2", "t_config"));
    }
    
    @Test
    public void assertEvaluateForCalculate() {
        List<String> expected = new InlineExpressionParser("t_${[\"new${1+2}\",'old']}_order_${1..2}").evaluate();
        assertThat(expected.size(), is(4));
        assertThat(expected, hasItems("t_new3_order_1", "t_new3_order_2", "t_old_order_1", "t_old_order_2"));
    }
}
