package org.apache.shardingsphere.core.util;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

import javax.sql.DataSource;

import org.apache.shardingsphere.core.exception.UnrecognizeDatasourceInfoException;
import org.apache.shardingsphere.core.metadata.datasource.DataSourceInfo;

public class DataSourceUtil {
    public static DataSourceInfo getDataSourceInfo(final DataSource dataSource) {
        try {
            DataSourceInfo sourceInfo = new DataSourceInfo();
            String username = null;
            Field[] fields = dataSource.getClass().getSuperclass().getDeclaredFields();
            for (Field field : fields) {
                if (!Modifier.isStatic(field.getModifiers())) {
                    if (field.getName().toLowerCase().contains("url")) {
                        field.setAccessible(true);
                        String jdbcurl = field.get(dataSource) == null ? null : field.get(dataSource).toString();
                        sourceInfo.setUrl(jdbcurl);
                    }
                    if (field.getName().toLowerCase().contains("username")) {
                        field.setAccessible(true);
                        username = field.get(dataSource) == null ? null : field.get(dataSource).toString();
                        sourceInfo.setUsername(username);
                    }
                }
            }
            return sourceInfo;
        }catch (Exception e) {
            throw new UnrecognizeDatasourceInfoException(e);
        }
    }
}
