package org.apache.shardingsphere.sql.parser.clickhouse.visitor.statement;

import org.apache.shardingsphere.sql.parser.api.visitor.statement.type.*;
import org.apache.shardingsphere.sql.parser.clickhouse.visitor.statement.type.*;
import org.apache.shardingsphere.sql.parser.spi.SQLStatementVisitorFacade;

/**
 * @author zzypersonally@gmail.com
 * @since 2024/5/7 16:04
 */
public class ClickHouseStatementVisitorFacade implements SQLStatementVisitorFacade {

    @Override
    public Class<? extends DMLStatementVisitor> getDMLVisitorClass() {
        return ClickHouseDMLStatementVisitor.class;
    }

    @Override
    public Class<? extends DDLStatementVisitor> getDDLVisitorClass() {
        return ClickHouseDDLStatementVisitor.class;
    }

    @Override
    public Class<? extends TCLStatementVisitor> getTCLVisitorClass() {
        return ClickHouseTCLStatementVisitor.class;
    }

    @Override
    public Class<? extends DCLStatementVisitor> getDCLVisitorClass() {
        return ClickHouseDCLStatementVisitor.class;
    }

    @Override
    public Class<? extends DALStatementVisitor> getDALVisitorClass() {
        return ClickHouseDALStatementVisitor.class;
    }

    @Override
    public Class<? extends RLStatementVisitor> getRLVisitorClass() {
        return ClickHouseRLStatementVisitor.class;
    }

    @Override
    public String getDatabaseType() {
        return "ClickHouse";
    }
}
