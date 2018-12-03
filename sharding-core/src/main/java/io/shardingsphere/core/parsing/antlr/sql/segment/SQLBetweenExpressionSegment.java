package io.shardingsphere.core.parsing.antlr.sql.segment;

import io.shardingsphere.core.parsing.parser.expression.SQLExpression;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public class SQLBetweenExpressionSegment implements SQLExpressionSegment {
    
    private final SQLExpression beginExpress;
    
    private final SQLExpression endExpress;
}
