package io.shardingsphere.core.parsing.antlr.sql.segment;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public class PropertyExpressionSegment extends SelectExpressionSegment {
    
    private final String owner;
    
    private final String name;
    
    private final int startPosition;
}
