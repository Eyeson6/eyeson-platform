package com.eyeson.fastform.service.impl;

import com.eyeson.fastform.bean.CommonSearchSql;
import com.eyeson.fastform.bean.SelectBean;
import com.eyeson.fastform.common.*;
import com.eyeson.fastform.dao.FastCommonMapper;
import com.eyeson.fastform.service.CommonSearchService;
import com.eyeson.fastform.service.CommonSearchTemplateService;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.lang.reflect.Method;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * 版权：融贯资讯 <br/>
 * 作者：jing.zhao@rogrand.com <br/>
 * 生成日期：2016-3-7 <br/>
 * 描述：通用查询服务类
 */
@Service("commonSearchService")
public class CommonSearchServiceImpl implements CommonSearchService {

    private static Map<String, Map<String, Object>> templateMap = new HashMap<String, Map<String, Object>>();

    private static final String SUFFIX = ".xml";

    @Autowired
    private FastCommonMapper fastCommonMapper;

    @Value("${commom.search.xml.path}")
    private String xmlPath;

    private Log logger = LogFactory.getLog(getClass());

    @Autowired
    private CommonSearchTemplateService commonSearchTemplateService;

    /**
     * 描述：通用查询接口 <br/>
     * 作者：jing.zhao@rogrand.com <br/>
     * 生成日期：2016-3-8 <br/>
     *
     * @param menuId
     * @return
     */
    @SuppressWarnings("unchecked")
    @Override
    public PageResult search(String menuId, PageParam pageParam)
            throws KkmyException {
        Map<String, Object> tempMap = commonSearchTemplateService
                .getTempMap(menuId);
        // ////////////////处理sql标签////////////////////////////////////
        Map<String, Object> sqlMap;
        String sql;
        List<String> sqlList = null;
        int sqlArrayLength = 0;
        StringBuffer sb;
        int index = 0;
        try {
            sqlMap = (Map<String, Object>) tempMap.get("sql");
            Object object = sqlMap.get("#text");
            if (object instanceof JSONArray) {
                sqlList = (List<String>) JSONArray
                        .toCollection((JSONArray) object);
                // sqlArray = ((String [])sqlMap.get("#text"));
            } else {
                sqlList = new ArrayList<String>();
                sqlList.add((String) sqlMap.get("#text"));
            }
            sqlArrayLength = sqlList.size();
            sql = sqlList.get(index++);
            sb = new StringBuffer(sql);
        } catch (Exception e) {
            throw new KkmyException("sql配置不正确，查询失败");
        }
        // /////////////////处理if条件////////////////////////////////
        if (sqlMap.containsKey("if")) {
            try {
                Object ifObject = sqlMap.get("if");
                List<Map<String, Object>> ifList = null;
                if (ifObject instanceof JSONArray) {
                    ifList = (List<Map<String, Object>>) sqlMap.get("if");
                } else {
                    ifList = new ArrayList<Map<String, Object>>();
                    ifList.add((Map<String, Object>) sqlMap.get("if"));
                }
                for (Map<String, Object> ifMap : ifList) {
                    String methodString = (String) ifMap.get("@method");
                    String methodParam = (String) ifMap.get("@args");
                    boolean flag = (Boolean) invokeUtilMethod(methodString,
                            methodParam, pageParam);
                    if (flag) {
                        sb.append(" " + ifMap.get("#text"));
                    }
                    if (index < sqlArrayLength) {
                        sb.append(sqlList.get(index++));
                    }
                }

                // /////////////////////将参数注入到sql////////////////////////
                sql = sb.toString();
                for (String currKey : pageParam.keySet()) {
                    // String conditionName = (String)
                    // conditionMap.get("@name");
                    Object conditionValue = pageParam.get(currKey);
                    try {
                        sql = sql
                                .replaceAll("\\%\\$\\{" + currKey + "\\}\\%",
                                        "'%" + conditionValue.toString() + "%'")
                                .replaceAll("\\%\\$\\{" + currKey + "\\}",
                                        "'%" + conditionValue.toString() + "'")
                                .replaceAll("\\$\\{" + currKey + "\\}\\%",
                                        "'" + conditionValue.toString() + "%'")
                                .replaceAll("\\$\\{" + currKey + "\\}",
                                        "'" + conditionValue.toString() + "'");
                    } catch (Exception e) {
                    }
                }
                sql = sql.replaceAll("\n", "").replaceAll("\t", "")
                        .replaceAll("\\?", "");
            } catch (Exception e) {
                throw new KkmyException("查询条件配置错误，查询失败");
            }
            String sort = pageParam.getSort();
            String order = pageParam.getOrder();
            if (sort != null && order != null && !"".equals(sort)
                    && !"".equals(order)) {
                String orderSql = sql;
                if (orderSql.replaceAll(" ", "").contains("orderby")) {
                    sql = sql + "," + sort + " " + order;
                } else {
                    sql = sql + " order by " + sort + " " + order;
                }
            }
        }
        try {
            if (pageParam.get("page") != null && pageParam.get("rows") != null) {
                String countSql = "select count(1) from (" + sql
                        + ") as total ";
                Long recordCount = fastCommonMapper.queryCount(countSql);
                pageParam.setRecordCount(recordCount);
                if (pageParam.getRows() == 0)
                    pageParam.setRows(20);
                if (recordCount > 0) {
                    if (pageParam.getPage() == 0)
                        pageParam.setPage(1l);
                    Long skipResults = (pageParam.getPage() - 1)
                            * pageParam.getRows();
                    String foot = "limit " + skipResults + ","
                            + pageParam.getRows();
                    sql = sql + " " + foot;
                }

            }
            List list = fastCommonMapper
                    .queryList(sql);
            PageResult pageResult = new PageResult();
            pageResult.setPageList(list);
            pageResult.setPageParam(pageParam);
            return pageResult;
        } catch (Exception e) {
            logger.error("执行sql错误，查询失败", e);
            throw new KkmyException("执行sql错误，查询失败");
        }

    }

