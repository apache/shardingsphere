package org.apache.shardingsphere.sql.parser.sql.common.segment.ddl.index;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.sql.parser.sql.common.segment.ddl.AlterDefinitionSegment;

/**
 * @author xuyang
 */
@RequiredArgsConstructor
@Getter
public class DropIndexDefinitionSegment implements AlterDefinitionSegment {

    private final int startIndex;

    private final int stopIndex;

    private final IndexSegment indexSegment;
}
