package net.mafiascum.reformatter;

import java.util.Map;

import net.mafiascum.reformatter.post.PostMetadata;

public class QuoteReformatterAdditionalData extends ReformatterAdditionalData {
  
  protected Map<Integer, PostMetadata> postMetadataMap;
  
  public Map<Integer, PostMetadata> getPostMetadataMap() {
    return postMetadataMap;
  }
  public void setPostMetadataMap(Map<Integer, PostMetadata> postMetadataMap) {
    this.postMetadataMap = postMetadataMap;
  }
  
  public Class<? extends Reformatter> getReformatterClass() {
    return QuoteReformatter.class;
  }
}
