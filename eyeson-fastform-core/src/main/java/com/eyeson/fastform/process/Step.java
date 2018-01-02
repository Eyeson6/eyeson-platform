package com.eyeson.fastform.process;

import com.eyeson.fastform.common.KkmyException;
import com.eyeson.fastform.common.PageParam;
import net.sf.json.JSONObject;

/**
 * 版权：融贯资讯 <br/>
 * 作者：jing.zhao@rogrand.com <br/>
 * 生成日期：2016-5-17 <br/>
 * 描述：流程节点类
 */
public interface Step {

    /**
     * 描述：执行流程节点 <br/>
     * 作者：jing.zhao@rogrand.com <br/>
     * 生成日期：2016-5-17 <br/>
     *
     * @param map
     * @return
     * @throws KkmyException
     */
    public PageParam execute(PageParam map, JSONObject stepObj, String menuId) throws KkmyException;
}
