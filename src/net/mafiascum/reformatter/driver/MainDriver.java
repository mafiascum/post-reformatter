package net.mafiascum.reformatter.driver;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import net.mafiascum.reformatter.ReformatterAdditionalData;
import net.mafiascum.reformatter.ReformatterMatchData;
import net.mafiascum.reformatter.ReformatterType;
import net.mafiascum.reformatter.entity.Entity;
import net.mafiascum.reformatter.entity.EntityProcessor;
import net.mafiascum.reformatter.entity.EntityType;

public class MainDriver {

  public static void main(String[] args) throws Exception {
    
    new MainDriver().process(args);
  }
  
  public void process(String[] args) throws Exception {
    
    Map<String, String> paramMap = new HashMap<>();
    
    if(!parseArguments(args, paramMap))
      return;
    
    if(!validateArguments(paramMap))
      return;
    
    String inputFilePath = paramMap.get("f");
    String dbHost = paramMap.get("h");
    String dbUsername = paramMap.get("u");
    String dbPassword = paramMap.get("p");
    String dbSchema = paramMap.get("s");
    int dbPort = Integer.valueOf(paramMap.get("r"));
    int batchSize  = Integer.valueOf(paramMap.get("b"));
    String reformatterTypeName = paramMap.get("t");
    
    ReformatterType reformatterType = getReformatterType(reformatterTypeName);
    
    if(reformatterType == null) {
      System.out.print("Invalid reformatter type `" + reformatterTypeName + "`.");
      return;
    }
    
    Map<EntityType, Set<Integer>> entityTypeToEntityIdsMap = getEntityTypeToEntityIdsMap(inputFilePath);
    Connection connection = createConnection(dbHost, dbPort, dbUsername, dbPassword, dbSchema);
    Statement statement = connection.createStatement();
    processEntitiesByType(connection, statement, entityTypeToEntityIdsMap, batchSize, reformatterType);
    
    statement.close();
    if(!connection.getAutoCommit())
      connection.commit();
    connection.close();
  }
  
  protected boolean validateArguments(Map<String, String> paramMap) {
    if(
        !paramMap.containsKey("f") ||
        !paramMap.containsKey("u") ||
        !paramMap.containsKey("s") ||
        !paramMap.containsKey("t")
    ) {
      usage(Optional.of("Missing at least one required parameter."));
      return false;
    }
    
    if(!paramMap.containsKey("h"))
      paramMap.put("h", "localhost");
    if(!paramMap.containsKey("p"))
      paramMap.put("p", "");
    if(!paramMap.containsKey("b"))
      paramMap.put("b", "10000");
    if(!paramMap.containsKey("r"))
      paramMap.put("r", "3306");
    
    if(tryParseInteger(paramMap.get("b")) == null || tryParseInteger(paramMap.get("b")) <= 0) {
      usage(Optional.of("Batch size `-b` must be a valid, positive integer."));
      return false;
    }
    
    if(tryParseInteger(paramMap.get("r")) == null) {
      usage(Optional.of("Port `-r` must be a valid integer."));
      return false;
    }
    
    return true;
  }
  
  protected Integer tryParseInteger(String str) {
    try {
      return Integer.valueOf(str);
    }
    catch(NumberFormatException exception) {
      return null;
    }
  }
  
  protected boolean parseArguments(String[] args, Map<String, String> paramMap) {
    String lastReadParamName = null;
    
    for(int index = 0;index < args.length;++index) {
      
      String arg = args[index];
      
      if(lastReadParamName != null) {
        paramMap.put(lastReadParamName, arg);
        lastReadParamName = null;
        continue;
      }
      
      if(!arg.startsWith("-")) {
        usage(Optional.of("Expected parameter name. Argument `" + arg + "` did not start with a '-'"));
        return false;
      }
      
      if(arg.length() <= 1) {
        usage(Optional.of("Empty parameter value."));
        return false;
      }
      
      lastReadParamName = arg.substring(1);
    }
    
    if(lastReadParamName != null) {
      usage(Optional.of("Parameter `" + lastReadParamName + "` has no value"));
      return false;
    }
    
    return true;
  }
  
