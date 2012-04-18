package database_connector;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class DatabaseAdapter {
  private Connection conn;
  private byte[] uuid;

  public DatabaseAdapter(String connectionString, byte[] uuid) throws Exception {
    Class.forName("com.mysql.jdbc.Driver").newInstance();
    conn = DriverManager.getConnection(connectionString);
    this.uuid = uuid;
  }

  public Long GetAspectId(String word) {
    try {
      PreparedStatement stmt = conn
          .prepareStatement("SELECT CAST(CONV(SUBSTRING(MD5(aspects.uuid), 1, 15), 16, 10) AS SIGNED INTEGER) AS featureId FROM aspects WHERE aspects.uuid IN (SELECT keywords.aspect_uuid FROM keywords WHERE keywords.label=?) AND aspects.setcover_uuid=?;");
      stmt.setString(1, word);
      stmt.setBytes(2, uuid);
      ResultSet set = stmt.executeQuery();
      if (set.next()) {
        Long result = set.getLong("featureId");
        set.close();
        stmt.close();
        return result;
      }
      set.close();
      stmt.close();
      return new Long(-1);
    } catch (Exception ex) {
      ex.printStackTrace();
      return new Long(-1);
    }
  }

  public void CloseAdapter() {
    try {
      conn.close();
    } catch (Exception ex) {
    }
  }
}
