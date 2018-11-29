package io.shardingsphere.core.parsing.antlr.sql.segment;

import io.shardingsphere.core.constant.ShardingOperator;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public class ConditionSegment implements SQLSegment {
    
    private final ColumnSegment column;
    
    private final ShardingOperator operator;
    
    private final SQLExpressionSegment expression;
    
}