  protected void usage(Optional<String> errorMessage) {
    System.out.println("Usage:\n\n"
        + " -f   input file path\n"
        + " -h   database hostname\n"
        + " -r   database port\n"
        + " -p   database password\n"
        + " -u   database username\n"
        + " -s   database schema\n"
        + " -b   batch size\n"
        + " -t   reformatter type(possible values: QUOTE)"
    );
    
    if(errorMessage.isPresent()) {
      System.out.println("\n" + errorMessage.get());
    }
  }
  
  protected ReformatterType getReformatterType(String reformatterTypeName) {
    for(ReformatterType reformatterType : ReformatterType.values()) {
      if(reformatterType.getName().equalsIgnoreCase(reformatterTypeName))
        return reformatterType;
    }
    
    return null;
  }
  
  protected void processEntitiesByType(
      final Connection connection,
      final Statement statement,
      final Map<EntityType, Set<Integer>> entityTypeToEntityIdsMap,
      final int batchSize,
      final ReformatterType reformatterType
  ) throws SQLException {
    
    for(EntityType entityType : entityTypeToEntityIdsMap.keySet()) {
      
      Set<Integer> entityIds = entityTypeToEntityIdsMap.get(entityType);
      
      processEntityType(connection, statement, entityType, entityIds, batchSize, reformatterType);
    }
  }
  
  protected void processEntityType(
      final Connection connection,
      final Statement statement,
      final EntityType entityType,
      final Collection<Integer> entityIds,
      final int batchSize,
      final ReformatterType reformatterType
  ) throws SQLException {
    
    System.out.println("Processing entities of type `" + entityType.getName() + "`. " + entityIds.size() + " entities in total. Batch size: " + batchSize);
    Iterator<Integer> entityIdIterator = entityIds.iterator();
    EntityProcessor entityProcessor = entityType.getProcessor();
    
    for(int offset = 0;offset < entityIds.size();offset += batchSize) {
      
      System.out.println("[" + entityType.getName() + "] Offset: " + offset + " / " + entityIds.size() + "...");
      Set<Integer> batchEntityIdSet = createBatchEntityIdSet(entityIdIterator, batchSize);
      processEntityTypeBatch(connection, statement, batchEntityIdSet, entityProcessor, reformatterType);
    }
  }
  
  protected void processEntityTypeBatch(
      final Connection connection,
      final Statement statement,
      final Set<Integer> entityIdSet,
      final EntityProcessor entityProcessor,
      final ReformatterType reformatterType
  ) throws SQLException {
    
    //Load this batch of entities from the database.
    Map<Integer, Entity> entityMap = entityProcessor.getEntityMap(statement, entityIdSet);
    
    //Perform the first pass, gathering information about each entity.
    Map<Integer, List<ReformatterMatchData>> entityIdToMatchDataMap = performEntityBatchFirstPass(entityMap.values(), reformatterType);
    
    //Using the data above, load any additional data that may be required.
    ReformatterAdditionalData additionalData = reformatterType.getReformatter().loadAdditionalData(statement, entityIdToMatchDataMap);
    
    //Perform the second pass, reformatting the entity body as needed.
    List<Entity> modifiedEntities = performEntityBatchSecondPass(entityMap.values(), additionalData, entityIdToMatchDataMap, reformatterType);
    System.out.println(" - " + modifiedEntities.size() + " / " + entityMap.size() + " entities modified.");
    
    //Save the entities to the system.
    entityProcessor.saveEntities(connection, statement, modifiedEntities);
  }
  
  protected List<Entity> performEntityBatchSecondPass(
      final Collection<Entity> entities,
      final ReformatterAdditionalData additionalData,
      final Map<Integer, List<ReformatterMatchData>> entityIdToMatchDataMap,
      final ReformatterType reformatterType
  ) {
    
    List<Entity> modifiedEntities = new ArrayList<>();
    
    for(Entity entity : entities) {
      String newBody = reformatterType.getReformatter().performEntitySecondPass(entity.getBody(), entityIdToMatchDataMap.get(entity.getId()), additionalData);
      
      if(!newBody.equals(entity.getBody())) {
        entity.setBody(newBody);
        modifiedEntities.add(entity);
      }
    }
    
    return modifiedEntities;
  }
  
