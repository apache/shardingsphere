package io.shardingsphere.core.parsing.antlr.sql.segment;

import java.util.LinkedList;
import java.util.List;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public final class AndConditionSegment implements SQLSegment {
    
    private List<ConditionSegment> conditions = new LinkedList<>();
}
