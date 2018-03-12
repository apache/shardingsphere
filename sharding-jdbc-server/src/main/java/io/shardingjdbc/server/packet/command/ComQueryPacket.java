package io.shardingjdbc.server.packet.command;

import io.shardingjdbc.core.constant.SQLType;
import io.shardingjdbc.core.parsing.SQLJudgeEngine;
import io.shardingjdbc.core.parsing.parser.sql.SQLStatement;
import io.shardingjdbc.server.DataSourceManager;
import io.shardingjdbc.server.constant.ColumnType;
import io.shardingjdbc.server.constant.StatusFlag;
import io.shardingjdbc.server.packet.AbstractMySQLSentPacket;
import io.shardingjdbc.server.packet.MySQLPacketPayload;
import io.shardingjdbc.server.packet.ok.EofPacket;
import io.shardingjdbc.server.packet.ok.ErrPacket;
import io.shardingjdbc.server.packet.ok.OKPacket;
import lombok.extern.slf4j.Slf4j;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.LinkedList;
import java.util.List;

/**
 * COM_QUERY command packet.
 * @see <a href="https://dev.mysql.com/doc/internals/en/com-query.html">COM_QUERY</a>
 *
 * @author zhangliang
 */
@Slf4j
public final class ComQueryPacket extends AbstractCommandPacket {
    
    private String sql;
    
    @Override
    public ComQueryPacket read(final MySQLPacketPayload mysqlPacketPayload) {
        sql = mysqlPacketPayload.readStringEOF();
        log.debug("SQL received for Sharding-JDBC-server: {}", sql);
        return this;
    }
    
    @Override
    public List<AbstractMySQLSentPacket> execute() {
        List<AbstractMySQLSentPacket> result = new LinkedList<>();
        int currentSequenceId = getSequenceId();
        try (
                Connection conn = DataSourceManager.getInstance().getDataSource().getConnection();
                Statement statement = conn.createStatement()) {
            SQLStatement sqlStatement = new SQLJudgeEngine(sql).judge();
            ResultSet resultSet;
            if (SQLType.DQL == sqlStatement.getType()) {
                resultSet = statement.executeQuery(sql);
            } else if (SQLType.DML == sqlStatement.getType() || SQLType.DDL == sqlStatement.getType()) {
                statement.executeUpdate(sql);
                resultSet = statement.getResultSet();
            } else {
                statement.execute(sql);
                resultSet = statement.getResultSet();
            }
            if (null == resultSet) {
                result.add(new OKPacket(++currentSequenceId, 0, 0, StatusFlag.SERVER_STATUS_AUTOCOMMIT.getValue(), 0, ""));
                return result;
            }
            ResultSetMetaData resultSetMetaData = resultSet.getMetaData();
            int columnCount = resultSetMetaData.getColumnCount();
            if (0 == columnCount) {
                result.add(new OKPacket(++currentSequenceId, 0, 0, StatusFlag.SERVER_STATUS_AUTOCOMMIT.getValue(), 0, ""));
                return result;
            }
            result.add(new FieldCountPacket(++currentSequenceId, columnCount));
            for (int i = 1; i <= columnCount; i++) {
                result.add(new ColumnDefinition41Packet(++currentSequenceId, resultSetMetaData.getSchemaName(i), resultSetMetaData.getTableName(i), 
                        resultSetMetaData.getTableName(i), resultSetMetaData.getColumnLabel(i), resultSetMetaData.getColumnName(i), 
                        resultSetMetaData.getColumnDisplaySize(i), ColumnType.valueOfJDBCType(resultSetMetaData.getColumnType(i)), 0));
            }
            result.add(new EofPacket(++currentSequenceId, 0, StatusFlag.SERVER_STATUS_AUTOCOMMIT.getValue()));
            while (resultSet.next()) {
                List<Object> data = new LinkedList<>();
                for (int i = 1; i <= columnCount; i++) {
                    data.add(resultSet.getObject(i));
                }
                result.add(new TextResultSetRowPacket(++currentSequenceId, data));
            }
            result.add(new EofPacket(++currentSequenceId, 0, StatusFlag.SERVER_STATUS_AUTOCOMMIT.getValue()));
        } catch (final SQLException ex) {
            result.add(new ErrPacket(++currentSequenceId, ex.getErrorCode(), "", ex.getSQLState(), ex.getMessage()));
            return result;
        } catch (final Exception ex) {
            if (ex.getCause() instanceof SQLException) {
                SQLException cause = (SQLException) ex.getCause();
                result.add(new ErrPacket(++currentSequenceId, cause.getErrorCode(), "", cause.getSQLState(), cause.getMessage()));
            } else {
                // TODO standard ShardingJdbcException
                result.add(new ErrPacket(++currentSequenceId, 99, "", "unknown", ex.getMessage()));
            }
            return result;
        }
        return result;
    }
}
