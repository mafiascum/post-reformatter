package net.mafiascum.reformatter.util;
import java.util.Collection;
import java.util.Iterator;

public class SQLUtil {
  public String escapeString (String text) {
    return text.replaceAll("'", "''").replaceAll("\\\\", "\\\\\\\\");
  }
  
  public String escapeQuoteString (String text) {
    return (text != null) ? "'" + escapeString(text) + "'" : "NULL";
  }
  public String escapeQuoteColumnName(String columnName) {
    return "`" + columnName.replaceAll("`", "") + "`";
  }
  
  public <T> String buildListSQL (Collection<T> set, boolean quoteElements, boolean onEmptyAddNull) {
    if (onEmptyAddNull && set.isEmpty())
      return "(null)";

    StringBuilder strBuf = new StringBuilder("(");
    Iterator<T> iter = set.iterator();
    boolean firstPass = true;
    while (iter.hasNext()) {
      if (firstPass)
        firstPass = false;
      else
        strBuf.append(',');

      Object element = iter.next();
      if (element == null)
        strBuf.append("NULL");
      else if (quoteElements)
        strBuf.append(escapeQuoteString(element.toString()));
      else
        strBuf.append(element);
    }
    strBuf.append(')');
      return strBuf.toString();
  }
  
  public String getEscapedTableName(String schema, String table) {
    if(schema != null && !schema.isEmpty())
      return escapeQuoteColumnName(schema) + "." + escapeQuoteColumnName(table);
    return escapeQuoteColumnName(table);
  }
  
  public String encodeBooleanInt (boolean value) {
    return value ? "1" : "0";
  }
}
