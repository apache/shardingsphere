package io.shardingsphere.core.parsing.antlr.sql.segment;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@RequiredArgsConstructor
@Getter
@Setter
public class ShowParamSegment implements SQLSegment {
    private final String name;
}
