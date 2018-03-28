/*
 * Copyright 1999-2015 dangdang.com.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package io.shardingjdbc.core.integrate.jaxb.helper;

import com.google.common.base.Strings;
import com.google.common.collect.Sets;
import io.shardingjdbc.core.constant.DatabaseType;
import io.shardingjdbc.core.constant.SQLType;
import io.shardingjdbc.core.integrate.jaxb.SQLAssert;
import io.shardingjdbc.core.integrate.jaxb.SQLAsserts;
import io.shardingjdbc.test.sql.SQLCasesLoader;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class SQLAssertJAXBHelper {
    
    /**
     * Get data parameters.
     * 
     * @param filePath file path
     * @param sqlType SQL type
     * @return data parameters
     */
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
            for (DatabaseType each : getDatabaseTypes(SQLCasesLoader.getInstance().getDatabaseTypes(assertObj.getId()))) {
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
        result[1] = SQLCasesLoader.getInstance().getSupportedSQL(sqlAssert.getId());
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
    
    private static Collection<DatabaseType> getDatabaseTypes(final String databaseTypes) {
        if (Strings.isNullOrEmpty(databaseTypes)) {
            return Sets.newHashSet(DatabaseType.values());
        }
        Set<DatabaseType> result = new HashSet<>();
        for (String each : databaseTypes.split(",")) {
            result.add(DatabaseType.valueOf(each));
        }
        return result;
    }
}
