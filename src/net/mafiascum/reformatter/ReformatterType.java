package net.mafiascum.reformatter;

public enum ReformatterType {

  QUOTE(0, "QUOTE", new QuoteReformatter());
  
  protected int id;
  protected String name;
  protected Reformatter reformatter;
  
  private ReformatterType(int id, String name, Reformatter reformatter) {
    this.id = id;
    this.name = name;
    this.reformatter = reformatter;
  }
  
  public int getId() {
    return id;
  }
  public String getName() {
    return name;
  }
  public Reformatter getReformatter() {
    return reformatter;
  }
}
