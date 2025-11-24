package org.apache.shardingsphere.database.protocol.postgresql.packet.command.query.extended.bind.protocol.text.impl;

import org.apache.shardingsphere.database.protocol.postgresql.packet.command.query.extended.bind.protocol.text.PostgreSQLTextValueParser;
import org.apache.shardingsphere.infra.exception.external.sql.type.wrapper.SQLWrapperException;
import org.postgresql.util.PGobject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;

public final class PostgreSQLUDTValueParser implements PostgreSQLTextValueParser<PGobject> {

    private static final Logger log = LoggerFactory.getLogger(PostgreSQLUDTValueParser.class);

    @Override
      public PGobject parse(final String value) {
          try {
              PGobject result = new PGobject();
              System.out.println("Parsing PostgreSQL UDT value: " + value);
              log.debug("Parsing PostgreSQL UDT value '{}'", value);
              // Set the actual type name based on the UDT name
              // This will be set dynamically based on column metadata
              result.setValue(value);
              return result;
          } catch (final SQLException ex) {
              throw new SQLWrapperException(ex);
          }
      }
  }