    @SuppressWarnings("unchecked")
    @Override
    public Map<String, Object> chart(String menuId, PageParam pageParam)
            throws KkmyException {
        // 定义返回参数
        Map<String, Object> resultMap = new HashMap<String, Object>();
        List<String> listLabelsResult = new ArrayList<String>();
        resultMap.put("labelsList", listLabelsResult);
        pageParam.setPage(1l);
        pageParam.setRows(30);
        PageResult pageResult = this.search(menuId, pageParam);
        List<Map<String, Object>> dataList = new ArrayList<Map<String, Object>>();
        Map<String, Object> tempMap = commonSearchTemplateService
                .getTempMap(menuId);
        // Map<String, Object> chartMap = (Map<String, Object>)
        // tempMap.get("chart");
        Object chartObject = tempMap.get("chart");
        List<Map<String, Object>> chartList = null;
        if (chartObject instanceof JSONArray) {
            chartList = (List<Map<String, Object>>) JSONArray.toCollection(
                    (JSONArray) chartObject, HashMap.class);
            resultMap.put("mutiChart", true);
        } else if (chartObject instanceof List) {
            chartList = (List<Map<String, Object>>) chartObject;
            resultMap.put("mutiChart", true);
        } else {
            chartList = new ArrayList<Map<String, Object>>();
            chartList.add((Map<String, Object>) chartObject);
        }
        if (pageParam.get("index") == null) {
            throw new KkmyException("图表配置不正确");
        }
        Integer chartIndex = Integer
                .parseInt(pageParam.get("index").toString());
        if (chartIndex == null || chartIndex >= chartList.size()) {
            throw new KkmyException("图表配置不正确");
        }
        Map<String, Object> chartMap = chartList.get(chartIndex);
        String lables = (String) chartMap.get("@labels");
        Object object = chartMap.get("data");
        if (object instanceof JSONArray) {
            dataList = (List<Map<String, Object>>) JSONArray.toCollection(
                    (JSONArray) object, HashMap.class);
        } else if (object instanceof List) {
            dataList = (List<Map<String, Object>>) object;
        } else {
            dataList = new ArrayList<Map<String, Object>>();
            dataList.add((Map<String, Object>) chartMap.get("data"));
        }
        // 预先准备返回值中的value集合
        for (Map<String, Object> dataMap : dataList) {
            if (dataMap.get("@name") != null) {
                resultMap.put("valueList_" + dataMap.get("@name"),
                        new ArrayList<String>());
            }

        }

        List<Map<String, Object>> searchResultlist = pageResult.getPageList();
        for (Map<String, Object> map : searchResultlist) {
            for (Map<String, Object> dataMap : dataList) {
                List<String> list = (List<String>) resultMap.get("valueList_"
                        + dataMap.get("@name"));
                Object dataobj = map.get(dataMap.get("@name"));
                if (!CommonSearchUtils.isNum(dataobj)) {
                    throw new KkmyException("数据不正确，统计图数据只能为数字类型");
                }
                list.add(obj2string(dataobj));
            }
            Object labelObject = map.get(lables);
            listLabelsResult.add(obj2string(labelObject));

        }
        return resultMap;
    }

