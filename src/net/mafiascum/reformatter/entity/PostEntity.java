package net.mafiascum.reformatter.entity;

public class PostEntity extends Entity {

  public PostEntity(int id, String body) {
    super(id, body);
  }
  
  public EntityType getType() {
    return EntityType.POST;
  }
}
