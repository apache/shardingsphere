package org.apache.shardingsphere.infra.distsql.exception.rule;

import java.util.Collection;

public class RequiredKeyGeneratorMissedException extends RuleDefinitionViolationException {

    private static final long serialVersionUID = -2391552466149640249L;

    public RequiredKeyGeneratorMissedException(final String type, final String schemaName, final Collection<String> keyGeneratorNames) {
        super(1118, String.format("%s key generator `%s` do not exist in schema `%s`.", type, keyGeneratorNames, schemaName));
    }
    
}
