package org.apache.shardingsphere.core.optimize.segment.insert.expression;

import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

public class DerivedParameterMarkerExpressionSegmentTest {

    @Test
    public void assertInstanceConstructedOk() {
        int parameterMarkerIndex = 10;
        String type = "type";

        DerivedParameterMarkerExpressionSegment derivedParameterMarkerExpressionSegment = new DerivedParameterMarkerExpressionSegment(parameterMarkerIndex,type);

        assertThat(derivedParameterMarkerExpressionSegment.getType(), is(type));
        assertThat(derivedParameterMarkerExpressionSegment.getStartIndex(), is(0));
        assertThat(derivedParameterMarkerExpressionSegment.getStopIndex(), is(0));
        assertThat(derivedParameterMarkerExpressionSegment.getParameterMarkerIndex(), is(parameterMarkerIndex));
    }

}
