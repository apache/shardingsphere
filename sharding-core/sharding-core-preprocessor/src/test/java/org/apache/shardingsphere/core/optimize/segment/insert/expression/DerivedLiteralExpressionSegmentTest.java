package org.apache.shardingsphere.core.optimize.segment.insert.expression;

import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

public class DerivedLiteralExpressionSegmentTest {


    @Test
    public void assertInstanceConstructedOk() {
        Object literals = new Object();
        String type = "type";

        DerivedLiteralExpressionSegment derivedLiteralExpressionSegment = new DerivedLiteralExpressionSegment(literals,type);

        assertThat(derivedLiteralExpressionSegment.getType(), is(type));
        assertThat(derivedLiteralExpressionSegment.getStartIndex(), is(0));
        assertThat(derivedLiteralExpressionSegment.getStopIndex(), is(0));
        assertThat(derivedLiteralExpressionSegment.getLiterals(), is(literals));
    }

}
