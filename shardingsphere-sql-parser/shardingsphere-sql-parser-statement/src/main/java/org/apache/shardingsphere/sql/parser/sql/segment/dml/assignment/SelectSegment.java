package org.apache.shardingsphere.sql.parser.sql.segment.dml.assignment;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.sql.parser.sql.segment.SQLSegment;
import org.apache.shardingsphere.sql.parser.sql.statement.dml.SelectStatement;

/**
 * Select segment.
 */
@RequiredArgsConstructor
@Getter
public class SelectSegment implements SQLSegment {
    
    private final int startIndex;
    
    private final int stopIndex;
    
    private final SelectStatement selectStatement;
}
