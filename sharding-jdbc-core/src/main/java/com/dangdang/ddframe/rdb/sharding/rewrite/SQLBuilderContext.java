package com.dangdang.ddframe.rdb.sharding.rewrite;

import com.dangdang.ddframe.rdb.sharding.parsing.parser.token.SQLToken;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.TreeSet;

/**
 * SQL构建器上下文.
 *
 * @author zhangliang
 */
@RequiredArgsConstructor
@Getter
public final class SQLBuilderContext {
    
    private final String originalSQL;
    
    private final List<SQLToken> sqlTokens = new LinkedList<>();
    
    private final Collection<String> tableNames = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
}
