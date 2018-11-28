package io.shardingsphere.core.parsing.parser.dialect.postgresql.statement;

import io.shardingsphere.core.parsing.parser.sql.dal.DALStatement;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public final class ShowStatement extends DALStatement {

    private String name;
}
