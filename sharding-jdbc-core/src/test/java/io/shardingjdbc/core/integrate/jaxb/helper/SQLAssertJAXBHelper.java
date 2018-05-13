package io.shardingjdbc.core.integrate.jaxb.helper;

import io.shardingjdbc.core.common.jaxb.helper.SQLStatementHelper;
import io.shardingjdbc.core.constant.DatabaseType;
import io.shardingjdbc.core.constant.SQLType;
import io.shardingjdbc.core.integrate.jaxb.SQLAssert;
import io.shardingjdbc.core.integrate.jaxb.SQLAsserts;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class SQLAssertJAXBHelper {
    
    public static Collection<Object[]> getDataParameters(final String filePath, final SQLType sqlType) {
        Collection<Object[]> result = new ArrayList<>();
        URL url = SQLAssertJAXBHelper.class.getClassLoader().getResource(filePath);
        if (null == url) {
            return Collections.emptyList();
        }
        File assertFilePath = new File(url.getPath());
        if (!assertFilePath.exists()) {
            return Collections.emptyList();
        }
        
        if (assertFilePath.isDirectory()) {
            File[] files = assertFilePath.listFiles();
            if (null == files) {
                return Collections.emptyList();
            }
            for (File each : files) {
                if (each.isDirectory()) {
                    continue;
                }
                if (isTypeMatched(each.getName(), sqlType)) {
                    result.addAll(dataParameters(each));
                }
            }
        } else {
            if (isTypeMatched(assertFilePath.getName(), sqlType)) {
                result.addAll(dataParameters(assertFilePath));
            }
        }
        return result;
    }
    
    private static Collection<Object[]> dataParameters(final File file) {
        SQLAsserts asserts = loadSQLAsserts(file);
        List<Object[]> result = new ArrayList<>();
        for (int i = 0; i < asserts.getSqlAsserts().size(); i++) {
            SQLAssert assertObj = asserts.getSqlAsserts().get(i);
            for (DatabaseType each : SQLStatementHelper.getTypes(assertObj.getId())) {
                result.add(getDataParameter(assertObj, each));
            }
        }
        return result;
    }
    
    private static SQLAsserts loadSQLAsserts(final File file) {
        try {
            return (SQLAsserts) JAXBContext.newInstance(SQLAsserts.class).createUnmarshaller().unmarshal(file);
        } catch (final JAXBException ex) {
            throw new RuntimeException(ex);
        }
    }
    
    private static Object[] getDataParameter(final SQLAssert sqlAssert, final DatabaseType dbType) {
        final Object[] result = new Object[4];
        result[0] = sqlAssert.getId();
        result[1] = SQLStatementHelper.getSql(sqlAssert.getId());
        result[2] = dbType;
        result[3] = sqlAssert.getSqlShardingRules();
        return result;
    }
    
    private static boolean isTypeMatched(final String fileName, final SQLType sqlType) {
        switch (sqlType) {
            case DDL:
                return fileName.startsWith("alter") || fileName.startsWith("create") || fileName.startsWith("drop") || fileName.startsWith("truncate");
            case DML:
                return fileName.startsWith("delete") || fileName.startsWith("insert") || fileName.startsWith("update");
            case DQL:
                return fileName.startsWith("select");
            default: return false;
        }
    }
}
