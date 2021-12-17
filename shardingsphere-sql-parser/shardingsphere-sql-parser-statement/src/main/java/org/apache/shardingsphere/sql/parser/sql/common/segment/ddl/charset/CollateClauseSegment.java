package org.apache.shardingsphere.sql.parser.sql.common.segment.ddl.charset;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.sql.parser.sql.common.segment.SQLSegment;

/**
 * Collate clause segment.
 */
@RequiredArgsConstructor
@Getter
public final class CollateClauseSegment implements SQLSegment {
    
    private final int startIndex;
    
    private final int stopIndex;
    
    private final String name;
}
