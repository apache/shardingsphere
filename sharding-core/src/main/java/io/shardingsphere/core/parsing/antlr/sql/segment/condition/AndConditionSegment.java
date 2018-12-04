package io.shardingsphere.core.parsing.antlr.sql.segment.condition;

import java.util.LinkedList;
import java.util.List;

import io.shardingsphere.core.parsing.antlr.sql.segment.SQLSegment;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public final class AndConditionSegment implements SQLSegment {
    
    private List<ConditionSegment> conditions = new LinkedList<>();
}
