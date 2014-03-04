package com.and.wellnessconnected.cassandra;

/**
 * Created by charliefederspiel on 3/3/14.
 */
public interface NoSQLBackend {
    public void init(SimpleClient c);
    public String getKeySpaceName();
    public String getTableName();
    public String getTableNameInContext();
}
