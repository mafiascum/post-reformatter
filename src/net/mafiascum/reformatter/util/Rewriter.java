package net.mafiascum.reformatter.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class Rewriter
{
  private Pattern pattern;
  private Matcher matcher;
  
  public Rewriter(String regex)
  {
    this.pattern = Pattern.compile(regex);
  }
  
  public String group(int i)
  {
    return matcher.group(i);
  }
  
  public abstract String replacement();
  
  public String rewrite(CharSequence original) {
    this.matcher = pattern.matcher(original);
    StringBuffer result = new StringBuffer(original.length());
    while (matcher.find())
    {
      matcher.appendReplacement(result, "");
      result.append(replacement());
    }
    matcher.appendTail(result);
    return result.toString();
  }
}