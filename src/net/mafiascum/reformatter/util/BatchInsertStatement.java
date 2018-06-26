package net.mafiascum.reformatter.util;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BatchInsertStatement {

  protected static final int DEFAULT_BATCH_SIZE = 100;

  protected Map<String, Integer> columnToIndexMap;
  protected Map<String, String> currentRowColumnToValueMap;
  protected StringBuilder queryStringBuilder;
  protected int batchSize;
  protected int numberOfRowsLoaded;
  protected boolean insertIgnore;
  protected Connection connection;
  protected Statement statement;
  protected boolean loadingRow;
  protected String schema;
  protected String tableName;
  protected SQLUtil sqlUtil = new SQLUtil();
  
  protected BatchInsertStatement() {
    queryStringBuilder = new StringBuilder();
    columnToIndexMap = new HashMap<String, Integer>();
    currentRowColumnToValueMap = new HashMap<String, String>();
    numberOfRowsLoaded = 0;
    loadingRow = false;
  }
  
  public BatchInsertStatement (Connection connection, Statement statement, String table) {
    this(connection, statement, table, DEFAULT_BATCH_SIZE, false);
  }

  public BatchInsertStatement (Connection connection, Statement statement, String table, int batchSize) {
    this(connection, statement, table, batchSize, false);
  }
  
  public BatchInsertStatement(Connection connection, Statement statement, String table, int batchSize, boolean insertIgnore) {
    this();
    this.connection = connection;
    this.statement = statement;
    this.batchSize = batchSize;
    this.tableName = table;
    this.insertIgnore = insertIgnore;
  }
  
  public BatchInsertStatement withSchema(String schema) {
    this.schema = schema;
    return this;
  }
  
  public BatchInsertStatement (Statement statement, String table) {
    this(statement, table, DEFAULT_BATCH_SIZE, false);
  }

  public BatchInsertStatement (Statement statement, String table, int batchSize) {
    this(statement, table, batchSize, false);
  }
  
  public BatchInsertStatement (Statement statement, String table, int batchSize, boolean insertIgnore) {
    this(null, statement, table, batchSize, insertIgnore);
  }
  
  protected void setupQuery() {
    queryStringBuilder.setLength(0);
    columnToIndexMap.clear();
    int index = 0;
    
    if(!insertIgnore)
      queryStringBuilder.append("INSERT INTO ");
    else
      queryStringBuilder.append("INSERT IGNORE INTO ");
    
    queryStringBuilder.append(sqlUtil.getEscapedTableName(schema, tableName)).append('(');
    
    for(String columnName : currentRowColumnToValueMap.keySet()) {
      
      columnToIndexMap.put(columnName, index);
      if(index++ > 0)
        queryStringBuilder.append(',');
      queryStringBuilder.append(sqlUtil.escapeQuoteColumnName(columnName));
    }

    queryStringBuilder.append(")VALUES");
  }
  
  public void beginEntry() throws SQLException {
    if(loadingRow)
      throw new SQLException("Already loading row.");
    loadingRow = true;
    currentRowColumnToValueMap.clear();
  }
  
  public void endEntry() throws SQLException {
    if(!loadingRow)
      throw new SQLException("No row was being loaded.");
    
    if(numberOfRowsLoaded == 0) {
      setupQuery();
    }
    
    List<String> currentRow = Arrays.asList(new String[columnToIndexMap.size()]);
    
    for(String columnName : currentRowColumnToValueMap.keySet()) {
      currentRow.set(columnToIndexMap.get(columnName), currentRowColumnToValueMap.get(columnName));
    }
    
    writeRow(currentRow);
    
    ++numberOfRowsLoaded;
    loadingRow = false;
    
    if(numberOfRowsLoaded >= batchSize)
      flush();
  }
  
  protected void writeRow(Collection<String> currentRow) {
    int index = 0;
    if(numberOfRowsLoaded > 0)
      queryStringBuilder.append(',');
    queryStringBuilder.append('(');
    for(String columnValue : currentRow) {
      if(index++ > 0)
        queryStringBuilder.append(',');
      queryStringBuilder.append(columnValue);
    }
    queryStringBuilder.append(')');
  }
  
  public void flush() throws SQLException {
    flush(statement);
  }
  
  protected void flush(Statement statement) throws SQLException {
    
    String sql = queryStringBuilder.toString();
    
    queryStringBuilder.setLength(0);
    statement.executeUpdate(sql);
    
    this.numberOfRowsLoaded = 0;
    this.columnToIndexMap.clear();
  }
  
  public void finish() throws SQLException {
    if(numberOfRowsLoaded > 0)
      flush();
  }
  
  public BatchInsertStatement putEscapedString(String columnName, String escapedValue) {
    currentRowColumnToValueMap.put(columnName, escapedValue);
    return this;
  }
  
  public BatchInsertStatement put(String columnName, Object value) {
    return putEscapedString(columnName, "NULL");
  }
  
  public BatchInsertStatement put(String columnName, String value) {
    return putEscapedString(columnName, sqlUtil.escapeQuoteString(value));
  }
  
  public BatchInsertStatement put(String columnName, Integer value) {
    return putEscapedString(columnName, value == null ? "NULL" : String.valueOf(value));
  }
  
  public BatchInsertStatement put(String columnName, int value) {
    return putEscapedString(columnName, String.valueOf(value));
  }
  
  public BatchInsertStatement put(String columnName, Double value) {
    return putEscapedString(columnName, value == null ? "NULL" : String.valueOf(value));
  }
  
  public BatchInsertStatement put(String columnName, double value) {
    return putEscapedString(columnName, String.valueOf(value));
  }
  
  public BatchInsertStatement put(String columnName, boolean value) {
    return putEscapedString(columnName, sqlUtil.encodeBooleanInt(value));
  }

  public BatchInsertStatement put(String columnName, Boolean value) {
    return putEscapedString(columnName, sqlUtil.encodeBooleanInt(value));
  }
  
  public BatchInsertStatement put(String columnName, Long value) {
    return putEscapedString(columnName, value == null ? "NULL" : String.valueOf(value));
  }

  public BatchInsertStatement put(String columnName, long value) {
    return putEscapedString(columnName, String.valueOf(value));
  }
  
  public BatchInsertStatement put(String columnName, BigDecimal value) {
    return putEscapedString(columnName, String.valueOf(value));
  }
  
  public BatchInsertStatement putMoney(String columnName, BigDecimal moneyValue) {
    return putEscapedString(columnName, moneyValue == null ? "NULL" : String.valueOf(moneyValue.movePointRight(2).intValue()));
  }
}