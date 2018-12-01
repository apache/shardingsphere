package io.shardingsphere.core.parsing.antlr.sql.segment;

import io.shardingsphere.core.parsing.parser.expression.SQLExpression;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public class SQLEqualsExpressionSegment extends SQLExpressionSegment {
    
    private final SQLExpression expression;
}
