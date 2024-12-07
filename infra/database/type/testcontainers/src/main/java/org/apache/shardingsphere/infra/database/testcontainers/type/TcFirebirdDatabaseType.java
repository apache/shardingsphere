package org.apache.shardingsphere.infra.database.testcontainers.type;
import org.apache.shardingsphere.infra.database.core.type.DatabaseType;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;

import java.util.Collection;
import java.util.Collections;
import java.util.Optional;

/**
 * Database type of Firebird in testcontainers-java.
 */
public final class TcFirebirdDatabaseType implements TestcontainersDatabaseType {
    @Override
    public Collection<String> getJdbcUrlPrefixes() {
        return Collections.singleton("jdbc:tc:firebird:");
    }

    @Override
    public Optional<DatabaseType> getTrunkDatabaseType() {
        return Optional.of(TypedSPILoader.getService(DatabaseType.class, "Firebird"));
    }

    @Override
    public String getType() {
        return "TC-Firebird";
    }
}
