package io.shardingsphere.core.parsing.antlr.sql.segment.condition;

import io.shardingsphere.core.constant.ShardingOperator;
import io.shardingsphere.core.parsing.antlr.sql.segment.SQLSegment;
import io.shardingsphere.core.parsing.antlr.sql.segment.column.ColumnSegment;
import io.shardingsphere.core.parsing.antlr.sql.segment.expr.SQLExpressionSegment;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public class ConditionSegment implements SQLSegment {
    
    private final ColumnSegment column;
    
    private final ShardingOperator operator;
    
    private final SQLExpressionSegment expression;
    
}
