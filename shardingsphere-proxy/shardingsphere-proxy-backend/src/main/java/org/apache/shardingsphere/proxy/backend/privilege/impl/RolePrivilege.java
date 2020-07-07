package org.apache.shardingsphere.shardingproxy.backend.privilege.impl;

import lombok.Getter;
import lombok.Setter;
import org.apache.shardingsphere.shardingproxy.backend.privilege.Privilege;
import org.apache.shardingsphere.shardingproxy.backend.privilege.PrivilegeExecutorWrapper;
import org.apache.shardingsphere.shardingproxy.backend.privilege.PrivilegeModel;
import org.apache.shardingsphere.underlying.common.exception.ShardingSphereException;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

@Getter
@Setter
public class RolePrivilege extends PrivilegeModel implements PrivilegeExecutorWrapper {
    private String roleName;

    public RolePrivilege(String roleName){
        this.setRoleName(roleName);
    }

    @Override
    public boolean checkPrivilege(String privilegeType, String information) {
        HashSet<Privilege> targetPrivileges = this.chosePrivilegeType(privilegeType);
        String[] splitTargets = information.split("\\.");
        switch (splitTargets.length){
            case 1:
                Iterator<Privilege> iterator = targetPrivileges.iterator();
                while (iterator.hasNext()){
                    Privilege curPrivilege = iterator.next();
                    if(curPrivilege.containsTargetPlace(splitTargets[0])) return true;
                }
                return false;
            case 2:
                return checkPrivilege(privilegeType, splitTargets[0], splitTargets[1]);
            case 3:
                return checkPrivilege(privilegeType, splitTargets[0], splitTargets[1], splitTargets[2]);
            default:
                throw new ShardingSphereException("Invalid privilege format.");
        }
    }

    @Override
    public boolean checkPrivilege(String privilegeType, String database, String table) {
        HashSet<Privilege> targetPrivileges = this.chosePrivilegeType(privilegeType);
        Iterator<Privilege> iterator = targetPrivileges.iterator();
        while (iterator.hasNext()){
            Privilege curPrivilege = iterator.next();
            if(curPrivilege.containsTargetPlace(database,table)) return true;
        }
        return false;
    }

    @Override
    public boolean checkPrivilege(String privilegeType, String database, String table, String column) {
        HashSet<Privilege> targetPrivileges = this.chosePrivilegeType(privilegeType);
        Iterator<Privilege> iterator = targetPrivileges.iterator();
        while (iterator.hasNext()){
            Privilege curPrivilege = iterator.next();
            if(curPrivilege.containsTargetPlace(database,table,column)) return true;
        }
        return false;
    }

    @Override
    public void grant(String privilegeType, String information) {
        Privilege targetPrivilege = new Privilege(information);
        this.addPrivilege(privilegeType,targetPrivilege);
    }

    @Override
    public void grant(String privilegeType, String database, String table) {
        Privilege targetPrivilege = new Privilege(database, table);
        this.addPrivilege(privilegeType, targetPrivilege);
    }

    @Override
    public void grant(String privilegeType, String database, String table, List<String> column) {
        Privilege targetPrivilege = new Privilege(database, table, column);
        this.addPrivilege(privilegeType,targetPrivilege);
    }

    @Override
    public void revoke(String privilegeType, String information) {
        Privilege privilege = new Privilege(information);
        try{
            this.removePrivilege(privilegeType,privilege);
        }
        catch (Exception e){
            throw new ShardingSphereException("there is no such grant defined for role '"+this.getRoleName());
        }
    }

    @Override
    public void revoke(String privilegeType, String database, String table) {
        Privilege privilege = new Privilege(database, table);
        try{
            this.removePrivilege(privilegeType,privilege);
        }
        catch (Exception e){
            throw new ShardingSphereException("there is no such grant defined for role '"+this.getRoleName());
        }
    }

    @Override
    public void revoke(String privilegeType, String database, String table, List<String> column) {
        Privilege privilege = new Privilege(database, table, column);
        try{
            this.removePrivilege(privilegeType,privilege);
        }
        catch (Exception e){
            throw new ShardingSphereException("there is no such grant defined for role '"+this.getRoleName());
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        RolePrivilege that = (RolePrivilege) o;
        return Objects.equals(roleName, that.roleName) &&
                super.equals(that);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), roleName);
    }
}
