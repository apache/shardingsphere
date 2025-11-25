package org.apache.shardingsphere.database.protocol.postgresql.packet.command.query.extended.bind.protocol.text.impl;

import org.apache.shardingsphere.database.protocol.postgresql.packet.command.query.extended.bind.protocol.text.PostgreSQLTextValueParser;
import org.apache.shardingsphere.infra.exception.external.sql.type.wrapper.SQLWrapperException;
import org.postgresql.util.PGobject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;

public final class PostgreSQLUDTValueParser implements PostgreSQLTextValueParser<PGobject> {


    @Override
    public PGobject parse(final String value) {
        if (null == value) {
            return null;
        }
        PGobject obj = new PGobject();
        try {
//            obj.setType(typeName);
            obj.setValue(value);
            return obj;
        } catch (SQLException ex) {
            throw new IllegalArgumentException(
                    String.format("Failed to parse UDT `%s` with value `%s`", "typeName", value), ex);
        }
    }
}