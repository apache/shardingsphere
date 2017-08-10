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
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class SQLStatementHelper {
    
    private static final Map<String, SQLStatement> statementMap;
    
    private static final Map<String, SQLStatement> unsuppportedStatementMap;
    
    static {
        statementMap = loadSqlStatements("sql");
        unsuppportedStatementMap = loadSqlStatements("sql/unsupported");
    }
    
    private static Map<String, SQLStatement> loadSqlStatements(final String directory) {
        Map<String, SQLStatement> result = new HashMap<>();
        URL url = SQLAssertJAXBHelper.class.getClassLoader().getResource(directory);
        if (url == null) {
            return result;
        }
        File filePath = new File(url.getPath());
        if (!filePath.exists()) {
            return result;
        }
        File[] files = filePath.listFiles();
        if (null == files) {
            return result;
        }
        for (File each : files) {
            if (each.isDirectory()) {
                continue;
            }
            try {
                SQLStatements statements = (SQLStatements) JAXBContext.newInstance(SQLStatements.class).createUnmarshaller().unmarshal(each);
                for (SQLStatement statement : statements.getSqls()) {
                    result.put(statement.getId(), statement);
                }
            } catch (final JAXBException ex) {
                throw new RuntimeException(ex);
            }
        }
        return result;
    }
    
    public static Collection<SQLStatement> getUnsupportedSqlStatements() {
        return unsuppportedStatementMap.values();
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
