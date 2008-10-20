package com.computas.sublima.query.service;


import com.hp.hpl.jena.db.DBConnection;
import com.hp.hpl.jena.db.IDBConnection;

//import java.io.BufferedWriter;
//import java.io.FileWriter;
import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.sql.*;

/**
 * @author: mha
 * Date: 08.jan.2008
 */
public class DatabaseService {
  private String M_DB_URL = "jdbc:postgresql://localhost/subdata";// SettingsService.getProperty("sublima.database.url");
  private String M_DB_USER = "subuser";//SettingsService.getProperty("sublima.database.username");
  private String M_DB_PASSWD = "subpasswd";//SettingsService.getProperty("sublima.database.password");
  private String M_DB = "PostgreSQL";//SettingsService.getProperty("sublima.database.databasetype");
  private String M_DBDRIVER_CLASS = "org.postgresql.Driver";//SettingsService.getProperty("sublima.database.class");

  private IDBConnection connection = null;
  private Connection jConnection = null;

  public IDBConnection getConnection() {

    try {
      Class.forName(M_DBDRIVER_CLASS);
    } catch (ClassNotFoundException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    connection = new DBConnection(M_DB_URL, M_DB_USER, M_DB_PASSWD, M_DB);

    return connection;
  }

  public void closeConnection() {
    try {
      connection.close();
    } catch (SQLException e) {
      e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
    }
  }

  public Connection getJavaSQLConnection() {
    try {
      Class.forName(M_DBDRIVER_CLASS);
    } catch (ClassNotFoundException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

    try {
      jConnection = DriverManager.getConnection(M_DB_URL, M_DB_USER, M_DB_PASSWD);
    } catch (SQLException e) {
      e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
    }

    return jConnection;
  }

  public void closeJavaSQLConnection() {
    try {
      jConnection.close();
    } catch (SQLException e) {
      e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
    }
  }

  public int doSQLUpdate(String sql) throws SQLException {
    Statement stmt;
    int rows;

    stmt = getJavaSQLConnection().createStatement();
    rows = stmt.executeUpdate(sql);
    jConnection.commit();
    stmt.close();
    closeJavaSQLConnection();

    return rows;
  }

  /**
   * Method do do a SQL query. Returns a Statement instead of a ResultSet since a ResultSet is closed whenever the Statement is closed.
   * Use Statement.getResultSet() to get the actual ResultSet.
   *
   * @param sql
   * @return Statement
   * @throws SQLException
   */
  public Statement doSQLQuery(String sql) throws SQLException {
    Statement stmt;
    ResultSet rs;


    stmt = getJavaSQLConnection().createStatement();
    rs = stmt.executeQuery(sql);
    closeJavaSQLConnection();

    return stmt;
  }

  public void writeModelToFile(String filename, String format) {

    try {
      // Create file
      //FileWriter fstream = new FileWriter(filename);
      //BufferedWriter out = new BufferedWriter(fstream);
      FileOutputStream fstream = new FileOutputStream(filename);
      BufferedOutputStream out = new BufferedOutputStream(fstream);
      
      SettingsService.getModel().write(out, format);     
      //Close the output stream
      out.close();
    } catch (Exception e) {//Catch exception if any
      System.err.println("Error : " + e.getMessage());
      e.printStackTrace(System.err);
    }
  }
}
