package org.apache.shardingsphere.infra.exception.kernel.syntax;

import org.apache.shardingsphere.infra.exception.core.external.sql.sqlstate.XOpenSQLState;
import org.apache.shardingsphere.infra.exception.core.external.sql.type.kernel.category.SyntaxSQLException;

public final class UniqueCommonTableExpressionException extends SyntaxSQLException {

    private static final long serialVersionUID = -8206891094419297634L;

    public UniqueCommonTableExpressionException(final String alias )
    {
        super(XOpenSQLState.SYNTAX_ERROR, 500, "Not unique table/alias: '%s'", alias);
    }
}
