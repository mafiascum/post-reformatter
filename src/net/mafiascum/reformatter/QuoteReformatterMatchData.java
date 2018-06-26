package net.mafiascum.reformatter;

import java.util.Optional;

public class QuoteReformatterMatchData extends ReformatterMatchData {

  protected Optional<String> username = Optional.empty();
  protected Optional<String> topicPostNumber = Optional.empty();
  protected Optional<Integer> postId = Optional.empty();
  protected Optional<String> urlAttribute = Optional.empty();
  protected Optional<String> authorAttribute = Optional.empty();
  
  public Class<?extends Reformatter> getReformatterClass() {
    return QuoteReformatter.class;
  }

  public Optional<String> getUsername() {
    return username;
  }

  public void setUsername(Optional<String> username) {
    this.username = username;
  }

  public Optional<String> getTopicPostNumber() {
    return topicPostNumber;
  }

  public void setTopicPostNumber(Optional<String> topicPostNumber) {
    this.topicPostNumber = topicPostNumber;
  }

  public Optional<Integer> getPostId() {
    return postId;
  }

  public void setPostId(Optional<Integer> postId) {
    this.postId = postId;
  }

  public Optional<String> getUrlAttribute() {
    return urlAttribute;
  }
  
  public void setUrlAttribute(Optional<String> urlAttribute) {
    this.urlAttribute = urlAttribute;
  }
  
  public Optional<String> getAuthorAttribute() {
    return authorAttribute;
  }
  
  public void setAuthorAttribute(Optional<String> authorAttribute) {
    this.authorAttribute = authorAttribute;
  }
}
