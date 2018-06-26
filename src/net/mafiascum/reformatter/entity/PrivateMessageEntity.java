package net.mafiascum.reformatter.entity;

public class PrivateMessageEntity extends Entity {

  public PrivateMessageEntity(int id, String body) {
    super(id, body);
  }
  
  public EntityType getType() {
    return EntityType.PRIVATE_MESSAGE;
  }
}
