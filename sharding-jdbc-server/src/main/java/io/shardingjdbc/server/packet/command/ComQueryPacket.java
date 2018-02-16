package io.shardingjdbc.server.packet.command;

import io.shardingjdbc.core.api.ShardingDataSourceFactory;
import io.shardingjdbc.core.exception.ShardingJdbcException;
import io.shardingjdbc.server.constant.ColumnType;
import io.shardingjdbc.server.constant.StatusFlag;
import io.shardingjdbc.server.packet.MySQLPacketPayload;
import io.shardingjdbc.server.packet.MySQLSentPacket;
import io.shardingjdbc.server.packet.ok.EofPacket;

import javax.sql.DataSource;
import java.io.File;
import java.io.IOException;
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
public final class ComQueryPacket extends CommandPacket {
    
    private String sql;
    
    @Override
    public ComQueryPacket read(final MySQLPacketPayload mysqlPacketPayload) {
        sql = mysqlPacketPayload.readStringEOF();
        return this;
    }
    
    @Override
    public List<MySQLSentPacket> execute() {
        List<MySQLSentPacket> result = new LinkedList<>();
        int currentSequenceId = getSequenceId();
        // TODO init data source in startup
        DataSource dataSource;
        try {
            dataSource = ShardingDataSourceFactory.createDataSource(new File(ComQueryPacket.class.getResource("/META-INF/sharding-config.yaml").getFile()));
        } catch (IOException | SQLException ex) {
            throw new ShardingJdbcException(ex);
        }
        try (
                Connection conn = dataSource.getConnection();
                Statement statement = conn.createStatement()) {
            statement.execute(sql);
            ResultSet resultSet = statement.getResultSet();
            if (null == resultSet) {
                result.add(new EofPacket(++currentSequenceId, 0, StatusFlag.SERVER_STATUS_AUTOCOMMIT.getValue()));
                result.add(new EofPacket(++currentSequenceId, 0, StatusFlag.SERVER_STATUS_AUTOCOMMIT.getValue()));
                return result;
            } 
            ResultSetMetaData resultSetMetaData = resultSet.getMetaData();
            int columnCount = resultSetMetaData.getColumnCount();
            result.add(new ComQueryResponsePacket(++currentSequenceId, columnCount));
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
        } catch (SQLException ex) {
            throw new ShardingJdbcException(ex);
        }
        return result;
    }
}
