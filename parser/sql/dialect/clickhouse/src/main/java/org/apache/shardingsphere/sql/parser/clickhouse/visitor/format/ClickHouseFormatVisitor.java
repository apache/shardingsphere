package org.apache.shardingsphere.sql.parser.clickhouse.visitor.format;

import org.apache.shardingsphere.sql.parser.api.visitor.format.SQLFormatVisitor;
import org.apache.shardingsphere.sql.parser.autogen.ClickHouseStatementBaseVisitor;

/**
 * @author zzypersonally@gmail.com
 * @description
 * @since 2024/5/9 15:27
 */
public final class ClickHouseFormatVisitor extends ClickHouseStatementBaseVisitor<String> implements SQLFormatVisitor {


    @Override
    public String getDatabaseType() {
        return "ClickHouse";
    }

}
