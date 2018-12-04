package io.shardingsphere.core.parsing.antlr.sql.segment.condition;

import java.util.LinkedList;
import java.util.List;

import io.shardingsphere.core.parsing.antlr.sql.segment.SQLSegment;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public final class OrConditionSegment implements SQLSegment {
    
    private List<AndConditionSegment> andConditions = new LinkedList<>();
}
