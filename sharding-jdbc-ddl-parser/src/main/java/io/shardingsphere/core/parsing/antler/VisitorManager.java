package io.shardingsphere.core.parsing.antler;

import java.util.HashMap;
import java.util.Map;

import io.shardingsphere.core.constant.DatabaseType;
import io.shardingsphere.core.parsing.antler.visitor.CreateTableVisitor;
import io.shardingsphere.core.parsing.antler.visitor.OnlyTableVisitor;
import io.shardingsphere.core.parsing.antler.visitor.SQLVisitor;
import io.shardingsphere.core.parsing.antler.visitor.mysql.MySQLAlterTableVisitor;
import io.shardingsphere.core.parsing.antler.visitor.oracle.OracleAlterTableVisitor;
import io.shardingsphere.core.parsing.antler.visitor.postgre.PostgreAlterTableVisitor;
import io.shardingsphere.core.parsing.antler.visitor.sqlserver.SQLServerAlterTableVisitor;

public class VisitorManager {
    private Map<String, SQLVisitor> visitors = new HashMap<String, SQLVisitor>();
    private static VisitorManager instance = new VisitorManager();

    private VisitorManager() {
        visitors.put("CreateTable", new CreateTableVisitor());
        visitors.put(DatabaseType.MySQL+"AlterTable", new MySQLAlterTableVisitor());
        visitors.put(DatabaseType.PostgreSQL+"AlterTable", new PostgreAlterTableVisitor());
        visitors.put(DatabaseType.Oracle+"AlterTable", new OracleAlterTableVisitor());
        visitors.put(DatabaseType.SQLServer+"AlterTable", new SQLServerAlterTableVisitor());
        visitors.put("DropTable", new OnlyTableVisitor());
        visitors.put("TruncateTable", new OnlyTableVisitor());
    }

    public static VisitorManager getInstance() {
        return instance;
    }

    /**
     * get sql visitor
     * 
     * @param dbType
     * @param commandName
     * @return
     */
    public SQLVisitor getVisitor(final DatabaseType dbType, final String commandName) {
        String key = dbType.name() + commandName;
        SQLVisitor visitor = visitors.get(key);
        if (visitor != null) {
            return visitor;
        }

        return visitors.get(commandName);
    }
}
