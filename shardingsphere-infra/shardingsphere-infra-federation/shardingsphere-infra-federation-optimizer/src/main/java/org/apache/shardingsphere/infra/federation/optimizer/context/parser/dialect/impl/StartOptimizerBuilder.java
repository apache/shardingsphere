package org.apache.shardingsphere.infra.federation.optimizer.context.parser.dialect.impl;

import org.apache.calcite.config.CalciteConnectionProperty;
import org.apache.calcite.config.Lex;
import org.apache.calcite.sql.fun.SqlLibrary;
import org.apache.calcite.sql.validate.SqlConformanceEnum;
import org.apache.shardingsphere.infra.federation.optimizer.context.parser.dialect.OptimizerSQLDialectBuilder;

import java.util.Properties;

public class StartOptimizerBuilder implements OptimizerSQLDialectBuilder {
    @Override
    public Properties build() {
        Properties result = new Properties();
        result.setProperty(CalciteConnectionProperty.LEX.camelName(), Lex.MYSQL.name());
        result.setProperty(CalciteConnectionProperty.CONFORMANCE.camelName(), SqlConformanceEnum.MYSQL_5.name());
        result.setProperty(CalciteConnectionProperty.FUN.camelName(), SqlLibrary.MYSQL.fun);
        return result;
    }

    @Override
    public String getType() {
        return "START-DB";
    }
}
