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

import java.util.List;
import java.util.Map;

@Service("sqlStep")
public class SqlStep extends BaseSqlStep implements Step {

    private final static Logger logger = LoggerFactory.getLogger(SqlStep.class);

    private static final String SQLID_PREFIX = "sqlid_";
    @Autowired
    private FastCommonMapper commonMapper;

    @Autowired
    private CommonSearchTemplateService commonSearchTemplateService;

    @Override
    public PageParam execute(PageParam contextMap, JSONObject stepObj,
                             String menuId) throws KkmyException {
        logger.info("开始执行sql流程:" + stepObj);
        String sqlId = (String) stepObj.get("@ref");
        Map<String, Object> sqlMap = commonSearchTemplateService.getTempSql(
                sqlId, menuId);
        return executeSql(contextMap, sqlMap);
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
                if (orderSql.replaceAll(" ", "").toLowerCase().contains("orderby")) {
//					sql = sql + "," + sort + " " + order;
                    sql = "select * from (" + sql + ")containtable order by " + sort + " " + order;
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
        } else if ("list".equals(sqltype)) {
            try {
                List list = commonMapper.queryList(sql);
                pageParam.put("pageList", list);
            } catch (Exception e) {
                e.printStackTrace();
                logger.error(e.getMessage());
                throw new KkmyException("执行sql错误，查询失败");
            }
        } else if ("query".equals(sqltype) || StringUtil.isEmpty(sqltype)) {

            try {
                Map<String, Object> queryResult = commonMapper.queryRecord(sql);
                if (queryResult != null) {
                    pageParam.putAll(commonMapper.queryRecord(sql));
                }
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
                    pageParam.put("last_affected_number", object);
                    if (searchSql.getKeyProperty() != null) {
                        pageParam
                                .put("keyProperty", searchSql.getKeyProperty());
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    throw new KkmyException("执行sql错误，添加失败");
                }
            } else if ("execute".equals(sqltype)) {
                try {
                    // /////////////////执行sql////////////////////////
                    CommonSearchSql searchSql = new CommonSearchSql(sql);
                    Object object = commonMapper.insertSql(searchSql);
                    pageParam.put("last_affected_number", object);
                    if (searchSql.getKeyProperty() != null) {
                        pageParam
                                .put("keyProperty", searchSql.getKeyProperty());
                    }
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
                    if (searchSql.getKeyProperty() != null) {
                        pageParam
                                .put("keyProperty", searchSql.getKeyProperty());
                    }
                    Object object = commonMapper.insertSql(searchSql);
                    pageParam.put("last_affected_number", object);
                } catch (Exception e) {
                    throw new KkmyException("执行sql错误，添加失败");
                }
            }
        }

        return pageParam;
    }

//	/**
//	 * 
//	 * 描述：处理sql配置,将配置转成sql语句,并填充参数 <br/>
//	 * 作者：jing.zhao@rogrand.com <br/>
//	 * 生成日期：2016-5-24 <br/>
//	 * 
//	 * @param sqlMap
//	 *            sql配置
//	 * @param pageParam
//	 *            请求参数
//	 * @return 返回处理完后的sql字符串
//	 * @throws KkmyException
//	 */
//	@SuppressWarnings("unchecked")
//	private String processSql(Map<String, Object> sqlMap, PageParam pageParam)
//			throws KkmyException {
//		String sql;
//		List<String> sqlList = null;
//		int sqlArrayLength = 0;
//		StringBuffer sb;
//		int index = 0;
//		try {
//			Object object = sqlMap.get("#text");
//			if (object instanceof JSONArray) {
//				sqlList = (List<String>) JSONArray
//						.toCollection((JSONArray) object);
//				// sqlArray = ((String [])sqlMap.get("#text"));
//			} else {
//				sqlList = new ArrayList<String>();
//				sqlList.add((String) sqlMap.get("#text"));
//			}
//			sqlArrayLength = sqlList.size();
//			sql = sqlList.get(index++);
//			sb = new StringBuffer(sql);
//		} catch (Exception e) {
//			throw new KkmyException("sql配置不正确，查询失败");
//		}
//		// /////////////////处理if条件////////////////////////////////
//
//		try {
//			if (sqlMap.containsKey("if")) {
//				Object ifObject = sqlMap.get("if");
//				List<Map<String, Object>> ifList = null;
//				if (ifObject instanceof JSONArray) {
//					ifList = (List<Map<String, Object>>) sqlMap.get("if");
//				} else {
//					ifList = new ArrayList<Map<String, Object>>();
//					ifList.add((Map<String, Object>) sqlMap.get("if"));
//				}
//				for (Map<String, Object> ifMap : ifList) {
//					String methodString = (String) ifMap.get("@method");
//					String methodParam = (String) ifMap.get("@args");
//					boolean flag = (Boolean) invokeUtilMethod(methodString,
//							methodParam, pageParam);
//					if (flag) {
//						sb.append(" " + ifMap.get("#text"));
//					}
//					if (index < sqlArrayLength) {
//						sb.append(sqlList.get(index++));
//					}
//				}
//
//			}
//			// /////////////////////将参数注入到sql////////////////////////
//			sql = sb.toString();
//			for (String currKey : pageParam.keySet()) {
//				// String conditionName = (String)
//				// conditionMap.get("@name");
//				Object conditionValue = pageParam.get(currKey);
//				try {
//					sql = sql
//							.replaceAll("\\%\\$\\{" + currKey + "\\}\\%",
//									"'%" + conditionValue.toString() + "%'")
//							.replaceAll("\\%\\$\\{" + currKey + "\\}",
//									"'%" + conditionValue.toString() + "'")
//							.replaceAll("\\$\\{" + currKey + "\\}\\%",
//									"'" + conditionValue.toString() + "%'")
//							.replaceAll("\\$\\{" + currKey + "\\}",
//									"'" + conditionValue.toString() + "'");
//				} catch (Exception e) {
//				}
//			}
//			sql = sql.replaceAll("\n", "").replaceAll("\t", "")
//					.replaceAll("\\?", "");
//		} catch (Exception e) {
//			throw new KkmyException("查询条件配置错误，查询失败");
//		}
//
//		return sql;
//	}
}
