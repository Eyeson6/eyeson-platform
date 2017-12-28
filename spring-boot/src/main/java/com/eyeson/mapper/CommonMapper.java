package com.eyeson.mapper;


import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

/**
 * Created by Blues Zhao on 上午10:27 2017/12/27.
 */
public interface CommonMapper {

    @Select("${sql}")
    List<Map> getAll(Map sql);
}
