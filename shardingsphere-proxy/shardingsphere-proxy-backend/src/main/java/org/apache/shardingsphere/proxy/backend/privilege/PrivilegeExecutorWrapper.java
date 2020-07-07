package org.apache.shardingsphere.shardingproxy.backend.privilege;

import java.util.List;

public interface PrivilegeExecutorWrapper {
    public boolean checkPrivilege(String privilegeType, String database, String table, String column);

    public boolean checkPrivilege(String privilegeType, String database, String table);

    public boolean checkPrivilege(String privilegeType, String information);

    public void grant(String privilegeType, String database, String table, List<String> column);

    public void grant(String privilegeType, String database, String table);

    public void grant(String privilegeType, String information);

    public void revoke(String privilegeType, String database, String table, List<String> column);

    public void revoke(String privilegeType, String database, String table);

    public void revoke(String privilegeType, String information);
}
