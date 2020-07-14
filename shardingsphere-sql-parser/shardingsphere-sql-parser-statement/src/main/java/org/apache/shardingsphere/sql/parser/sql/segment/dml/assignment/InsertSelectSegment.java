package org.apache.shardingsphere.sql.parser.sql.segment.dml.assignment;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.sql.parser.sql.segment.SQLSegment;

/**
 * Insert select segment.
 */
@RequiredArgsConstructor
@Getter
public class InsertSelectSegment implements SQLSegment {
    
    private final int startIndex;
    
    private final int stopIndex;
    
    private final SelectSegment select;
}
