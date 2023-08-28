package org.apache.shardingsphere.sql.parser.sql.dialect.statement.opengauss.segment;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.apache.shardingsphere.sql.parser.sql.common.segment.SQLSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.ddl.partition.PartitionNameSegment;

@RequiredArgsConstructor
@Getter
@Setter
public class RenamePartitionSegment implements SQLSegment {

    private final int startIndex;

    private final int stopIndex;

    private final PartitionNameSegment oldPartition;

    private final PartitionNameSegment newPartition;
}
