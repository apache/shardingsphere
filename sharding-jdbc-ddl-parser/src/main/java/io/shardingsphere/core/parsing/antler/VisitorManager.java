package io.shardingsphere.core.parsing.antler;

import java.util.HashMap;
import java.util.Map;

import io.shardingsphere.core.constant.DatabaseType;
import io.shardingsphere.core.parsing.antler.visitor.AlterTableVisitor;
import io.shardingsphere.core.parsing.antler.visitor.CreateTableVisitor;
import io.shardingsphere.core.parsing.antler.visitor.OnlyTableVisitor;
import io.shardingsphere.core.parsing.antler.visitor.SQLVisitor;

public class VisitorManager {
    private Map<String, SQLVisitor> visitors = new HashMap<String, SQLVisitor>();
    private static VisitorManager instance = new VisitorManager();

    private VisitorManager() {
        visitors.put("CreateTable", new CreateTableVisitor());
        visitors.put("AlterTable", new AlterTableVisitor());
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
        String key = dbType.name() + "-" + commandName;
        SQLVisitor visitor = visitors.get(key);
        if (visitor != null) {
            return visitor;
        }

        return visitors.get(commandName);
    }
}
