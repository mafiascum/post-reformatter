package net.mafiascum.reformatter.entity;
import java.sql.ResultSet;
import java.sql.SQLException;

public class PostEntityProcessor extends EntityProcessor {
  
  public Entity getEntity(ResultSet resultSet) throws SQLException {
    return new PostEntity(resultSet.getInt(getEntityType().getIdColumnName()), resultSet.getString(getEntityType().getTextColumnName()));
  }
  
  public EntityType getEntityType() {
    return EntityType.POST;
  }
}
