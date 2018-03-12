package io.shardingjdbc.dbtest.common;

import io.shardingjdbc.core.constant.DatabaseType;

public class DatabaseTypeUtils {


    public static DatabaseType getDatabaseType(String type){

        DatabaseType[] databaseTypes =  DatabaseType.values();
        for (DatabaseType databaseType : databaseTypes) {
            if(type.equalsIgnoreCase(databaseType.name())){
                return databaseType;
            }
        }
        return null;
    }

}
