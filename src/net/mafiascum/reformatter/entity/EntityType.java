package net.mafiascum.reformatter.entity;

public enum EntityType {
  
  POST(0, "POST", new PostEntityProcessor(), "phpbb_posts", "post_id", "post_text"),
  PRIVATE_MESSAGE(1, "PM", new PrivateMessageEntityProcessor(), "phpbb_privmsgs", "msg_id", "message_text");
  
  protected int value;
  protected String name;
  protected EntityProcessor processor;
  protected String tableName;
  protected String idColumnName;
  protected String textColumnName;
  
  private EntityType(int value, String name, EntityProcessor processor, String tableName, String idColumnName, String textColumnName) {
    this.value = value;
    this.name = name;
    this.processor = processor;
    this.tableName = tableName;
    this.idColumnName = idColumnName;
    this.textColumnName = textColumnName;
  }
  
  public int getValue() {
    return value;
  }
  public String getName() {
    return name;
  }
  public EntityProcessor getProcessor() {
    return processor;
  }
  public String getTableName() {
    return tableName;
  }
  public String getIdColumnName() {
    return idColumnName;
  }
  public String getTextColumnName() {
    return textColumnName;
  }
}
