package com.dangdang.ddframe.rdb.sharding.parsing.parser.clause;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * DELETE从句解析器门面类.
 *
 * @author zhangliang
 */
@RequiredArgsConstructor
@Getter
public abstract class AbstractDeleteClauseParserFacade {
    
    private final TableClauseParser tableClauseParser;
    
    private final WhereClauseParser whereClauseParser;
}
