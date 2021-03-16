package org.apache.shardingsphere.infra.optimize.planner;

import org.apache.calcite.rel.RelNode;
import org.apache.calcite.sql.SqlNode;
import org.apache.shardingsphere.infra.binder.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.metadata.schema.ShardingSphereSchema;
import org.apache.shardingsphere.infra.optimize.ExecStmt;
import org.apache.shardingsphere.infra.optimize.converter.RelNodeConverter;
import org.apache.shardingsphere.infra.optimize.converter.SqlNodeConverter;

import java.util.Optional;

public class Compiler {
    
    public static ExecStmt compileQuery(String schemaName, ShardingSphereSchema shardingSphereSchema, SQLStatementContext statementContext) {
        Optional<SqlNode> convertSqlStatement = SqlNodeConverter.convertSqlStatement(statementContext);
        if(!convertSqlStatement.isPresent()) {
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
