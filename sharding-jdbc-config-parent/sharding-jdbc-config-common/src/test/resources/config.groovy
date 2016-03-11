import com.dangdang.ddframe.rdb.sharding.api.strategy.database.DatabaseShardingStrategy
import com.dangdang.ddframe.rdb.sharding.api.strategy.table.TableShardingStrategy
import com.dangdang.ddframe.rdb.sharding.config.common.internal.MultiAlgorithm
import com.dangdang.ddframe.rdb.sharding.config.common.internal.SingleAlgorithm
import org.apache.commons.dbcp.BasicDataSource

BasicDataSource ds1 = new BasicDataSource()
ds1.driverClassName = 'org.h2.Driver'
ds1.url = 'jdbc:h2:mem:%s;DB_CLOSE_DELAY=-1;DATABASE_TO_UPPER=false;MODE=MYSQL'
ds1.username = 'sa'
ds1.password = ''
ds1.maxActive = 100

BasicDataSource ds2 = new BasicDataSource()
ds2.driverClassName = 'org.h2.Driver'
ds2.url = 'jdbc:h2:mem:%s;DB_CLOSE_DELAY=-1;DATABASE_TO_UPPER=false;MODE=MYSQL'
ds2.username = 'sa'
ds2.password = ''
ds2.maxActive = 100

datasource db0: ds1, db1: ds2

table "config", ["config_${0..1}"]

def db001 = new DatabaseShardingStrategy("order_id", new SingleAlgorithm())
def table001 = new TableShardingStrategy(["id"], new MultiAlgorithm())

table "t_order", ["t_order_${0..1}"], db001, table001
table "t_order_item", ["t_order_item_${0..1}"], db001, table001

bind(["t_order", "t_order_item"])

defaultStrategy databaseStrategy(["order_id", "user_id"], { order_id.longValue() + 1 })
defaultStrategy table001
