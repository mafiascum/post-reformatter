package net.mafiascum.reformatter.post;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import net.mafiascum.reformatter.util.SQLUtil;

public class PostUtil {

  protected SQLUtil sqlUtil = new SQLUtil();
  
  public Map<Integer, PostMetadata> getPostMetadataMap(
      final Statement statement,
      final Collection<Integer> postIds
  ) throws SQLException {
    
    String sql = " SELECT"
               + "   `post_id`,"
               + "   `poster_id`,"
               + "   `post_time`"
               + " FROM `phpbb_posts`"
               + " WHERE `post_id` IN" + sqlUtil.buildListSQL(postIds, true, true);
    
    ResultSet resultSet = statement.executeQuery(sql);
    Map<Integer, PostMetadata> postMetadataMap = new HashMap<>();
    
    while(resultSet.next()) {
      PostMetadata postMetadata = new PostMetadata();
      
      postMetadata.setPosterId(resultSet.getInt("poster_id"));
      postMetadata.setPostId(resultSet.getInt("post_id"));
      postMetadata.setPostTime(resultSet.getInt("post_time"));
      
      postMetadataMap.put(postMetadata.getPostId(), postMetadata);
    }
    
    resultSet.close();
    return postMetadataMap;
  }
}
