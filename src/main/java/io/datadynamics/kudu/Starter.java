package io.datadynamics.kudu;

import lombok.extern.slf4j.Slf4j;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * @author Haneul, Kim
 * @version 0.1
 * @since 2022-03-26
 */
@Slf4j
public class Starter {
  public static void main(String[] args) throws ClassNotFoundException, SQLException {
    if (args.length < 2) {
      log.error("java -jar kudu-jdbc-tester-0.4.jar <JDBC_URL> <SQL> [<PRINT_ROWS>]");
      log.error("JDBC_URL: jdbc:impala://<HOST>:<PORT>/<DATABASE>");
      log.error(" example: jdbc:impala://hdw1.dd.io:21050/default;Property1=Value;Property2=Value;");
      log.error(" example: jdbc:impala://hdw1.dd.io:21050/default;AuthMech=3;UID=ldap_username;PWD=ldap_password;");
      log.error(" example: jdbc:kudu:schemaFactory=com.twilio.kudu.sql.schema.DefaultKuduSchemaFactory;schema=default;timeZone=ko_KR;caseSensitive=false;schema.connect=adm1:7051,hdm1:7051,hdm2:7051");
      // select * from \"default.log_range\" where start_time between '2021-01-01 00:00:00' and '2021-05-31 23:59:59'
      return;
    }
    // String jdbcUrl = "jdbc:impala://hdw1.dd.io:21050/default;Property1=Value;Property2=Value;";
    String jdbcUrl = args[0];
    String[] sqls = args[1].split(";");
    boolean doPrint = args.length > 2;
    long printRows = doPrint ? Long.parseLong(args[2]) : Long.MAX_VALUE;
    log.info("jdbcUrl = {}", jdbcUrl);
    log.info("sqls = {}", (Object) sqls);
    log.info("doPrint = {}", doPrint);
    log.info("printRows = {}", printRows);

    long startTime = System.nanoTime();
    log.info("started");
    if (jdbcUrl.startsWith("jdbc.impala://")) {
      Class.forName("com.cloudera.impala.jdbc.Driver");
    } else {
      Class.forName("org.apache.calcite.jdbc.KuduDriver");
    }
    // int fetchSize = 10;

    Connection conn = DriverManager.getConnection(jdbcUrl);
    for (String sql : sqls) {
      log.info("sql = {}", sql);
      if (sql == null || "".equals(sql.trim())) {
        continue;
      }
      Statement stmt = conn.createStatement();
      // stmt.setFetchSize(fetchSize);
      ResultSet rs = stmt.executeQuery(sql);
      // rs.setFetchSize(fetchSize);

      // ResultSetMetaData metaData = rs.getMetaData();
      // int columnCount = metaData.getColumnCount();
      // List<String> columnNames = new ArrayList<>(columnCount);
      // List<String> columnTypes = new ArrayList<>(columnCount);
      // for (int i = 1; i <= columnCount; i++) {
      //   String columnName = metaData.getColumnName(i);
      //   String columnType = metaData.getColumnTypeName(i);
      //   columnNames.add(columnName);
      //   columnTypes.add(columnType);
      //   log.info("c[{}] = {} {}", i - 1, columnName, columnType);
      // }
      int rows = 0;
      while (rs.next()) {
        if (doPrint && rows % printRows == 0) {
          // for (int i = 1; i <= columnCount; i++) {
          //   String columnName = columnNames.get(i - 1);
          //   String columnType = columnTypes.get(i - 1);
          //   Object object = rs.getObject(i);
          //   log.info("[{}] {}.{}={}", rows, columnName, columnType, object);
          // }
          log.info("rows = {}", rows);
        }
        rows++;
      }
      long finishTime = System.nanoTime();
      log.info("finished");
      log.info("total rows = {}", rows);

      rs.close();
      stmt.close();

      double elapsedTimeMs = (double) (finishTime - startTime) / 1000000000;
      log.info("elapsedTime = {} s", elapsedTimeMs);
    }

    conn.close();
  }
}
