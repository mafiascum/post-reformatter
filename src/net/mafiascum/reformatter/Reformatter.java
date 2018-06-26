package net.mafiascum.reformatter;

import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Map;

public interface Reformatter {

  public List<ReformatterMatchData> performFirstPass(final String text);
  public ReformatterAdditionalData loadAdditionalData(final Statement statement, final Map<Integer, List<ReformatterMatchData>> entityIdToMatchDataMap) throws SQLException;
  public String performEntitySecondPass(final String entityBody, final List<ReformatterMatchData> entityMatchDataList, final ReformatterAdditionalData additionalData);
}
