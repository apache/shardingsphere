package org.apache.shardingsphere.infra.expression;

import com.google.common.collect.Lists;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.BinaryOperationExpression;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.ExpressionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.simple.ParameterMarkerExpressionSegment;

import java.util.List;

/**
 * Expression segment util.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ExpressionSegmentUtil {
    /**
     * Extract all ParameterMarkerExpressionSegment from ExpressionSegment list.
     *
     * @param expressions ExpressionSegment list
     * @return ParameterMarkerExpressionSegment list
     */
    public static List<ParameterMarkerExpressionSegment> extractParameterMarkerExpressionSegment(final List<ExpressionSegment> expressions) {
        List<ParameterMarkerExpressionSegment> extractList = Lists.newArrayList();
        for (ExpressionSegment each : expressions) {
            if (each instanceof ParameterMarkerExpressionSegment) {
                extractList.add((ParameterMarkerExpressionSegment) each);
            } else if (each instanceof BinaryOperationExpression) {
                if (((BinaryOperationExpression) each).getRight() instanceof ParameterMarkerExpressionSegment) {
                    extractList.add((ParameterMarkerExpressionSegment) ((BinaryOperationExpression) each).getRight());
                }
            }
        }
        return extractList;
    }
}
