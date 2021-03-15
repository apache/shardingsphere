package org.apache.shardingsphere.infra.rewrite.sql.token.pojo.generic;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.sql.parser.sql.common.constant.QuoteCharacter;

import java.util.Optional;

@RequiredArgsConstructor
@Getter
public final class SubstitutableColumn {
    private final Optional<String> tableName;

    private final Optional<String> owner;

    private final String name;

    private final QuoteCharacter quoteCharacter;

    private final Optional<String> alias;
}