    private String obj2string(Object object) {
        if (object instanceof Timestamp) {
            DateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            return sdf.format(object);
        } else {
            return object.toString();
        }

    }

    /**
     * 描述：通用添加接口 <br/>
     * 作者：jing.zhao@rogrand.com <br/>
     * 生成日期：2016-3-8 <br/>
     *
     * @param menuId
     * @return
     */
    @SuppressWarnings("unchecked")
    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED, rollbackFor = Throwable.class)
    public void insert(String menuId, PageParam pageParam) throws KkmyException {
        Map<String, Object> tempMap = commonSearchTemplateService
                .getTempMap(menuId);
        // ////////////////处理sql标签////////////////////////////////////
        Map<String, Object> sqlMap;
        String sql;
        List<String> sqlList = null;
        int sqlArrayLength = 0;
        StringBuffer sb;
        int index = 0;
        try {
            Map map = (Map) tempMap.get("insert");
            sqlMap = (JSONObject) map.get("sql");
            Object object = sqlMap.get("#text");
            if (object instanceof JSONArray) {
                sqlList = (List<String>) JSONArray
                        .toCollection((JSONArray) object);
                // sqlArray = ((String [])sqlMap.get("#text"));
            } else {
                sqlList = new ArrayList<String>();
                sqlList.add((String) sqlMap.get("#text"));
            }
            sqlArrayLength = sqlList.size();
            sql = sqlList.get(index++);
            sb = new StringBuffer(sql);
        } catch (Exception e) {
            throw new KkmyException("sql配置不正确，查询失败");
        }
        // /////////////////处理if条件////////////////////////////////
        if (sqlMap.containsKey("if")) {
            try {
                Object ifObject = sqlMap.get("if");
                List<Map<String, Object>> ifList = null;
                if (ifObject instanceof JSONArray) {
                    ifList = (List<Map<String, Object>>) sqlMap.get("if");
                } else {
                    ifList = new ArrayList<Map<String, Object>>();
                    ifList.add((Map<String, Object>) sqlMap.get("if"));
                }
                for (Map<String, Object> ifMap : ifList) {
                    String methodString = (String) ifMap.get("@method");
                    String methodParam = (String) ifMap.get("@args");
                    boolean flag = (Boolean) invokeUtilMethod(methodString,
                            methodParam, pageParam);
                    if (flag) {
                        sb.append(" " + ifMap.get("#text"));
                    }
                    if (index < sqlArrayLength) {
                        sb.append(sqlList.get(index++));
                    }
                }

                // /////////////////////将参数注入到sql////////////////////////
                sql = sb.toString();
                for (String currKey : pageParam.keySet()) {
                    // String conditionName = (String)
                    // conditionMap.get("@name");
                    Object conditionValue = pageParam.get(currKey);
                    try {
                        sql = sql
                                .replaceAll("\\%\\$\\{" + currKey + "\\}\\%",
                                        "'%" + conditionValue.toString() + "%'")
                                .replaceAll("\\%\\$\\{" + currKey + "\\}",
                                        "'%" + conditionValue.toString() + "'")
                                .replaceAll("\\$\\{" + currKey + "\\}\\%",
                                        "'" + conditionValue.toString() + "%'")
                                .replaceAll("\\$\\{" + currKey + "\\}",
                                        "'" + conditionValue.toString() + "'");
                    } catch (Exception e) {
                    }
                }
                sql = sql.replaceAll("\n", "").replaceAll("\t", "")
                        .replaceAll("\\?", "");
            } catch (Exception e) {
                throw new KkmyException("查询条件配置错误，查询失败");
            }
        }
        try {
            // ////////////////去掉条件中最后一个逗号///////////////////////
            String[] ss = sql.split(",");
            StringBuffer sss = new StringBuffer();
            for (int i = 0; i < ss.length; i++) {
                if (i == ss.length / 2 - 1 || i == ss.length - 2
                        || i == ss.length - 1) {
                    sss.append(ss[i]);
                } else {
                    sss.append(ss[i]).append(",");
                }
            }
            sql = sss.toString();
            // /////////////////执行sql////////////////////////
            CommonSearchSql searchSql = new CommonSearchSql(sql);
            Object object = fastCommonMapper.insertSql(searchSql);
            System.out.println(object);
        } catch (Exception e) {
            throw new KkmyException("执行sql错误，添加失败");
        }

    }

    /**
     * 描述：通用修改接口 <br/>
     * 作者：jing.zhao@rogrand.com <br/>
     * 生成日期：2016-3-8 <br/>
     *
     * @param menuId
     * @return
     */
    @SuppressWarnings("unchecked")
    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED, rollbackFor = Throwable.class)
    public void update(String menuId, PageParam pageParam) throws KkmyException {
        Map<String, Object> tempMap = commonSearchTemplateService
                .getTempMap(menuId);
        // ////////////////处理sql标签////////////////////////////////////
        Map<String, Object> sqlMap;
        String sql;
        List<String> sqlList = null;
        int sqlArrayLength = 0;
        StringBuffer sb;
        int index = 0;
        try {
            Map map = (Map) tempMap.get("update");
            sqlMap = (JSONObject) map.get("sql");
            Object object = sqlMap.get("#text");
            if (object instanceof JSONArray) {
                sqlList = (List<String>) JSONArray
                        .toCollection((JSONArray) object);
                // sqlArray = ((String [])sqlMap.get("#text"));
            } else {
                sqlList = new ArrayList<String>();
                sqlList.add((String) sqlMap.get("#text"));
            }
            sqlArrayLength = sqlList.size();
            sql = sqlList.get(index++);
            sb = new StringBuffer(sql);
        } catch (Exception e) {
            throw new KkmyException("sql配置不正确，查询失败");
        }
        // /////////////////处理if条件////////////////////////////////
        if (sqlMap.containsKey("if")) {
            try {
                Object ifObject = sqlMap.get("if");
                List<Map<String, Object>> ifList = null;
                if (ifObject instanceof JSONArray) {
                    ifList = (List<Map<String, Object>>) sqlMap.get("if");
                } else {
                    ifList = new ArrayList<Map<String, Object>>();
                    ifList.add((Map<String, Object>) sqlMap.get("if"));
                }
                for (Map<String, Object> ifMap : ifList) {
                    String methodString = (String) ifMap.get("@method");
                    String methodParam = (String) ifMap.get("@args");
                    boolean flag = (Boolean) invokeUtilMethod(methodString,
                            methodParam, pageParam);
                    if (flag) {
                        sb.append(" " + ifMap.get("#text"));
                    }
                    if (index < sqlArrayLength) {
                        sb.append(sqlList.get(index++));
                    }
                }

                // /////////////////////将参数注入到sql////////////////////////
                sql = sb.toString();
                for (String currKey : pageParam.keySet()) {
                    // String conditionName = (String)
                    // conditionMap.get("@name");
                    Object conditionValue = pageParam.get(currKey);
                    try {
                        sql = sql
                                .replaceAll("\\%\\$\\{" + currKey + "\\}\\%",
                                        "'%" + conditionValue.toString() + "%'")
                                .replaceAll("\\%\\$\\{" + currKey + "\\}",
                                        "'%" + conditionValue.toString() + "'")
                                .replaceAll("\\$\\{" + currKey + "\\}\\%",
                                        "'" + conditionValue.toString() + "%'")
                                .replaceAll("\\$\\{" + currKey + "\\}",
                                        "'" + conditionValue.toString() + "'");
                    } catch (Exception e) {
                    }
                }
                sql = sql.replaceAll("\n", "").replaceAll("\t", "")
                        .replaceAll("\\?", "");
            } catch (Exception e) {
                throw new KkmyException("查询条件配置错误，查询失败");
            }
        }
        try {
            // ////////////////去掉条件中最后一个逗号///////////////////////
            String[] ss = sql.split(",");
            StringBuffer sss = new StringBuffer();
            for (int i = 0; i < ss.length; i++) {
                if (i == ss.length - 2 || i == ss.length - 1) {
                    sss.append(ss[i]);
                } else {
                    sss.append(ss[i]).append(",");
                }
            }
            sql = sss.toString();
            // /////////////////执行sql////////////////////////
            CommonSearchSql searchSql = new CommonSearchSql(sql);
            fastCommonMapper.insertSql(searchSql);
        } catch (Exception e) {
            throw new KkmyException("执行sql错误，添加失败");
        }

    }

    /**
     * 描述：反射调用静态方法 <br/>
     * 作者：jing.zhao@rogrand.com <br/>
     * 生成日期：2016-3-8 <br/>
     *
     * @return
     */
    private Object invokeUtilMethod(String methodStirng, String methodParam,
                                    Map<String, Object> paraMap) {
        try {
            if (StringUtil.isEmpty(methodParam)
                    || StringUtil.isEmpty(methodStirng)) {
                return null;
            }

            // 动态计算参数个数
            String[] params = methodParam.split(",");
            Class c = Class.forName(CommonSearchUtils.class.getName());
            Class[] paramClass = new Class[params.length];
            for (int i = 0; i < params.length; i++) {
                String arg = params[i];
                if ((!StringUtil.isEmpty(arg)) && arg.startsWith("${")
                        && arg.endsWith("}")) {
                    arg = arg.replaceAll("\\$\\{", "").replaceAll("\\}", "");
                    String argValue = (String) paraMap.get(arg);
                    params[i] = argValue;
                }
                paramClass[i] = Object.class;

            }
            // 得到方法
            Method m = c.getMethod(methodStirng, paramClass);
            // 执行方法
            Object object = m.invoke(c, params);
            return object;
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 描述：〈根据name和menuId获取select控件的初始化值〉 <br/>
     * 作者：zhengbin.jin@rogrand.com <br/>
     * 生成日期：2016年3月10日 <br/>
     *
     * @param param
     * @return
     */
    @SuppressWarnings("unchecked")
    @Override
    public List<SelectBean> searchSelect(PageParam param, String type) {
        String menuId = param.get("menuId").toString();
        String name = param.get("name").toString();
        Map<String, Object> tempMap = commonSearchTemplateService
                .getTempMap(menuId);
        List<Map<String, Object>> conditions = new ArrayList<Map<String, Object>>();
        if ("main".equals(type)) {
            tempMap.get("query");
            conditions = (List<Map<String, Object>>) tempMap.get("conditions");
        } else if ("add".equals(type)) {
            Map insert = (Map) tempMap.get("insert");
            conditions = (List<Map<String, Object>>) insert.get("conditions");
        } else {
            Map update = (Map) tempMap.get("update");
            conditions = (List<Map<String, Object>>) update.get("conditions");
        }
        List<SelectBean> beans = new ArrayList<SelectBean>();
        for (Map<String, Object> map : conditions) {
            if (map.get("@name").toString().equals(name)) {
                List<Map<String, Object>> options = (List<Map<String, Object>>) map
                        .get("option");
                if (options != null) {
                    for (Map<String, Object> option : options) {
                        SelectBean bean = new SelectBean();
                        bean.setId(option.get("@id").toString());
                        bean.setText(option.get("@text").toString());
                        beans.add(bean);
                    }
                } else {
                    String sql = (String) map.get("sql");
                    List<Map> list = fastCommonMapper.queryList(sql);
                    for (Map option : list) {
                        SelectBean bean = new SelectBean();
                        bean.setId(option.get("id").toString());
                        bean.setText(option.get("text").toString());
                        beans.add(bean);
                    }
                }
            }
        }
        return beans;
    }

    @SuppressWarnings("unchecked")
    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED, rollbackFor = Throwable.class)
    public void delete(PageParam pageParam) throws KkmyException {
        Map<String, Object> tempMap = commonSearchTemplateService
                .getTempMap(pageParam.get("menuId").toString());
        // ////////////////处理sql标签////////////////////////////////////
        Map<String, Object> sqlMap;
        String sql;
        List<String> sqlList = null;
        int sqlArrayLength = 0;
        StringBuffer sb;
        int index = 0;
        try {
            Map map = (Map) tempMap.get("delete");
            sqlMap = (JSONObject) map.get("sql");
            Object object = sqlMap.get("#text");
            if (object instanceof JSONArray) {
                sqlList = (List<String>) JSONArray
                        .toCollection((JSONArray) object);
                // sqlArray = ((String [])sqlMap.get("#text"));
            } else {
                sqlList = new ArrayList<String>();
                sqlList.add((String) sqlMap.get("#text"));
            }
            sqlArrayLength = sqlList.size();
            sql = sqlList.get(index++);
            sb = new StringBuffer(sql);
        } catch (Exception e) {
            throw new KkmyException("sql配置不正确，查询失败");
        }
        // /////////////////处理if条件////////////////////////////////
        if (sqlMap.containsKey("if")) {
            try {
                Object ifObject = sqlMap.get("if");
                List<Map<String, Object>> ifList = null;
                if (ifObject instanceof JSONArray) {
                    ifList = (List<Map<String, Object>>) sqlMap.get("if");
                } else {
                    ifList = new ArrayList<Map<String, Object>>();
                    ifList.add((Map<String, Object>) sqlMap.get("if"));
                }
                for (Map<String, Object> ifMap : ifList) {
                    String methodString = (String) ifMap.get("@method");
                    String methodParam = (String) ifMap.get("@args");
                    boolean flag = (Boolean) invokeUtilMethod(methodString,
                            methodParam, pageParam);
                    if (flag) {
                        sb.append(" " + ifMap.get("#text"));
                    }
                    if (index < sqlArrayLength) {
                        sb.append(sqlList.get(index++));
                    }
                }

                // /////////////////////将参数注入到sql////////////////////////
                sql = sb.toString();
                for (String currKey : pageParam.keySet()) {
                    // String conditionName = (String)
                    // conditionMap.get("@name");
                    Object conditionValue = pageParam.get(currKey);
                    try {
                        sql = sql
                                .replaceAll("\\%\\$\\{" + currKey + "\\}\\%",
                                        "'%" + conditionValue.toString() + "%'")
                                .replaceAll("\\%\\$\\{" + currKey + "\\}",
                                        "'%" + conditionValue.toString() + "'")
                                .replaceAll("\\$\\{" + currKey + "\\}\\%",
                                        "'" + conditionValue.toString() + "%'")
                                .replaceAll("\\$\\{" + currKey + "\\}",
                                        "'" + conditionValue.toString() + "'");
                    } catch (Exception e) {
                    }
                }
                sql = sql.replaceAll("\n", "").replaceAll("\t", "")
                        .replaceAll("\\?", "");
            } catch (Exception e) {
                throw new KkmyException("查询条件配置错误，查询失败");
            }
        }
        try {
            fastCommonMapper.deleteSql(sql);
        } catch (Exception e) {
            throw new KkmyException("执行sql错误，添加失败");
        }

    }

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
    @Override
    public boolean searchRepeat(String table, String colum, Object value) {
        StringBuffer sql = new StringBuffer();
        sql.append("select count(1) from ").append(table).append(" where ")
                .append(colum).append("=").append("'").append(value)
                .append("'");
        Long recordCount = fastCommonMapper.queryCount(sql.toString());
        if (recordCount > 0) {
            return true;
        }
        return false;
    }

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
    @Override
    public boolean searchRepeat(String table, String colum, Object value,
                                String key, String kvalue) {
        StringBuffer sql = new StringBuffer();
        sql.append("select count(1) from ").append(table).append(" where ")
                .append(colum).append("=").append("'").append(value)
                .append("'").append(" and ").append(key).append("!=")
                .append("'").append(kvalue).append("'");
        Long recordCount = fastCommonMapper.queryCount(sql.toString());
        if (recordCount > 0) {
            return true;
        }
        return false;
    }

    /**
     * 描述：〈查询单条数据〉 <br/>
     * 作者：zhengbin.jin@rogrand.com <br/>
     * 生成日期：2016年3月28日 <br/>
     *
     * @param menuId 模板ID
     * @param key    查询的唯一标识
     * @return
     */
    @Override
    public Map query(String menuId, String key) {
        Map<String, Object> templateMap = commonSearchTemplateService
                .getTempMap(menuId);
        Map update = (Map) templateMap.get("update");
        StringBuffer sql = new StringBuffer();
        sql.append("select * from ").append(update.get("table"))
                .append(" where ").append(update.get("key")).append("=")
                .append("'").append(key).append("'");
        return fastCommonMapper.queryRecord(sql.toString());
    }

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
    @SuppressWarnings("unchecked")
    @Override
    public Map queryCount(String string, PageParam pageParam)
            throws KkmyException {
        Map<String, Object> tempMap = commonSearchTemplateService
                .getTempMap(string);
        // ////////////////处理sql标签////////////////////////////////////
        Map<String, Object> sqlMap;
        String sql;
        List<String> sqlList = null;
        int sqlArrayLength = 0;
        StringBuffer sb;
        int index = 0;
        try {
            Map map = (Map) tempMap.get("count");
            sqlMap = (JSONObject) map.get("sql");
            Object object = sqlMap.get("#text");
            if (object instanceof JSONArray) {
                sqlList = (List<String>) JSONArray
                        .toCollection((JSONArray) object);
                // sqlArray = ((String [])sqlMap.get("#text"));
            } else {
                sqlList = new ArrayList<String>();
                sqlList.add((String) sqlMap.get("#text"));
            }
            sqlArrayLength = sqlList.size();
            sql = sqlList.get(index++);
            sb = new StringBuffer(sql);
        } catch (Exception e) {
            throw new KkmyException("sql配置不正确，查询失败");
        }
        // /////////////////处理if条件////////////////////////////////
        if (sqlMap.containsKey("if")) {
            try {
                Object ifObject = sqlMap.get("if");
                List<Map<String, Object>> ifList = null;
                if (ifObject instanceof JSONArray) {
                    ifList = (List<Map<String, Object>>) sqlMap.get("if");
                } else {
                    ifList = new ArrayList<Map<String, Object>>();
                    ifList.add((Map<String, Object>) sqlMap.get("if"));
                }
                for (Map<String, Object> ifMap : ifList) {
                    String methodString = (String) ifMap.get("@method");
                    String methodParam = (String) ifMap.get("@args");
                    boolean flag = (Boolean) invokeUtilMethod(methodString,
                            methodParam, pageParam);
                    if (flag) {
                        sb.append(" " + ifMap.get("#text"));
                    }
                    if (index < sqlArrayLength) {
                        sb.append(sqlList.get(index++));
                    }
                }

                // /////////////////////将参数注入到sql////////////////////////
                sql = sb.toString();
                for (String currKey : pageParam.keySet()) {
                    // String conditionName = (String)
                    // conditionMap.get("@name");
                    Object conditionValue = pageParam.get(currKey);
                    try {
                        sql = sql
                                .replaceAll("\\%\\$\\{" + currKey + "\\}\\%",
                                        "'%" + conditionValue.toString() + "%'")
                                .replaceAll("\\%\\$\\{" + currKey + "\\}",
                                        "'%" + conditionValue.toString() + "'")
                                .replaceAll("\\$\\{" + currKey + "\\}\\%",
                                        "'" + conditionValue.toString() + "%'")
                                .replaceAll("\\$\\{" + currKey + "\\}",
                                        "'" + conditionValue.toString() + "'");
                    } catch (Exception e) {
                    }
                }
                sql = sql.replaceAll("\n", "").replaceAll("\t", "")
                        .replaceAll("\\?", "");
            } catch (Exception e) {
                throw new KkmyException("查询条件配置错误，查询失败");
            }
        }
        try {
            Map map = fastCommonMapper.queryRecord(sql);
            return map;
        } catch (Exception e) {
            throw new KkmyException("执行sql错误，查询失败");
        }
    }

    /**
     * 将json转为map 描述：〈描述〉 <br/>
     * 作者：jing.zhao@rogrand.com <br/>
     * 生成日期：2016-3-7 <br/>
     *
     * @param jsonStr
     * @return
     */
    public Map<String, Object> parseJSON2Map(String jsonStr) {
        Map<String, Object> map = new HashMap<String, Object>();
        // 最外层解析
        JSONObject json = JSONObject.fromObject(jsonStr);
        for (Object k : json.keySet()) {
            Object v = json.get(k);
            // 如果内层还是数组的话，继续解析
            if (v instanceof JSONArray) {
                List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
                Iterator<JSONObject> it = ((JSONArray) v).iterator();
                while (it.hasNext()) {
                    JSONObject json2 = it.next();
                    list.add(parseJSON2Map(json2.toString()));
                }
                map.put(k.toString(), list);
            } else {
                map.put(k.toString(), v);
            }
        }
        return map;
    }

    @Override
    public void test(PageParam pageParam) {
        // TODO Auto-generated method stub

    }

    /**
     * 描述：〈获取初始值设置方法〉 <br/>
     * 作者：chenhui.yan@rogrand.com <br/>
     * 生成日期：2016年6月16日 <br/>
     */
    @SuppressWarnings("unchecked")
    @Override
    public Map<String, Object> defaultprocess(PageParam pageParam, Map<String, Object> tempMap, String type)
            throws KkmyException {

        return tempMap;
    }


    @Override
    public Map query(String menuId, String key, String buttonId, String operate)
            throws KkmyException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void buttonExecute(String menuId, PageParam pageParam,
                              String buttonId) throws KkmyException {
        // TODO Auto-generated method stub

    }

    @Override
    public List searchLinkage(PageParam model)
            throws KkmyException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void dynamicCheck(String menuId, PageParam pageParam, String buttonId)
            throws KkmyException {
        // TODO Auto-generated method stub

    }

    @Override
    public PageParam valideteProcess(PageParam model) throws KkmyException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public PageResult searchDynamicViewPage(String string, PageParam merchant)
            throws KkmyException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void operateCheck(String menuId, PageParam model, String buttonId)
            throws KkmyException {
        // TODO Auto-generated method stub

    }

    @Override
    public void convertValueToKey(Map row, Map condition) throws KkmyException {
        // TODO Auto-generated method stub

    }

}
