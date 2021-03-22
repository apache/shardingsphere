package org.apache.shardingsphere.infra.optimize.util;

import com.google.common.collect.ImmutableMap;
import org.apache.calcite.plan.RelTraitDef;
import org.apache.calcite.schema.SchemaPlus;
import org.apache.calcite.schema.Table;
import org.apache.calcite.schema.impl.AbstractSchema;
import org.apache.calcite.sql.parser.SqlParser;
import org.apache.calcite.tools.FrameworkConfig;
import org.apache.calcite.tools.Frameworks;
import org.apache.calcite.tools.Programs;
import org.apache.calcite.tools.RelBuilder;
import org.apache.shardingsphere.infra.metadata.schema.model.ColumnMetaData;
import org.apache.shardingsphere.infra.metadata.schema.model.TableMetaData;
import org.apache.shardingsphere.infra.optimize.schema.table.ShardingSphereCalciteTable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class RelBuilderTest {
    
    public static Frameworks.ConfigBuilder config() {
        final SchemaPlus rootSchema = Frameworks.createRootSchema(true);
        return Frameworks.newConfigBuilder()
                .parserConfig(SqlParser.Config.DEFAULT)
                .defaultSchema(rootSchema.add("test", new AbstractSchema() {
                    @Override
                    protected Map<String, Table> getTableMap() {
                        Collection<ColumnMetaData> columnMetaDataList = new ArrayList<>();
                        ColumnMetaData columnMetaData = new ColumnMetaData("c", 4, true, false, false);
                        TableMetaData tableMetaData = new TableMetaData(Arrays.asList(columnMetaData), Collections.emptyList());
                        ShardingSphereCalciteTable table = new ShardingSphereCalciteTable("EMP", tableMetaData);
                        return ImmutableMap.of(table.getTableName(), table);
                    }
                }))
                .traitDefs((List<RelTraitDef>) null)
                .programs(Programs.heuristicJoinOrder(Programs.RULE_SET, true, 2));
    }
    
    public static RelBuilder createRelBuilder() {
        FrameworkConfig config = RelBuilderTest.config().build();
        return RelBuilder.create(config);
    }
}
