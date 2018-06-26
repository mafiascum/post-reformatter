package net.mafiascum.reformatter.entity;

public abstract class Entity {
  
  protected int id;
  protected String body;
  
  public Entity(int id, String body) {
    this.id = id;
    this.body = body;
  }
  
  public int getId() {
    return id;
  }
  public void setId(int id) {
    this.id = id;
  }
  public String getBody() {
    return body;
  }
  public void setBody(String body) {
    this.body = body;
  }
  
  protected abstract EntityType getType();
}
