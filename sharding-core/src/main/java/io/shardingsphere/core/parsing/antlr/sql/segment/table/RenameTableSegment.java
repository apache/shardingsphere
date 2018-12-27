package io.shardingsphere.core.parsing.antlr.sql.segment.table;

import io.shardingsphere.core.parsing.antlr.sql.segment.SQLSegment;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public class RenameTableSegment implements SQLSegment {
    
    private final String newTableName;
}
