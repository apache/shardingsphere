package io.shardingsphere.core.parsing.antlr.sql.segment;

import com.google.common.base.Optional;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public class PropertyExpressionSegment extends SelectExpressionSegment {
    
    private final Optional<String> owner;
    
    private final String name;
    
    private final int startPosition;
}
