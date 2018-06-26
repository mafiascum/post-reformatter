package net.mafiascum.reformatter;

import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.mafiascum.reformatter.post.PostMetadata;
import net.mafiascum.reformatter.post.PostUtil;
import net.mafiascum.reformatter.util.Rewriter;

public class QuoteReformatter implements Reformatter {
  
  protected Pattern usernamePattern = Pattern.compile("^, (.*?)$");
  
  protected String specialFormatPattern = "<QUOTE author=\"(?:In)? *\\[url=.*?\\](.*?)\\[/url\\](.*?)\" url=\"(.*?)\">";
  protected String embeddedUrlPattern = "<QUOTE[^>]*\\[url=(.*?)\\].*?><s>.*?</s>";
  protected String authorAttributePattern = "author=\"(.*?)\"";
  protected String urlAttributePattern = "url=\"(.*?)\"";
  protected String postIdPattern = "\\bp=(\\d+)\\b";
  protected String topicPostNumberPattern = "(\\d+)$";
  
  protected Pattern specialFormatPatternObject = Pattern.compile(specialFormatPattern);
  protected Pattern embeddedUrlPatternObject = Pattern.compile(embeddedUrlPattern);
  protected Pattern authorAttributePatternObject = Pattern.compile(authorAttributePattern);
  protected Pattern urlAttributePatternObject = Pattern.compile(urlAttributePattern);
  protected Pattern postIdPatternObject = Pattern.compile(postIdPattern);
  protected Pattern topicPostNumberPatternObject = Pattern.compile(topicPostNumberPattern);
  
  protected PostUtil postUtil = new PostUtil();
  
  public static void main(String[] args) {
    QuoteReformatter r = new QuoteReformatter();
    
    Matcher matcher = r.embeddedUrlPatternObject.matcher("<QUOTE author=\"In [url=http://www.mafiascum.net/forum/viewtopic.php?p=8724082#p8724082]post 3249[/url], KainTepes\" url=\"http://www.mafiascum.net/forum/viewtopic.php?p=8724082#p8724082\"><s>[quote=\"In [url=http://www.mafiascum.net/forum/viewtopic.php?p=8724082#p8724082]post 3249[/url], KainTepes\"]</s>");
    
    System.out.println("Any matches: " + matcher.find());
  }
  
  protected String createAttribute(String attributeName, Object attributeValue) {
    String attributeValueString = attributeValue.toString();
    
    attributeValueString = attributeValueString.replaceAll("\"", "\\\"");
    
    return attributeName + "=\"" + attributeValue + "\"";
  }
  
  protected void appendAttribute(
      final StringBuilder stringBuilder,
      final String attributeName,
      final Object attributeValue
  ) {
    stringBuilder
    .append(stringBuilder.length() > 0 ? " " : "")
    .append(createAttribute(attributeName, attributeValue));
  }
  
  public String performEntitySecondPass(
      final String entityBody,
      final List<ReformatterMatchData> entityMatchDataList,
      final ReformatterAdditionalData additionalData
  ) {
    
    if(entityMatchDataList == null || entityMatchDataList.isEmpty())
      return entityBody;
    
    Iterator<ReformatterMatchData> matchDataIter = entityMatchDataList.iterator();
    QuoteReformatterAdditionalData quoteAdditionalData = (QuoteReformatterAdditionalData)additionalData;
    Map<Integer, PostMetadata> postMetadataMap = quoteAdditionalData.getPostMetadataMap();
    
    return new Rewriter(embeddedUrlPattern) {
      
      public String replacement() {
        
        
        QuoteReformatterMatchData matchData = (QuoteReformatterMatchData)matchDataIter.next();
        
        StringBuilder attributesBuilder = new StringBuilder();
        
        if(matchData.getUsername().isPresent() || matchData.authorAttribute.isPresent()) {
          appendAttribute(attributesBuilder, "author", matchData.getUsername().orElse(matchData.authorAttribute.get()));
        }
        
        if(matchData.getPostId().isPresent()) {
          appendAttribute(attributesBuilder, "post_id", matchData.getPostId().get());
          
          PostMetadata postMetadata = postMetadataMap.get(matchData.getPostId().get());
          
          if(postMetadata != null) {
            appendAttribute(attributesBuilder, "time", postMetadata.getPostTime());
            appendAttribute(attributesBuilder, "user_id", postMetadata.getPosterId());
          }
        }
        
        if(matchData.topicPostNumber.isPresent())
          appendAttribute(attributesBuilder, "iso", matchData.topicPostNumber.get());
        
        String attributesString = attributesBuilder.toString();
        String attributesSpace = attributesString.isEmpty() ? "" : " ";
        
        return "<QUOTE" + attributesSpace + attributesString + "><s>[quote" + attributesSpace + attributesString + "]</s>";
      }
    }.rewrite(entityBody);
  }
  
