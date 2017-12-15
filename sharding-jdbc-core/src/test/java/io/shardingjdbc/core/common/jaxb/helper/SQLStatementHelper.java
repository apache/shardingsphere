package io.shardingjdbc.core.common.jaxb.helper;

import io.shardingjdbc.core.common.jaxb.SQLStatement;
import io.shardingjdbc.core.common.jaxb.SQLStatements;
import io.shardingjdbc.core.integrate.jaxb.helper.SQLAssertJAXBHelper;
import io.shardingjdbc.core.constant.DatabaseType;
import com.google.common.collect.Sets;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import java.io.File;
import java.net.URL;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class SQLStatementHelper {
    
    private static final Map<String, SQLStatement> STATEMENT_MAP;
    
    private static final Map<String, SQLStatement> UNSUPPORTED_STATEMENT_MAP;
    
    static {
        STATEMENT_MAP = loadSqlStatements("sql");
        UNSUPPORTED_STATEMENT_MAP = loadSqlStatements("sql/unsupported");
    }
    
    private static Map<String, SQLStatement> loadSqlStatements(final String directory) {
        Map<String, SQLStatement> result = new HashMap<>();
        URL url = SQLAssertJAXBHelper.class.getClassLoader().getResource(directory);
        if (null == url) {
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
                for (File file : each.listFiles()) {
                    fillStatementMap(result, file);
                }
            } else {
                fillStatementMap(result, each);
            }
        }
        return result;
    }
    
    private static void fillStatementMap(final Map<String, SQLStatement> result, final File each) {
        try {
            SQLStatements statements = (SQLStatements) JAXBContext.newInstance(SQLStatements.class).createUnmarshaller().unmarshal(each);
            for (SQLStatement statement : statements.getSqls()) {
                result.put(statement.getId(), statement);
            }
        } catch (final JAXBException ex) {
            throw new RuntimeException(ex);
        }
    }
    
    public static Collection<SQLStatement> getUnsupportedSqlStatements() {
        return UNSUPPORTED_STATEMENT_MAP.values();
    }
    
    public static String getSql(final String sqlId) {
        checkSqlId(sqlId);
        SQLStatement statement = STATEMENT_MAP.get(sqlId);
        return statement.getSql();
    }
    
    public static Set<DatabaseType> getTypes(final String sqlId) {
        checkSqlId(sqlId);
        SQLStatement statement = STATEMENT_MAP.get(sqlId);
        if (null == statement.getTypes()) {
            return Sets.newHashSet(DatabaseType.values());
        }
        Set<DatabaseType> result = new HashSet<>();
        for (String each : statement.getTypes().split(",")) {
            result.add(DatabaseType.valueOf(each));
        }
        return result;
    }
    
    private static void checkSqlId(final String sqlId) {
        if (null == sqlId || !STATEMENT_MAP.containsKey(sqlId)) {
            throw new RuntimeException("Can't find sql of id:" + sqlId);
        }
    }
}
