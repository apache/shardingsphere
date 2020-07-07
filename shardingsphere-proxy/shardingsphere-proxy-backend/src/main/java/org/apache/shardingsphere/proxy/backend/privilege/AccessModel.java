package org.apache.shardingsphere.shardingproxy.backend.privilege;

import lombok.Getter;
import org.apache.shardingsphere.shardingproxy.backend.privilege.impl.RolePrivilege;
import org.apache.shardingsphere.shardingproxy.backend.privilege.impl.UserPrivilege;
import org.apache.shardingsphere.underlying.common.exception.ShardingSphereException;

import java.util.HashSet;
import java.util.Iterator;

@Getter
public class AccessModel {
    private HashSet<UserPrivilege> usersPrivilege = new HashSet<>();

    private HashSet<RolePrivilege> rolesPrivileges = new HashSet<>();

    public void addUser(UserPrivilege userPrivilege){
        this.getUsersPrivilege().add(userPrivilege);
    }

    public void removeUser(UserPrivilege userPrivilege){
        if(this.getUsersPrivilege().contains(userPrivilege)){
            this.getUsersPrivilege().remove(userPrivilege);
        }
    }

    public void addRole(RolePrivilege rolePrivilege){
        this.getRolesPrivileges().add(rolePrivilege);
    }

    public void removeUser(RolePrivilege rolePrivilege){
        if(this.getRolesPrivileges().contains(rolePrivilege)){
            this.getRolesPrivileges().remove(rolePrivilege);
        }
    }

    public UserPrivilege getUser(String userName){
        Iterator<UserPrivilege> iterator = this.getUsersPrivilege().iterator();
        while (iterator.hasNext()){
            UserPrivilege curUserPrivilege = iterator.next();
            if(curUserPrivilege.getUserName().equals(userName)) return curUserPrivilege;
        }
        throw new ShardingSphereException("No such user named :" + userName);
    }

    public RolePrivilege getRole(String roleName){
        Iterator<RolePrivilege> iterator = this.getRolesPrivileges().iterator();
        while (iterator.hasNext()){
            RolePrivilege curRolePrivilege = iterator.next();
            if(curRolePrivilege.getRoleName().equals(roleName)) return curRolePrivilege;
        }
        throw new ShardingSphereException("No such role named :" + roleName);
    }
}
