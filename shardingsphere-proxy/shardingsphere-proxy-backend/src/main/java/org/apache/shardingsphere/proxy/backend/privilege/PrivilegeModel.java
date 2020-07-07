package org.apache.shardingsphere.shardingproxy.backend.privilege;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.shardingsphere.underlying.common.exception.ShardingSphereException;

import java.util.HashSet;
import java.util.Objects;

/**
 * Privilege model.
 */
@Getter
@Setter
public class PrivilegeModel {
    public final static int INITIAL_PRIVILEGE_LENGTH = 8;
    // grant create delete(drop) update select
    protected HashSet<Privilege> grantPrivileges = new HashSet<>(PrivilegeModel.INITIAL_PRIVILEGE_LENGTH)
            ,insertPrivileges = new HashSet<>(PrivilegeModel.INITIAL_PRIVILEGE_LENGTH)
            ,deletePrivileges = new HashSet<>(PrivilegeModel.INITIAL_PRIVILEGE_LENGTH)
            ,updatePrivileges = new HashSet<>(PrivilegeModel.INITIAL_PRIVILEGE_LENGTH)
            ,selectPrivileges = new HashSet<>(PrivilegeModel.INITIAL_PRIVILEGE_LENGTH);

    protected HashSet<Privilege> chosePrivilegeType(String privilegeType){
        switch (privilegeType){
            case "grant":
                return this.getGrantPrivileges();
            case "create":
                return this.getInsertPrivileges();
            case "delete":
                return this.getDeletePrivileges();
            case "update":
                return this.getUpdatePrivileges();
            case "select":
                return this.getSelectPrivileges();
            default:
                throw new ShardingSphereException("Can not match privilege type");
        }
    }

    protected void addPrivilege(String privilegeType,Privilege privilege){
        HashSet<Privilege> targetPrivileges = chosePrivilegeType(privilegeType);
        targetPrivileges.add(privilege);
    }

    protected void removePrivilege(String privilegeType,Privilege privilege){
        HashSet<Privilege> targetPrivileges = chosePrivilegeType(privilegeType);
        if(targetPrivileges.contains(privilege)){
            targetPrivileges.remove(privilege);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PrivilegeModel that = (PrivilegeModel) o;
        return Objects.equals(grantPrivileges, that.grantPrivileges) &&
                Objects.equals(insertPrivileges, that.insertPrivileges) &&
                Objects.equals(deletePrivileges, that.deletePrivileges) &&
                Objects.equals(updatePrivileges, that.updatePrivileges) &&
                Objects.equals(selectPrivileges, that.selectPrivileges);
    }

    @Override
    public int hashCode() {
        return Objects.hash(grantPrivileges, insertPrivileges, deletePrivileges, updatePrivileges, selectPrivileges);
    }
}
