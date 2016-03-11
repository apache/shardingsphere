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