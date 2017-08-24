package com.dangdang.ddframe.rdb.sharding.parsing.parser.clause;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * INSERT从句解析器门面类.
 *
 * @author zhangliang
 */
@RequiredArgsConstructor
@Getter
public abstract class AbstractInsertClauseParserFacade {
    
    private final IntoClauseParser intoClauseParser;
}
