package org.apache.shardingsphere.infra.optimize.converter;

import org.apache.calcite.config.CalciteConnectionConfigImpl;
import org.apache.calcite.jdbc.CalciteSchema;
import org.apache.calcite.jdbc.JavaTypeFactoryImpl;
import org.apache.calcite.plan.Contexts;
import org.apache.calcite.plan.ConventionTraitDef;
import org.apache.calcite.plan.RelOptCluster;
import org.apache.calcite.plan.RelOptCostFactory;
import org.apache.calcite.plan.RelOptCostImpl;
import org.apache.calcite.plan.RelOptPlanner;
import org.apache.calcite.plan.volcano.VolcanoPlanner;
import org.apache.calcite.prepare.CalciteCatalogReader;
import org.apache.calcite.rel.RelCollationTraitDef;
import org.apache.calcite.rel.RelNode;
import org.apache.calcite.rel.RelRoot;
import org.apache.calcite.rel.type.RelDataTypeFactory;
import org.apache.calcite.rel.type.RelDataTypeSystem;
import org.apache.calcite.rex.RexBuilder;
import org.apache.calcite.schema.SchemaPlus;
import org.apache.calcite.sql.SqlNode;
import org.apache.calcite.sql.fun.SqlStdOperatorTable;
import org.apache.calcite.sql.type.SqlTypeFactoryImpl;
import org.apache.calcite.sql.validate.SqlConformanceEnum;
import org.apache.calcite.sql.validate.SqlValidator;
import org.apache.calcite.sql.validate.SqlValidatorUtil;
import org.apache.calcite.sql.validate.SqlValidatorWithHints;
import org.apache.calcite.sql2rel.SqlToRelConverter;
import org.apache.calcite.sql2rel.StandardConvertletTable;
import org.apache.calcite.tools.Frameworks;
import org.apache.shardingsphere.infra.metadata.schema.ShardingSphereSchema;
import org.apache.shardingsphere.infra.optimize.schema.ShardingSphereCalciteSchema;

import java.util.Properties;

/**
 * convert calcite SqlNode to calcite RelNode
 */
public class RelNodeConverter {

    static {
        System.setProperty("calcite.planner.topdown.opt", "true");
    }
    
    private final RelOptCostFactory costFactory = RelOptCostImpl.FACTORY;

    private static final RelDataTypeFactory typeFactory = new SqlTypeFactoryImpl(RelDataTypeSystem.DEFAULT);

    private final SqlToRelConverter.Config converterConfig;

    private final SqlValidatorWithHints validator;
    private final CalciteCatalogReader catalog;
    private final ShardingSphereSchema shardingSphereSchema;

    public RelNodeConverter(String schemaName, ShardingSphereSchema shardingSphereSchema) {
        this.shardingSphereSchema = shardingSphereSchema;
        this.converterConfig = SqlToRelConverter.config()
                .withInSubQueryThreshold(Integer.MAX_VALUE)
                .withExpand(false);
        CalciteSchema rootSchema = createRootSchema(schemaName);
        this.catalog = new CalciteCatalogReader(rootSchema,
                rootSchema.path(schemaName),
                new JavaTypeFactoryImpl(),
                new CalciteConnectionConfigImpl(new Properties()));

        validator = SqlValidatorUtil.newValidator(SqlStdOperatorTable.instance(), catalog, typeFactory, SqlValidator.Config.DEFAULT
                .withLenientOperatorLookup(false)
                .withSqlConformance(SqlConformanceEnum.DEFAULT)
                .withIdentifierExpansion(true));
    }

    /**
     * validate calcite SqlNode and convert SqlNode to RelNode
     * @param sqlNode SqlNode to be validated and converted
     * @return relational algebra of sql
     */
    public RelNode validateAndConvert(SqlNode sqlNode) {
        final RelOptCluster cluster = createRelOptCluster();
        final SqlToRelConverter sqlToRelConverter = new SqlToRelConverter(null, validator, catalog, cluster,
                StandardConvertletTable.INSTANCE, converterConfig);

        // validate and convert
        RelRoot root = sqlToRelConverter.convertQuery(sqlNode, true, true);
        return root.rel;
    }

    public RelOptCluster createRelOptCluster() {
        RexBuilder rexBuilder = new RexBuilder(typeFactory);
        RelOptPlanner planner = new VolcanoPlanner(costFactory, Contexts.EMPTY_CONTEXT);
        planner.clearRelTraitDefs();
        planner.addRelTraitDef(ConventionTraitDef.INSTANCE);
        planner.addRelTraitDef(RelCollationTraitDef.INSTANCE);

        return RelOptCluster.create(planner, rexBuilder);
    }

    CalciteSchema createRootSchema(String schemaName) {
        SchemaPlus rootSchema = Frameworks.createRootSchema(true);
        rootSchema.add(schemaName, new ShardingSphereCalciteSchema(shardingSphereSchema));
        return CalciteSchema.from(rootSchema);
    }

    
}
