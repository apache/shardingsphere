package org.apache.shardingsphere.infra.executor.exec;

import org.apache.calcite.config.CalciteConnectionConfigImpl;
import org.apache.calcite.jdbc.CalciteSchema;
import org.apache.calcite.jdbc.JavaTypeFactoryImpl;
import org.apache.calcite.plan.Contexts;
import org.apache.calcite.plan.RelOptCluster;
import org.apache.calcite.plan.RelOptCostImpl;
import org.apache.calcite.plan.RelOptPlanner;
import org.apache.calcite.plan.RelOptSchema;
import org.apache.calcite.plan.volcano.VolcanoPlanner;
import org.apache.calcite.prepare.CalciteCatalogReader;
import org.apache.calcite.rel.RelNode;
import org.apache.calcite.rel.core.RelFactories;
import org.apache.calcite.rel.type.RelDataTypeSystem;
import org.apache.calcite.rex.RexBuilder;
import org.apache.calcite.schema.SchemaPlus;
import org.apache.calcite.sql.type.SqlTypeFactoryImpl;
import org.apache.calcite.tools.Frameworks;
import org.apache.calcite.tools.RelBuilder;
import org.apache.shardingsphere.infra.optimize.schema.ShardingSphereCalciteSchema;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Properties;

public class JDBCQueryExecutorTest extends BaseExecutorTest {
    
    @Before
    public void init() {
        
    }
    
    @Test
    public void testExecute() {
        String schemaName = "logic_db";
        RelOptPlanner planner = new VolcanoPlanner(RelOptCostImpl.FACTORY, Contexts.EMPTY_CONTEXT);
        planner.clearRelTraitDefs();
        RexBuilder rexBuilder = new RexBuilder(new SqlTypeFactoryImpl(RelDataTypeSystem.DEFAULT));
        RelOptCluster cluster = RelOptCluster.create(planner, rexBuilder);
        
        SchemaPlus rootSchema = Frameworks.createRootSchema(true);
        rootSchema.add(schemaName, new ShardingSphereCalciteSchema(schema));
        CalciteSchema schema =  CalciteSchema.from(rootSchema);
    
        RelOptSchema catalog = new CalciteCatalogReader(schema,
                schema.path(schemaName),
                new JavaTypeFactoryImpl(),
                new CalciteConnectionConfigImpl(new Properties()));
        
        RelBuilder relBuilder = RelFactories.LOGICAL_BUILDER.create(cluster, catalog);
        relBuilder.scan("t_order");
        RelNode relNode = relBuilder.build();
        Assert.assertNotNull(relNode);
    }
}
