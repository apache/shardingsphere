package org.apache.shardingsphere.encrypt.context;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.encrypt.rule.EncryptColumn;
import org.apache.shardingsphere.encrypt.rule.EncryptRule;
import org.apache.shardingsphere.encrypt.spi.context.EncryptContext;

import java.util.Optional;

/**
 * Encrypt context builder.
 */
@NoArgsConstructor(access = AccessLevel.NONE)
public final class EncryptContextBuilder {
    
    public static EncryptContext build(final String schemaName, final String tableName, final String columnName, final EncryptRule encryptRule) {
        EncryptContext result = new EncryptContext(schemaName, tableName, columnName);
        Optional<EncryptColumn> encryptColumn = encryptRule.findEncryptTable(tableName).flatMap(optional -> optional.findEncryptColumn(columnName));
        encryptColumn.ifPresent(optional -> setEncryptDataType(result, optional));
        return result;
    }
    
    private static void setEncryptDataType(final EncryptContext result, final EncryptColumn encryptColumn) {
        result.setLogicDataType(encryptColumn.getLogicDataType());
        result.setPlainDataType(encryptColumn.getPlainDataType());
        result.setCipherDataType(encryptColumn.getCipherDataType());
        result.setAssistedQueryDataType(encryptColumn.getAssistedQueryDataType());
    }
}
