package com.eyeson.fastform.service;

import com.eyeson.fastform.bean.SelectBean;
import com.eyeson.fastform.common.KkmyException;
import com.eyeson.fastform.common.PageParam;
import com.eyeson.fastform.common.PageResult;

import java.util.List;
import java.util.Map;

/**
 * 版权：融贯资讯 <br/>
 * 作者：jing.zhao@rogrand.com <br/>
 * 生成日期：2016-3-7 <br/>
 * 描述：通用查询服务类
 */
public interface CommonSearchService {

    public void test(PageParam pageParam);

    /**
     * 描述：执行按钮提交 <br/>
     * 作者：jing.zhao@rogrand.com <br/>
     * 生成日期：2016-6-17 <br/>
     *
     * @param menuId    模板ID
     * @param pageParam 参数
     * @param buttonId  按钮ID
     * @throws KkmyException
     */
    public void buttonExecute(String menuId, PageParam pageParam, String buttonId) throws KkmyException;

    /**
     * 描述：通用查询动态检查 <br/>
     * 作者：jing.zhao@rogrand.com <br/>
     * 生成日期：2016-6-20 <br/>
     *
     * @param menuId
     * @param pageParam
     * @param buttonId
     * @throws KkmyException
     */
    public void dynamicCheck(String menuId, PageParam pageParam, String buttonId) throws KkmyException;

    public Map<String, Object> parseJSON2Map(String jsonStr);

    /**
     * 描述：通用查询接口 <br/>
     * 作者：jing.zhao@rogrand.com <br/>
     * 生成日期：2016-3-8 <br/>
     *
     * @param menuId
     * @return
     */
    public PageResult search(String menuId, PageParam pageParam)
            throws KkmyException;

    /**
     * 描述：通用查询统计图接口 <br/>
     * 作者：jing.zhao@rogrand.com <br/>
     * 生成日期：2016-5-16 <br/>
     *
     * @param menuId
     * @param pageParam
     * @return
     * @throws KkmyException
     */
    public Map<String, Object> chart(String menuId, PageParam pageParam)
            throws KkmyException;

    /**
     * 描述：通用添加接口 <br/>
     * 作者：jing.zhao@rogrand.com <br/>
     * 生成日期：2016-3-8 <br/>
     *
     * @param menuId
     * @return
     */
    public void insert(String menuId, PageParam pageParam) throws KkmyException;

    /**
     * 描述：通用修改接口 <br/>
     * 作者：jing.zhao@rogrand.com <br/>
     * 生成日期：2016-3-8 <br/>
     *
     * @param menuId
     * @return
     */
    public void update(String menuId, PageParam pageParam) throws KkmyException;

    /**
     * 描述：〈根据name和menuId获取select控件的初始化值〉 <br/>
     * 作者：zhengbin.jin@rogrand.com <br/>
     * 生成日期：2016年3月10日 <br/>
     *
     * @param param
     * @return
     * @throws KkmyException
     */
    public List<SelectBean> searchSelect(PageParam param, String type)
            throws KkmyException;

    public void delete(PageParam pageParam) throws KkmyException;

    /**
     * 描述：〈查询字段是否已存在〉 <br/>
     * 作者：zhengbin.jin@rogrand.com <br/>
     * 生成日期：2016年3月28日 <br/>
     *
     * @param table 表
     * @param colum 字段
     * @param value 值
     * @return
     */
    public boolean searchRepeat(String table, String colum, Object value);

    /**
     * 描述：〈查询字段是否已存在 不包含本条记录〉 <br/>
     * 作者：zhengbin.jin@rogrand.com <br/>
     * 生成日期：2016年3月28日 <br/>
     *
     * @param table 表
     * @param colum 字段
     * @param value 值
     * @return
     */
    public boolean searchRepeat(String table, String colum, Object value,
                                String key, String kvalue);

    /**
     * 描述：〈查询单条数据〉 <br/>
     * 作者：jing.zhao@rogrand.com <br/>
     * 生成日期：2016-6-17 <br/>
     *
     * @param menuId
     * @param key
     * @return
     * @throws KkmyException
     */
    public Map query(String menuId, String key, String buttonId, String operate) throws KkmyException;

    /**
     * 描述：〈查询单条数据〉 <br/>
     * 作者：zhengbin.jin@rogrand.com <br/>
     * 生成日期：2016年3月28日 <br/>
     *
     * @param menuId 模板ID
     * @param key    查询的唯一标识
     * @return
     * @throws KkmyException
     */
    public Map query(String menuId, String key) throws KkmyException;

    /**
     * 描述：〈查询表头统计项〉 <br/>
     * 作者：zhengbin.jin@rogrand.com <br/>
     * 生成日期：2016年4月12日 <br/>
     *
     * @param string
     * @param pageParam
     * @return
     * @throws KkmyException
     */
    public Map queryCount(String string, PageParam pageParam)
            throws KkmyException;

    public Map<String, Object> defaultprocess(PageParam pageParam, Map<String, Object> tempMap, String type)
            throws KkmyException;

    public List searchLinkage(PageParam model) throws KkmyException;

    public PageParam valideteProcess(PageParam model) throws KkmyException;

    public PageResult searchDynamicViewPage(String string, PageParam merchant) throws KkmyException;

    public void operateCheck(String menuId, PageParam model, String buttonId) throws KkmyException;

    /**
     * 描述：对row单行数据根据condition判断是否是选择项,如果是,则将选项的text改为id <br/>
     * 作者：jing.zhao@rogrand.com <br/>
     * 生成日期：2016-8-17 <br/>
     *
     * @param row
     * @param condition
     * @return
     * @throws KkmyException
     */
    public void convertValueToKey(Map row, Map condition) throws KkmyException;

}
