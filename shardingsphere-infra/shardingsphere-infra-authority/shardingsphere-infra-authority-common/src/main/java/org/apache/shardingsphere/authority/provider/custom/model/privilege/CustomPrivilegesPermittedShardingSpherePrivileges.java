package org.apache.shardingsphere.authority.provider.custom.model.privilege;

import java.util.Collection;
import java.util.Set;

import org.apache.shardingsphere.authority.model.AccessSubject;
import org.apache.shardingsphere.authority.model.PrivilegeType;
import org.apache.shardingsphere.authority.model.ShardingSpherePrivileges;
import org.apache.shardingsphere.authority.provider.natived.model.subject.SchemaAccessSubject;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class CustomPrivilegesPermittedShardingSpherePrivileges implements ShardingSpherePrivileges{

	private Set<String> schemas;
	
	@Override
	public void setSuperPrivilege() {
		
	}

	@Override
	public boolean hasPrivileges(String schema) {
		return schemas.contains(schema);
	}

	@Override
	public boolean hasPrivileges(Collection<PrivilegeType> privileges) {
		return true;
	}

	@Override
	public boolean hasPrivileges(AccessSubject accessSubject, Collection<PrivilegeType> privileges) {
		if (accessSubject instanceof SchemaAccessSubject) {
			return hasPrivileges(((SchemaAccessSubject) accessSubject).getSchema());
		}
		throw new UnsupportedOperationException(accessSubject.getClass().getCanonicalName());
	}

}
