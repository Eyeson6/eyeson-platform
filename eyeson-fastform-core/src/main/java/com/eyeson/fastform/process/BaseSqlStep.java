package com.eyeson.fastform.process;

import com.eyeson.fastform.common.CommonSearchUtils;
import com.eyeson.fastform.common.KkmyException;
import com.eyeson.fastform.common.PageParam;
import com.eyeson.fastform.common.StringUtil;
import com.eyeson.fastform.spring.AopTargetUtils;
import com.eyeson.fastform.spring.SpringContextHolder;
import net.sf.json.JSONArray;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class BaseSqlStep {

    private static final String CURRENT_DATA_PREFIX = "current_data_";

    private final static Logger logger = LoggerFactory
            .getLogger(BaseSqlStep.class);

    /**
     * 描述：反射调用静态方法 <br/>
     * 作者：jing.zhao@rogrand.com <br/>
     * 生成日期：2016-3-8 <br/>
     *
     * @return
     */
    protected Object invokeUtilMethod(String methodStirng, String methodParam,
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
                    String argValue = getParamValue(arg, paraMap);
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
     * 描述：从参数map中获取参数值，支持key=a.b的数据获取 <br/>
     * 作者：jing.zhao@rogrand.com <br/>
     * 生成日期：2016-7-5 <br/>
     *
     * @param key
     * @param paraMap
     * @return
     */
    public String getParamValue(String key, Map<String, Object> paraMap) {
        if (StringUtil.isEmpty(key)) {
            return null;
        }
        Object tempMap = paraMap;
        String[] keyArray = key.split("\\.");
        for (int i = 0; i < keyArray.length; i++) {
            if (tempMap == null) {
                return null;
            }
            if (tempMap instanceof Map) {
                if (i < keyArray.length - 1) {

                    tempMap = ((Map<String, Object>) tempMap).get(CURRENT_DATA_PREFIX + keyArray[i]);
                } else {
                    tempMap = ((Map<String, Object>) tempMap).get(keyArray[i]);
                }

            }
        }
        return tempMap == null ? null : tempMap.toString();
    }

//	
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
//	protected String processSql(Map<String, Object> sqlMap, PageParam pageParam)
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
//			List<String> paramNameList = matchSqlParam(sql);
//			for(String paramName : paramNameList){
//				String value = getParamValue(paramName, pageParam);
//				sql = sql
//						.replaceAll("\\%\\$\\{" + paramName + "\\}\\%",
//								"'%" + value + "%'")
//						.replaceAll("\\%\\$\\{" + paramName + "\\}",
//								"'%" + value + "'")
//						.replaceAll("\\$\\{" + paramName + "\\}\\%",
//								"'" + value + "%'")
//						.replaceAll("\\$\\{" + paramName + "\\}",
//								"'" + value + "'");
//			}
////			for (String currKey : pageParam.keySet()) {
////				// String conditionName = (String)
////				// conditionMap.get("@name");
////				Object conditionValue = pageParam.get(currKey);
////				try {
////					sql = sql
////							.replaceAll("\\%\\$\\{" + currKey + "\\}\\%",
////									"'%" + conditionValue.toString() + "%'")
////							.replaceAll("\\%\\$\\{" + currKey + "\\}",
////									"'%" + conditionValue.toString() + "'")
////							.replaceAll("\\$\\{" + currKey + "\\}\\%",
////									"'" + conditionValue.toString() + "%'")
////							.replaceAll("\\$\\{" + currKey + "\\}",
////									"'" + conditionValue.toString() + "'");
////				} catch (Exception e) {
////				}
////			}
//			
//			//sql里面支持service返回动态参数
//			List<String> ServiceNameList = matchServiceParam(sql);
//			for(String ServiceName : ServiceNameList){
//				String [] keyArray = ServiceName.split("\\.");
//				if(keyArray != null && keyArray.length == 2){
//					String value = invokeSpringMethod(pageParam,keyArray[0],keyArray[1]);
//					sql = sql.replaceAll("\\@\\{" + ServiceName + "\\}", value);
//				}else {
//					throw new KkmyException("sql配置错误,请检查配置");
//				}
//				
//			}
//			
//			
//			sql = sql.replaceAll("\n", "").replaceAll("\t", "")
//					.replaceAll("\\?", "");
//		} catch (Exception e) {
//			throw new KkmyException("查询条件配置错误，查询失败");
//		}
//
//		return sql;
//	}

    /**
     * 描述：处理sql配置,将配置转成sql语句,并填充参数 <br/>
     * 作者：jing.zhao@rogrand.com <br/>
     * 生成日期：2016-5-24 <br/>
     *
     * @param sqlMap    sql配置
     * @param pageParam 请求参数
     * @return 返回处理完后的sql字符串
     * @throws KkmyException
     */
    @SuppressWarnings("unchecked")
    public String processSql(Map<String, Object> sqlMap, PageParam pageParam)
            throws KkmyException {
        String sql;
        try {
            sql = spliceSql(pageParam, sqlMap);
            if (sqlMap.containsKey("set")) {
                List<Map<String, Object>> setList = null;
                Object setObject = sqlMap.get("set");
                if (setObject instanceof JSONArray) {
                    setList = (List<Map<String, Object>>) sqlMap.get("set");
                } else {
                    setList = new ArrayList<Map<String, Object>>();
                    setList.add((Map<String, Object>) sqlMap.get("set"));
                }
                for (Map<String, Object> setMap : setList) {
                    String suffix = (String) setMap.get("@suffix");
                    String subSql = spliceSql(pageParam, setMap);
                    if (!StringUtil.isEmpty(subSql) && !StringUtil.isEmpty(suffix)) {
                        int index = subSql.lastIndexOf(suffix);
                        subSql = subSql.substring(0, index) + subSql.substring(index + suffix.length());
                        subSql = subSql.replaceAll("\\$", "\\\\\\$").replaceAll("\\@", "\\\\\\@");
                        sql = sql.replaceFirst("\\#", subSql);
                    }
                }

            }
            // /////////////////////将参数注入到sql////////////////////////
            List<String> paramNameList = matchSqlParam(sql);
            for (String paramName : paramNameList) {
                String value = getParamValue(paramName, pageParam);
                sql = sql
                        .replaceAll("\\%\\$\\{" + paramName + "\\}\\%",
                                "'%" + value + "%'")
                        .replaceAll("\\%\\$\\{" + paramName + "\\}",
                                "'%" + value + "'")
                        .replaceAll("\\$\\{" + paramName + "\\}\\%",
                                "'" + value + "%'")
                        .replaceAll("\\$\\{" + paramName + "\\}",
                                "'" + value + "'");
            }

            //sql里面支持service返回动态参数
            List<String> ServiceNameList = matchServiceParam(sql);
            for (String ServiceName : ServiceNameList) {
                String[] keyArray = ServiceName.split("\\.");
                if (keyArray != null && keyArray.length == 2) {
                    String value = invokeSpringMethod(pageParam, keyArray[0], keyArray[1]);
                    sql = sql.replaceAll("\\@\\{" + ServiceName + "\\}", value);
                } else {
                    throw new KkmyException("sql配置错误,请检查配置");
                }

            }


            sql = sql.replaceAll("\n", "").replaceAll("\t", "")
                    .replaceAll("\\?", "");
        } catch (Exception e) {
            e.printStackTrace();
            throw new KkmyException("查询条件配置错误，查询失败");
        }

        return sql;
    }

    /**
     * 描述：sql的拼接,标签解析 <br/>
     * 作者：jing.zhao@rogrand.com <br/>
     * 生成日期：2016-7-14 <br/>
     *
     * @param sql
     * @param pageParam
     * @return
     * @throws KkmyException
     */
    private String spliceSql(PageParam pageParam, Map<String, Object> sqlMap) throws KkmyException {
        String sql;
        List<String> sqlList = null;
        try {
            Object object = sqlMap.get("#text");
            if (object instanceof JSONArray) {
                sqlList = (List<String>) JSONArray
                        .toCollection((JSONArray) object);
                // sqlArray = ((String [])sqlMap.get("#text"));
            } else {
                sqlList = new ArrayList<String>();
                sqlList.add((String) sqlMap.get("#text"));
            }
            sql = StringUtil.list2String(sqlList);
        } catch (Exception e) {
            throw new KkmyException("sql配置不正确，查询失败");
        }


        if (sqlMap.containsKey("if")) {
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
                    String value = ifMap.get("#text").toString();
//					value = "\\t and a.create_time >= ${startTime}";
                    value = value.replaceAll("\\$", "\\\\\\$").replaceAll("\\@", "\\\\\\@");
                    sql = sql.replaceFirst("\\?", value);
                } else {
                    sql = sql.replaceFirst("\\?", "");
                }
            }

        }
        return sql;
    }

    public List<String> matchSqlParam(String sql) throws KkmyException {
        List<String> list = new ArrayList<String>();
        int currentIndex = 0;
        int start = sql.indexOf("${", currentIndex);
        currentIndex = start;
        while (start >= 0) {
            currentIndex = start;
            int end = sql.indexOf("}", currentIndex);
            String paramName = sql.substring(start + 2, end);
            list.add(paramName);
            currentIndex = end;
            start = sql.indexOf("${", currentIndex);
        }
        return list;
    }


    private List<String> matchServiceParam(String sql) throws KkmyException {
        List<String> list = new ArrayList<String>();
        int currentIndex = 0;
        int start = sql.indexOf("@{", currentIndex);
        currentIndex = start;
        while (start >= 0) {
            currentIndex = start;
            int end = sql.indexOf("}", currentIndex);
            String paramName = sql.substring(start + 2, end);
            list.add(paramName);
            currentIndex = end;
            start = sql.indexOf("@{", currentIndex);
        }
        return list;
    }


    /**
     * 描述：基于反射执行spring-service方法 <br/>
     * 作者：jing.zhao@rogrand.com <br/>
     * 生成日期：2016-5-26 <br/>
     *
     * @param pageParam     上下文
     * @param serviceName   服务类名称
     * @param serviceMethod 服务方法名称
     * @return
     * @throws KkmyException
     */
    private String invokeSpringMethod(PageParam pageParam,
                                      String serviceName, String serviceMethod) throws KkmyException {
        ApplicationContext ac = SpringContextHolder.getApplicationContext();
        Object service = ac.getBean(serviceName);
        try {
            service = AopTargetUtils.getTarget(service);
            Class c = service.getClass();
            Method m = c.getMethod(serviceMethod, PageParam.class);
            Object object = m.invoke(service, pageParam);
            return object.toString();
        } catch (Exception e) {
            e.printStackTrace();
            logger.error(e.getMessage());
            throw new KkmyException("service配置不正确.serviceName" + serviceName
                    + ",serviceMethod:" + serviceMethod);
        }
    }

    public static void main(String[] args) {
        String str = " \t\t\t\t\t\t\t?  \t\t\t\t\t\t\t? \t\t\t\t\t\t\t, \t\t\t\t\t\t";
        String a = "\\t and a.create_time >= ${startTime}";
        a = a.replaceAll("\\$", "\\\\\\$");
        System.out.println(str.replaceFirst("\\?", a));
    }

}
