package com.eyeson.fastform.common;

import java.io.Serializable;

/**
 * 版权：融贯资讯 <br/>
 * 作者：yong.li@rogrand.com <br/>
 * 生成日期：2013-11-19 <br/>
 * 描述：数据库操作父接口,提供增删改查基本方法
 */
public interface DBInterface<T extends Serializable> {

    /**
     * 根据主键删除记录
     *
     * @param tid 主键id
     * @return 删除记录数
     */
    int deleteByPrimaryKey(Integer id);

    /**
     * 插入记录对象所有属性
     *
     * @param t 记录对象
     * @return 插入记录数
     */
    int insert(T record);

    /**
     * 插入记录对象不为空的属性
     *
     * @param t 记录对象
     * @return 插入记录数
     */
    int insertSelective(T record);

    /**
     * 根据主键id查询记录对象
     *
     * @param tid 主键id
     * @return 记录对象
     */
    T selectByPrimaryKey(Integer id);

    /**
     * 更新记录对象不为空的属性
     *
     * @param t 记录对象
     * @return 更新记录数
     */
    int updateByPrimaryKeySelective(T record);

    /**
     * 更新记录对象所有属性
     *
     * @param t 记录对象
     * @return 更新记录数
     */
    int updateByPrimaryKey(T record);

}
