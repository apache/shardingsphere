/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.shardingsphere.authority.loader.storage.impl.dialect;

import org.apache.shardingsphere.authority.loader.storage.impl.StoragePrivilegeLoader;
import org.apache.shardingsphere.authority.model.PrivilegeType;
import org.apache.shardingsphere.authority.model.ShardingSpherePrivileges;
import org.apache.shardingsphere.authority.model.database.SchemaPrivileges;
import org.apache.shardingsphere.authority.model.database.TablePrivileges;
import org.apache.shardingsphere.infra.metadata.user.Grantee;
import org.apache.shardingsphere.infra.metadata.user.ShardingSphereUser;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * SQLServer privilege loader.
 */
public final class SQLServerPrivilegeLoader implements StoragePrivilegeLoader {

    private static final String SCHEMA_PRIVILEGE_SQL =
            "SELECT pr.name AS GRANTEE, pe.state_desc AS STATE, pe.permission_name AS PRIVILEGE_TYPE "
                    + "FROM sys.database_principals AS pr JOIN sys.database_permissions AS pe "
                    + "ON pe.grantee_principal_id = pr.principal_id WHERE pr.name IN (%s)";

    private static final String TABLE_PRIVILEGE_SQL =
            "SELECT GRANTOR, GRANTEE, TABLE_CATALOG, TABLE_SCHEMA, TABLE_NAME, PRIVILEGE_TYPE, IS_GRANTABLE from INFORMATION_SCHEMA.TABLE_PRIVILEGES WHERE GRANTEE IN (%s)";

    @Override
    public Map<ShardingSphereUser, ShardingSpherePrivileges> load(final Collection<ShardingSphereUser> users, final DataSource dataSource) throws SQLException {
        Map<ShardingSphereUser, ShardingSpherePrivileges> result = new LinkedHashMap<>();
        users.forEach(user -> result.put(user, new ShardingSpherePrivileges()));
        fillSchemaPrivileges(result, dataSource, users);
        fillTablePrivileges(result, dataSource, users);
        return result;
    }

