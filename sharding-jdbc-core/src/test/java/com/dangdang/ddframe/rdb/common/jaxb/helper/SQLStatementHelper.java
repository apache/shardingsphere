package com.dangdang.ddframe.rdb.common.jaxb.helper;

import com.dangdang.ddframe.rdb.common.jaxb.SQLStatement;
import com.dangdang.ddframe.rdb.common.jaxb.SQLStatements;
import com.dangdang.ddframe.rdb.integrate.jaxb.helper.SQLAssertJAXBHelper;
import com.dangdang.ddframe.rdb.sharding.constant.DatabaseType;
import com.google.common.collect.Sets;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import java.io.File;
import java.net.URL;
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
        URL url = SQLAssertJAXBHelper.class.getClassLoader().getResource("sql");
        if (url == null) {
            return;
        }
        File filePath = new File(url.getPath());
        if (!filePath.exists()) {
            return;
        }
        File[] files = filePath.listFiles();
        if (null == files) {
            return;
        }
        for (File each : files) {
            try {
                SQLStatements statements = (SQLStatements) JAXBContext.newInstance(SQLStatements.class).createUnmarshaller().unmarshal(each);
                for (SQLStatement statement : statements.getSqls()) {
                    String id = statement.getId();
                    if (statementMap.containsKey(id)) {
                        throw new RuntimeException("Existed sql assert id with:" + id);
                    }
                    statementMap.put(id, statement);
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
