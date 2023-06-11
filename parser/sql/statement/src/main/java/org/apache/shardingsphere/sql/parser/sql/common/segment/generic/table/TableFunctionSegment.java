package org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table;


import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.ExpressionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.FunctionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.AliasSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.OwnerSegment;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Optional;

/**
 * Table Function Segment
 */
@RequiredArgsConstructor
@Getter
public class TableFunctionSegment implements TableSegment{
    
    private final int startIndex;
    
    private final int stopIndex;
    
    private final FunctionSegment tableFunction;

    @Setter
    private OwnerSegment owner;
    
    @Override
    public Optional<String> getAliasName() {
        return Optional.empty();
    }
    
    @Override
    public Optional<AliasSegment> getAlias() {
        return Optional.empty();
    }
    
    @Override
    public void setAlias(final AliasSegment alias) {
    
    }
}
