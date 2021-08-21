package org.apache.shardingsphere.infra.optimize.core.prepare;

import org.apache.calcite.DataContext;
import org.apache.calcite.adapter.java.JavaTypeFactory;
import org.apache.calcite.config.CalciteConnectionConfig;
import org.apache.calcite.jdbc.CalcitePrepare;
import org.apache.calcite.jdbc.CalciteSchema;
import org.apache.calcite.jdbc.JavaTypeFactoryImpl;
import org.apache.calcite.tools.RelRunner;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.List;

public class FederateContext implements CalcitePrepare.Context {
    @Override
    public JavaTypeFactory getTypeFactory() {
        return new JavaTypeFactoryImpl();
    }

    @Override
    public CalciteSchema getRootSchema() {
        return null;
    }

    @Override
    public CalciteSchema getMutableRootSchema() {
        return null;
    }

    @Override
    public List<String> getDefaultSchemaPath() {
        return null;
    }

    @Override
    public CalciteConnectionConfig config() {
        return null;
    }

    @Override
    public CalcitePrepare.SparkHandler spark() {
        return null;
    }

    @Override
    public DataContext getDataContext() {
        return null;
    }

    @Override
    public @Nullable List<String> getObjectPath() {
        return null;
    }

    @Override
    public RelRunner getRelRunner() {
        return null;
    }
}
