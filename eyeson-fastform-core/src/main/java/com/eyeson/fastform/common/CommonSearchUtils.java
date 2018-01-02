package com.eyeson.fastform.common;

import java.math.BigDecimal;


/**
 * 版权：融贯资讯 <br/>
 * 作者：jing.zhao@rogrand.com <br/>
 * 生成日期：2016-3-8 <br/>
 * 描述：通用查询工具类
 */
public class CommonSearchUtils {
    public static boolean isNoEmpty(Object object) {
        if (object == null) {
            return false;
        } else if (StringUtil.isEmpty(object.toString())) {
            return false;
        }
        return true;
    }

    public static boolean isEmpty(Object object) {
        if (object == null) {
            return true;
        } else if (StringUtil.isEmpty(object.toString())) {
            return true;
        }
        return false;
    }

    public static boolean isNum(Object object) {
        if (object == null) {
            return false;
        }

        if (object instanceof Integer || object instanceof Double || object instanceof BigDecimal || object instanceof Float || object instanceof Long) {
            return true;
        }
        return false;
    }

    public static boolean notEquals(Object object, Object obj) {
        if (object == null) {
            return false;
        } else if (StringUtil.isEmpty(object.toString())) {
            return false;
        }
        if (object instanceof String && obj instanceof String) {
            if (((String) object).equals((String) obj)) {
                return false;
            }
        }
        return true;
    }
}
