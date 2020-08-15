package org.apache.shardingsphere.infra.rewrite.sql.token.pojo.generic;

import org.apache.shardingsphere.sql.parser.sql.segment.dml.expr.ExpressionSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.expr.complex.ComplexExpressionSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.expr.simple.LiteralExpressionSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.expr.simple.ParameterMarkerExpressionSegment;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class InsertValueTest {

    @Test
    public void assertToString() {
        List<ExpressionSegment> expressionSegmentList = new ArrayList<>();

        ParameterMarkerExpressionSegment parameterMarkerExpressionSegment =
                new ParameterMarkerExpressionSegment(1,1,1);
        LiteralExpressionSegment literalExpressionSegment
                = new LiteralExpressionSegment(2,2,"literals");
        ComplexExpressionSegment complexExpressionSegment = new ComplexExpressionSegment() {
            @Override
            public String getText() {
                return "complexExpressionSegment";
            }

            @Override
            public int getStartIndex() {
                return 3;
            }

            @Override
            public int getStopIndex() {
                return 3;
            }
        };

        expressionSegmentList.add(parameterMarkerExpressionSegment);
        expressionSegmentList.add(literalExpressionSegment);
        expressionSegmentList.add(complexExpressionSegment);

        InsertValue insertValue = new InsertValue(expressionSegmentList);
        String result = insertValue.toString();
        assertThat(result,is("(?, 'literals', complexExpressionSegment)"));
    }
}
