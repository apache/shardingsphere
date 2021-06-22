package org.apache.shardingsphere.authority.provider.custom;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.shardingsphere.authority.model.ShardingSpherePrivileges;
import org.apache.shardingsphere.authority.provider.custom.builder.CustomPrivilegeBuilder;
import org.apache.shardingsphere.authority.spi.AuthorityProvideAlgorithm;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.user.Grantee;
import org.apache.shardingsphere.infra.metadata.user.ShardingSphereUser;

public final class CustomPrivilegesPermittedAuthorityProviderAlgorithm implements AuthorityProvideAlgorithm {
    
    public static final String PROP_USER_SCHEMA_MAPPINGS = "user-schema-mappings";

    private final Map<ShardingSphereUser, ShardingSpherePrivileges> userPrivilegeMap = new ConcurrentHashMap<>();

    private Properties props;

    @Override
    public void setProps(final Properties props) {
        this.props = props;
    }

    @Override
    public Properties getProps() {
        return this.props;
    }

    @Override
    public void init(final Map<String, ShardingSphereMetaData> mataDataMap, final Collection<ShardingSphereUser> users) {
        this.userPrivilegeMap.putAll(CustomPrivilegeBuilder.build(users, props));
    }

    @Override
    public void refresh(final Map<String, ShardingSphereMetaData> mataDataMap, final Collection<ShardingSphereUser> users) {
        this.userPrivilegeMap.putAll(CustomPrivilegeBuilder.build(users, props));
    }

    @Override
    public Optional<ShardingSpherePrivileges> findPrivileges(final Grantee grantee) {
        return userPrivilegeMap.keySet().stream().filter(each -> each.getGrantee().equals(grantee)).findFirst().map(userPrivilegeMap::get);
    }

    @Override
    public String getType() {
        return "CUSTOM_PRIVILEGES_PERMITTED";
    }

}
