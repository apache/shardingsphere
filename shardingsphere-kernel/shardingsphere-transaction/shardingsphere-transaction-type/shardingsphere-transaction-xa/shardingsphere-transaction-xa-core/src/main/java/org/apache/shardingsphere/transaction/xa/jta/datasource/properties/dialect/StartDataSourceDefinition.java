package org.apache.shardingsphere.transaction.xa.jta.datasource.properties.dialect;

import org.apache.shardingsphere.transaction.xa.jta.datasource.properties.XADataSourceDefinition;

import java.util.Collection;
import java.util.Collections;

public class StartDataSourceDefinition implements XADataSourceDefinition {
    @Override
    public Collection<String> getXADriverClassName() {
        return Collections.emptyList();
    }

    @Override
    public String getType() {
        return "START-DB";
    }
}