  protected Map<Integer, List<ReformatterMatchData>> performEntityBatchFirstPass(
      final Collection<Entity> entities,
      final ReformatterType reformatterType
  ) {
    Map<Integer, List<ReformatterMatchData>> entityIdToMatchDataMap = new HashMap<>();
    
    for(Entity entity : entities) {
      List<ReformatterMatchData> entityMatchData = reformatterType.getReformatter().performFirstPass(entity.getBody());
      entityIdToMatchDataMap.put(entity.getId(), entityMatchData);
    }
    
    return entityIdToMatchDataMap;
  }
  
  protected Set<Integer> createBatchEntityIdSet(
      final Iterator<Integer> entityIdIterator,
      final int batchSize
  ) {
    Set<Integer> batchEntityIdSet = new HashSet<>();
    for(int subCounter = 0;subCounter < batchSize && entityIdIterator.hasNext();++subCounter)
      batchEntityIdSet.add(entityIdIterator.next());
    return batchEntityIdSet;
  }
  
  protected Connection createConnection(
      final String dbHost,
      final int dbPort,
      final String dbUsername,
      final String dbPassword,
      final String dbSchema
  ) throws SQLException, InstantiationException, IllegalAccessException, ClassNotFoundException {
    String dbUrl = String.format(
        "jdbc:mysql://%s:%s/%s?useUnicode=yes&characterEncoding=UTF-8&jdbcCompliantTruncation=false&zeroDateTimeBehavior=convertToNull&allowMultiQueries=true&rewriteBatchedStatements=true",
        dbHost,
        dbPort,
        dbSchema
    );
    
    Class.forName("com.mysql.jdbc.Driver").newInstance();
    return DriverManager.getConnection(dbUrl, dbUsername, dbPassword);
  }
  
  protected Map<EntityType, Set<Integer>> getEntityTypeToEntityIdsMap(
      final String inputFilePath
 ) throws IOException {
    Map<String, EntityType> nameToEntityTypeMap = getNameToEntityTypeMap();
    Map<EntityType, Set<Integer>> entityTypeToEntityIdsMap = new HashMap<>();
    
    for(EntityType entityType : EntityType.values()) {
      entityTypeToEntityIdsMap.put(entityType, new HashSet<>());
    }
    
    BufferedReader bufferedReader = new BufferedReader(new FileReader(new File(inputFilePath)));
    String line;
    int lineNumber = 0;
    
    while( (line = bufferedReader.readLine()) != null ) {
      ++lineNumber;
      String[] lineComponents = line.split("\t");
      
      if(lineComponents.length < 2) {
        System.out.println("Error on line #" + lineNumber + ": expected 2 components, found " + lineComponents.length + ".");
        continue;
      }
      
      String entityName = lineComponents[0].trim();
      EntityType entityType = nameToEntityTypeMap.get(entityName);
      
      if(entityType == null) {
        System.out.println("Error on line #" + lineNumber + ": Invalid entity type `" + entityName + "`.");
        continue;
      }
      
      Integer entityId = tryParseInteger(lineComponents[1].trim());
      
      if(entityId == null) {
        System.out.println("Error on line #" + lineNumber + ": Invalid entity ID `" + lineComponents[1] + "`.");
        continue;
      }
      
      Set<Integer> entityIdSet = entityTypeToEntityIdsMap.get(entityType);
      
      if(entityIdSet.contains(entityId)) {
        System.out.println("Error on line #" + lineNumber + ": Entity ID `" + entityId + "` appeared earlier for type `" + entityType.getName() + "`.");
        continue;
      }
      
      entityIdSet.add(entityId);
    }
    
    bufferedReader.close();
    return entityTypeToEntityIdsMap;
  }
  
  protected Map<String, EntityType> getNameToEntityTypeMap() {
    Map<String, EntityType> nameToEntityTypeMap = new HashMap<>();
    
    for(EntityType entityType : EntityType.values())
      nameToEntityTypeMap.put(entityType.getName(), entityType);
    
    return nameToEntityTypeMap;
  }
}
