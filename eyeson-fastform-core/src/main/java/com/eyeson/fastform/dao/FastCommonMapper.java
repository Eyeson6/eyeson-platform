package com.eyeson.fastform.dao;


import com.eyeson.fastform.bean.CommonSearchSql;
import org.apache.ibatis.annotations.Param;
import org.springframework.dao.DataAccessException;

import java.util.List;
import java.util.Map;

public interface FastCommonMapper {

    public long queryCount(@Param("sql") String sql) throws DataAccessException;

    public int insertSql(CommonSearchSql commonSearchSql) throws DataAccessException;

    public <T> List<T> queryList(@Param("sql") String sql) throws DataAccessException;

    public int deleteSql(@Param("sql") String sql) throws DataAccessException;

    public Map<String, Object> queryRecord(@Param("sql") String sql) throws DataAccessException;

}