  protected Optional<String> extractUsername(String urlDisplay) {
    Matcher usernameMatcher = usernamePattern.matcher(urlDisplay);
    
    if(usernameMatcher.find())
      return Optional.of(usernameMatcher.group(1).trim());
    return Optional.empty();
  }
  
  protected Optional<String> extractTopicPostNumber(String topicPostNumberPortionDisplay) {
    Matcher topicPostNumberMatcher = topicPostNumberPatternObject.matcher(topicPostNumberPortionDisplay);
    
    if(topicPostNumberMatcher.find())
      return Optional.of(topicPostNumberMatcher.group(1));
    return Optional.empty();
  }
  
  public ReformatterAdditionalData loadAdditionalData(
      final Statement statement,
      final Map<Integer, List<ReformatterMatchData>> entityIdToMatchDataMap
  ) throws SQLException {
    QuoteReformatterAdditionalData additionalData = new QuoteReformatterAdditionalData();
    
    Set<Integer> postIdSet = new HashSet<>();
    for(List<ReformatterMatchData> reformatterMatchDataList : entityIdToMatchDataMap.values()) {
      for(ReformatterMatchData reformatterMatchData : reformatterMatchDataList) {
        
        QuoteReformatterMatchData quoteMatchData = (QuoteReformatterMatchData)reformatterMatchData;
        
        if(quoteMatchData.getPostId().isPresent())
          postIdSet.add(quoteMatchData.getPostId().get());
      }
    }
    
    additionalData.setPostMetadataMap(postUtil.getPostMetadataMap(statement, postIdSet));
    return additionalData;
  }
  
  public List<ReformatterMatchData> performFirstPass(final String text) {
    List<ReformatterMatchData> matchDataList = new ArrayList<>();
    
    Matcher embeddedUrlMatcher = embeddedUrlPatternObject.matcher(text);
    
    while(embeddedUrlMatcher.find()) {
      
      QuoteReformatterMatchData matchData = new QuoteReformatterMatchData();
      
      String entireMatch = embeddedUrlMatcher.group();
      Matcher specialFormatMatcher = specialFormatPatternObject.matcher(entireMatch);
      Matcher authorAttributeMatcher = authorAttributePatternObject.matcher(entireMatch);
      Matcher urlAttributeMatcher = urlAttributePatternObject.matcher(entireMatch);
      
      if(authorAttributeMatcher.find()) {
        matchData.setAuthorAttribute(Optional.of(authorAttributeMatcher.group(1)));
      }
      if(urlAttributeMatcher.find()) {
        String urlAttribute = urlAttributeMatcher.group(1);
        
        matchData.setUrlAttribute(Optional.of(urlAttribute));
        
        Matcher postIdMatcher = postIdPatternObject.matcher(urlAttribute);
        
        if(postIdMatcher.find()) {
          try {
            matchData.setPostId(Optional.of(Integer.valueOf(postIdMatcher.group(1))));
          }
          catch(NumberFormatException numberFormatException) {
            System.out.println("Invalid post ID: " + postIdMatcher.group(1));
          }
        }
      }
      
      if(specialFormatMatcher.find()) {
        
        String topicPostNumberPortionDisplay = specialFormatMatcher.group(1);
        String usernamePortionDisplay = specialFormatMatcher.group(2);
        
        matchData.setUsername(extractUsername(usernamePortionDisplay));
        matchData.setTopicPostNumber(extractTopicPostNumber(topicPostNumberPortionDisplay));
      }
      
      matchDataList.add(matchData);
    }
    
    return matchDataList;
  }
}