    private void fillSchemaPrivileges(final Map<ShardingSphereUser, ShardingSpherePrivileges> userPrivilegeMap,
                                      final DataSource dataSource, final Collection<ShardingSphereUser> users) throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            Statement statement = connection.createStatement();
            try (ResultSet resultSet = statement.executeQuery(getSchemaPrivilegesSQL(users))) {
                while (resultSet.next()) {
                    fillSchemaPrivileges(userPrivilegeMap, resultSet);
                }
            }
        }
    }

    private void fillSchemaPrivileges(final Map<ShardingSphereUser, ShardingSpherePrivileges> userPrivilegeMap, final ResultSet resultSet) throws SQLException {
        Optional<ShardingSphereUser> user = findShardingSphereUser(userPrivilegeMap, resultSet);
        if (user.isPresent()) {
            userPrivilegeMap.get(user.get()).getAdministrativePrivileges().getPrivileges().addAll(loadSchemaPrivileges(resultSet));
        }
    }

    private void fillTablePrivileges(final Map<ShardingSphereUser, ShardingSpherePrivileges> userPrivilegeMap,
                                     final DataSource dataSource, final Collection<ShardingSphereUser> users) throws SQLException {
        Map<ShardingSphereUser, Map<String, Map<String, List<PrivilegeType>>>> privilegeCache = new HashMap<>();
        try (Connection connection = dataSource.getConnection()) {
            Statement statement = connection.createStatement();
            try (ResultSet resultSet = statement.executeQuery(getTablePrivilegesSQL(users))) {
                while (resultSet.next()) {
                    collectPrivileges(privilegeCache, resultSet);
                }
            }
        }
        fillTablePrivileges(privilegeCache, userPrivilegeMap);
    }

    private void fillTablePrivileges(final Map<ShardingSphereUser, Map<String, Map<String, List<PrivilegeType>>>> privilegeCache,
                                     final Map<ShardingSphereUser, ShardingSpherePrivileges> userPrivilegeMap) {
        for (Map.Entry<ShardingSphereUser, Map<String, Map<String, List<PrivilegeType>>>> entry : privilegeCache.entrySet()) {
            for (String db : entry.getValue().keySet()) {
                for (String tableName : entry.getValue().get(db).keySet()) {
                    TablePrivileges tablePrivileges = new TablePrivileges(tableName, entry.getValue().get(db).get(tableName));
                    ShardingSpherePrivileges privileges = userPrivilegeMap.get(entry.getKey());
                    if (!privileges.getDatabasePrivileges().getSpecificPrivileges().containsKey(db)) {
                        privileges.getDatabasePrivileges().getSpecificPrivileges().put(db, new SchemaPrivileges(db));
                    }
                    privileges.getDatabasePrivileges().getSpecificPrivileges().get(db).getSpecificPrivileges().put(tableName, tablePrivileges);
                }
            }
        }
    }

    private void collectPrivileges(final Map<ShardingSphereUser, Map<String, Map<String, List<PrivilegeType>>>> privilegeCache, final ResultSet resultSet) throws SQLException {
        String db = resultSet.getString("TABLE_CATALOG");
        String tableName = resultSet.getString("TABLE_NAME");
        String privilegeType = resultSet.getString("PRIVILEGE_TYPE");
        boolean hasPrivilege = resultSet.getString("IS_GRANTABLE").equalsIgnoreCase("YES");
        String grantee = resultSet.getString("GRANTEE");
        if (hasPrivilege) {
            privilegeCache
                    .computeIfAbsent(new ShardingSphereUser(grantee, "", ""), k -> new HashMap<>())
                    .computeIfAbsent(db, k -> new HashMap<>())
                    .computeIfAbsent(tableName, k -> new ArrayList<>())
                    .add(getPrivilegeType(privilegeType));
        }
    }

    private Optional<ShardingSphereUser> findShardingSphereUser(final Map<ShardingSphereUser, ShardingSpherePrivileges> privileges, final ResultSet resultSet) throws SQLException {
        Grantee grantee = new Grantee(resultSet.getString("GRANTEE"), "");
        return privileges.keySet().stream().filter(each -> each.getGrantee().equals(grantee)).findFirst();
    }

    private Collection<PrivilegeType> loadSchemaPrivileges(final ResultSet resultSet) throws SQLException {
        Collection<PrivilegeType> result = new LinkedList<>();
        if ("GRANT".equals(resultSet.getString("STATE"))) {
            result.add(getPrivilegeType(resultSet.getString("PRIVILEGE_TYPE")));
        }
        return result;
    }

    private String getSchemaPrivilegesSQL(final Collection<ShardingSphereUser> users) {
        String userList = users.stream().map(each -> String.format("'%s'", each.getGrantee().getUsername()))
                .collect(Collectors.joining(","));
        return String.format(SCHEMA_PRIVILEGE_SQL, userList);
    }

    private String getTablePrivilegesSQL(final Collection<ShardingSphereUser> users) {
        String userList = users.stream().map(each -> String.format("'%s'", each.getGrantee().getUsername())).collect(Collectors.joining(", "));
        return String.format(TABLE_PRIVILEGE_SQL, userList);
    }

    private PrivilegeType getPrivilegeType(final String privilege) {
        switch (privilege) {
            case "SELECT":
                return PrivilegeType.SELECT;
            case "INSERT":
                return PrivilegeType.INSERT;
            case "UPDATE":
                return PrivilegeType.UPDATE;
            case "DELETE":
                return PrivilegeType.DELETE;
            case "CREATE FUNCTION":
                return PrivilegeType.CREATE_FUNCTION;
            case "REFERENCES":
                return PrivilegeType.REFERENCES;
            case "CREATE TABLE":
                return PrivilegeType.CREATE_TABLE;
            case "CREATE DATABASE":
                return PrivilegeType.CREATE_DATABASE;
            case "EXECUTE":
                return PrivilegeType.EXECUTE;
            case "CREATE VIEW":
                return PrivilegeType.CREATE_VIEW;
            case "CREATE PROCEDURE":
                return PrivilegeType.CREATE_PROC;
            case "BACKUP DATABASE":
                return PrivilegeType.BACKUP_DATABASE;
            case "CREATE DEFAULT":
                return PrivilegeType.CREATE_DEFAULT;
            case "BACKUP LOG":
                return PrivilegeType.BACKUP_LOG;
            case "CREATE RULE":
                return PrivilegeType.CREATE_RULE;
            case "CONNECT":
                return PrivilegeType.CONNECT;
            default:
                throw new UnsupportedOperationException(privilege);
        }
    }

    @Override
    public String getType() {
        return "SQLServer";
    }
}
