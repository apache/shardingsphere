package org.apache.shardingsphere.infra.optimize.planner;

import org.apache.calcite.rel.RelNode;
import org.apache.calcite.sql.SqlNode;
import org.apache.shardingsphere.infra.metadata.schema.ShardingSphereSchema;
import org.apache.shardingsphere.infra.optimize.ExecStmt;
import org.apache.shardingsphere.infra.optimize.converter.RelNodeConverter;
import org.apache.shardingsphere.infra.optimize.converter.SqlNodeConverter;
import org.apache.shardingsphere.sql.parser.sql.common.statement.SQLStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dml.SelectStatement;

import java.util.Optional;

public class Compiler {
    
    /**
     * compile ast to physical plan. 
     * @param schemaName schema name
     * @param shardingSphereSchema schema of logical db
     * @param sqlStatement ast
     * @return Wrapper class for Physical plan
     */
    public static ExecStmt compileQuery(final String schemaName, final ShardingSphereSchema shardingSphereSchema, final SQLStatement sqlStatement) {
        if (!(sqlStatement instanceof SelectStatement)) {
           return new ExecStmt(); 
        }
        Optional<SqlNode> convertSqlStatement = SqlNodeConverter.convertSqlStatement(sqlStatement);
        if (!convertSqlStatement.isPresent()) {
            return new ExecStmt();
        }
        SqlNode sqlNode = convertSqlStatement.get();
        RelNodeConverter relNodeConverter = new RelNodeConverter(schemaName, shardingSphereSchema);
        RelNode relNode = relNodeConverter.validateAndConvert(sqlNode);
        DefaultPlanner defaultPlanner = new DefaultPlanner();
        RelNode finalPlan = defaultPlanner.getPhysicPlan(relNode);
        return new ExecStmt(sqlNode, finalPlan, null);
    }
}
