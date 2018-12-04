package io.shardingsphere.core.parsing.antlr.sql.segment.expr;

import java.util.LinkedList;
import java.util.List;

import io.shardingsphere.core.parsing.parser.expression.SQLExpression;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public final class SQLInExpressionSegment implements SQLExpressionSegment {
    
    private final List<SQLExpression> sqlExpressions = new LinkedList<>();
}
