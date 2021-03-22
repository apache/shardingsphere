package org.apache.shardingsphere.infra.optimize.converter;

import org.apache.calcite.rel.RelNode;
import org.apache.calcite.rel.logical.LogicalFilter;
import org.apache.calcite.sql.fun.SqlStdOperatorTable;
import org.apache.calcite.tools.RelBuilder;
import org.apache.shardingsphere.infra.optimize.rel.CustomLogicalRelConverter;
import org.apache.shardingsphere.infra.optimize.rel.logical.LogicalScan;
import org.apache.shardingsphere.infra.optimize.util.RelBuilderTest;
import org.junit.Assert;
import org.junit.Test;

public class CustomLogicalRelConverterTest {
    
    @Test
    public void testConvertTableScan() {
        RelBuilder relBuilder = RelBuilderTest.createRelBuilder();
        relBuilder.scan("EMP");
        relBuilder.filter(relBuilder.call(SqlStdOperatorTable.GREATER_THAN,
                relBuilder.field("c"),
                relBuilder.literal(10)));
        RelNode relNode = relBuilder.build();
        RelNode res = CustomLogicalRelConverter.convert(relNode);
        Assert.assertTrue(res instanceof LogicalFilter);
        Assert.assertTrue(((LogicalFilter) res).getInput() instanceof LogicalScan);
    }
    
    
}
