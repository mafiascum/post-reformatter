package net.mafiascum.reformatter.entity;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import net.mafiascum.reformatter.util.BatchInsertStatement;
import net.mafiascum.reformatter.util.SQLUtil;

public abstract class EntityProcessor {

  protected SQLUtil sqlUtil = new SQLUtil();
  
  public Map<Integer, Entity> getEntityMap(Statement statement, Collection<Integer> entityIds) throws SQLException {
    
    Map<Integer, Entity> entityMap = new HashMap<>();
    EntityType entityType = getEntityType();
    
    String sql = " SELECT"
               + "   " + sqlUtil.escapeQuoteColumnName(entityType.getIdColumnName()) + ","
               + "   " + sqlUtil.escapeQuoteColumnName(entityType.getTextColumnName())
               + " FROM " + entityType.getTableName()
               + " WHERE " + sqlUtil.escapeQuoteColumnName(entityType.getIdColumnName()) + " IN " + sqlUtil.buildListSQL(entityIds, false, true);
    
    ResultSet resultSet = statement.executeQuery(sql);
    
    while(resultSet.next()) {
      
      Entity entity = getEntity(resultSet);
      entityMap.put(entity.getId(), entity);
    }
    
    resultSet.close();
    return entityMap;
  }
  
  public void saveEntities(Connection connection, Statement statement, Collection<Entity> entities) throws SQLException {
    
    dropTempSaveEntitiesTable(statement);
    
    createTempSaveEntitiesTable(statement);
    
    insertTempSaveEntities(connection, statement, entities);
    
    performSaveEntitiesUpdate(statement);
    
    dropTempSaveEntitiesTable(statement);
  }
  
  protected void insertTempSaveEntities(Connection connection, Statement statement, Collection<Entity> entities) throws SQLException {
    BatchInsertStatement batchInsertStatement = new BatchInsertStatement(
        connection,
        statement,
        getTempSaveEntitiesTableName(),
        entities.size(),
        false
    );
    
    for(Entity entity : entities) {
      batchInsertStatement.beginEntry();
      
      batchInsertStatement.put("entity_id", entity.getId());
      batchInsertStatement.put("text", entity.getBody());
      
      batchInsertStatement.endEntry();
    }
    
    batchInsertStatement.finish();
  }
  
  protected void performSaveEntitiesUpdate(Statement statement) throws SQLException {
    String sql = " UPDATE `" + getEntityType().getTableName() + "`, `" + getTempSaveEntitiesTableName() + "` SET"
               + "   `" + getEntityType().getTableName() + "`." + sqlUtil.escapeQuoteColumnName(getEntityType().getTextColumnName()) + "=`" + getTempSaveEntitiesTableName() + "`.`text`"
               + " WHERE `" + getEntityType().getTableName() + "`." + sqlUtil.escapeQuoteColumnName(getEntityType().getIdColumnName()) + "=`" + getTempSaveEntitiesTableName() + "`.`entity_id`";
    
    statement.executeUpdate(sql);
  }
  
  protected void dropTempSaveEntitiesTable(Statement statement) throws SQLException {
    String sql = " DROP TABLE IF EXISTS `" + getTempSaveEntitiesTableName() + "`";
    statement.executeUpdate(sql);
  }
  
  protected void createTempSaveEntitiesTable(Statement statement) throws SQLException {
    String sql = " CREATE TABLE `" + getTempSaveEntitiesTableName() + "` ("
               + "   `entity_id` int(11) unsigned not null,"
               + "   `text` mediumtext not null,"
               + " PRIMARY KEY(`entity_id`)"
               + ") ENGINE=MyISAM DEFAULT CHARSET=utf8 COLLATE=utf8_bin";
    
    statement.executeUpdate(sql);
  }
  
  public String getTempSaveEntitiesTableName() {
    return "temp_save_entities";
  }
  
  public abstract Entity getEntity(ResultSet resultSet) throws SQLException;
  public abstract EntityType getEntityType();
}
