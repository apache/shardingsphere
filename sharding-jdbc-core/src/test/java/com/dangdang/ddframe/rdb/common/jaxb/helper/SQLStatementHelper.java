package com.dangdang.ddframe.rdb.common.jaxb.helper;

import com.dangdang.ddframe.rdb.common.jaxb.SQLStatement;
import com.dangdang.ddframe.rdb.common.jaxb.SQLStatements;
import com.dangdang.ddframe.rdb.integrate.jaxb.helper.SQLAssertJAXBHelper;
import com.dangdang.ddframe.rdb.sharding.constant.DatabaseType;
import com.google.common.collect.Sets;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class SQLStatementHelper {
    
    private static Map<String, SQLStatement> statementMap = new HashMap<>();
    
    static {
        loadSqlStatements();
    }
    
    private static void loadSqlStatements() {
        File filePath = new File(SQLAssertJAXBHelper.class.getClassLoader().getResource("sql").getPath());
        if (!filePath.exists()) {
            return;
        }
        for (File each : filePath.listFiles()) {
            try {
                SQLStatements statements = (SQLStatements) JAXBContext.newInstance(SQLStatements.class).createUnmarshaller().unmarshal(each);
                for (SQLStatement statement : statements.getSqls()) {
                    statementMap.put(statement.getId(), statement);
                }
            } catch (final JAXBException ex) {
                throw new RuntimeException(ex);
            }
        }
    }
    
    public static String getSql(final String sqlId) {
        return statementMap.get(sqlId).getSql();
    }
    
    public static Set<DatabaseType> getTypes(final String sqlId) {
        if (null == sqlId || !statementMap.containsKey(sqlId)) {
            return Collections.emptySet();
        }
        SQLStatement statement = statementMap.get(sqlId);
        if (null == statement.getTypes()) {
            return Sets.newHashSet(DatabaseType.values());
        }
        Set<DatabaseType> result = new HashSet<>();
        for (String each : statement.getTypes().split(",")) {
            result.add(DatabaseType.valueOf(each));
        }
        return result;
    }
}
