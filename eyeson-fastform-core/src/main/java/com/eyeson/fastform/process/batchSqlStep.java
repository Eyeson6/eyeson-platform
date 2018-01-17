package com.eyeson.fastform.process;

import com.eyeson.fastform.bean.CommonSearchSql;
import com.eyeson.fastform.common.KkmyException;
import com.eyeson.fastform.common.PageParam;
import com.eyeson.fastform.common.StringUtil;
import com.eyeson.fastform.dao.FastCommonMapper;
import com.eyeson.fastform.service.CommonSearchTemplateService;
import net.sf.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.Map;

@Service("batchSqlStep")
public class batchSqlStep extends BaseSqlStep implements Step {

    private final static Logger logger = LoggerFactory.getLogger(batchSqlStep.class);

    private static final String SQLID_PREFIX = "sqlid_";

    private static final String CURRENT_DATA_PREFIX = "current_data_";

    private static final String PROCESS_BREAK_KEY = "mustBreak";
    @Autowired
    private FastCommonMapper commonMapper;

    @Autowired
    private CommonSearchTemplateService commonSearchTemplateService;

    @Autowired
    private Step sqlStep;

    @Override
    public PageParam execute(PageParam contextMap, JSONObject stepObj,
                             String menuId) throws KkmyException {
        logger.info("开始执行sql流程:" + stepObj);
        String sqlId = (String) stepObj.get("@ref");
        Map<String, Object> sqlMap = commonSearchTemplateService.getTempSql(
                sqlId, menuId);
        Collection collection = getCollection(contextMap, stepObj);
        String item = stepObj.get("@item") == null ? null : stepObj.get("@item").toString();
        if (collection == null || StringUtil.isEmpty(item)) {
            return sqlStep.execute(contextMap, stepObj, menuId);
        } else {
            for (Object object : collection) {
                contextMap.put(CURRENT_DATA_PREFIX + item, object);
                contextMap = executeSql(contextMap, sqlMap);
                contextMap.remove(CURRENT_DATA_PREFIX + item);
                if (contextMap != null && contextMap.get(PROCESS_BREAK_KEY) != null && Boolean.parseBoolean(contextMap.get(PROCESS_BREAK_KEY).toString())) {
                    break;
                }
            }
            return contextMap;
        }

    }

    /**
     * 描述：根据流程配置获取参数中的collection <br/>
     * 作者：jing.zhao@rogrand.com <br/>
     * 生成日期：2016-7-5 <br/>
     *
     * @param contextMap
     * @param stepObj
     * @return
     * @throws KkmyException
     */
    private Collection getCollection(PageParam contextMap, JSONObject stepObj) throws KkmyException {
        Object collectionObject = stepObj.get("@collection");
        if (collectionObject == null) {
            return null;
        }
        String collectionString = collectionObject.toString();
        Object collection = contextMap.get(collectionString);
        if (collection != null && (collection instanceof Collection || collection instanceof Collection)) {
            return (Collection) collection;
        } else {
            return null;
        }

    }

    /**
     * 执行sql通用查询接口 <br/>
     * 作者：jing.zhao@rogrand.com <br/>
     * 生成日期：2016-5-24 <br/>
     *
     * @param pageParam 通用查询上下文
     * @param sqlMap    sql配置
     * @return 通用查询上下文
     * @throws KkmyException
     */
    public PageParam executeSql(PageParam pageParam, Map<String, Object> sqlMap)
            throws KkmyException {

        String sql = processSql(sqlMap, pageParam);
        String sqltype = (String) sqlMap.get("@method");
        if ("select".equals(sqltype) || StringUtil.isEmpty(sqltype)) {
            // ////////动态排序///////////////////
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

            try {
                if (pageParam.get("page") != null
                        && pageParam.get("rows") != null) {
                    String countSql = "select count(1) from (" + sql
                            + ") as total ";
                    Long recordCount = commonMapper.queryCount(countSql);
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
                List list = commonMapper.queryList(sql);
                pageParam.put("pageList", list);
            } catch (Exception e) {
                e.printStackTrace();
                logger.error(e.getMessage());
                throw new KkmyException("执行sql错误，查询失败");
            }
        } else if ("query".equals(sqltype) || StringUtil.isEmpty(sqltype)) {

            try {
                pageParam.putAll(commonMapper.queryRecord(sql));
            } catch (Exception e) {
                e.printStackTrace();
                logger.error(e.getMessage());
                throw new KkmyException("执行sql错误，查询失败");
            }
        } else {
            if ("insert".equals(sqltype)) {
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
                    Object object = commonMapper.insertSql(searchSql);
                } catch (Exception e) {
                    e.printStackTrace();
                    throw new KkmyException("执行sql错误，添加失败");
                }
            } else if ("execute".equals(sqltype)) {
                try {
                    // /////////////////执行sql////////////////////////
                    CommonSearchSql searchSql = new CommonSearchSql(sql);
                    Object object = commonMapper.insertSql(searchSql);
                    System.out.println(object);
                } catch (Exception e) {
                    e.printStackTrace();
                    throw new KkmyException("执行sql错误，添加失败");
                }
            } else {
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
                    Object object = commonMapper.insertSql(searchSql);
                } catch (Exception e) {
                    throw new KkmyException("执行sql错误，添加失败");
                }
            }
        }

        return pageParam;
    }
}
