/**
 * 
 */
package com.mychaelstyle.sakurachan;

import java.util.Map;

import org.json.JSONObject;

/**
 * Sakura chan plugin interface
 * 
 * @author Masanori Nakashima
 */
public interface Plugin {

    public void prepare(JSONObject config, Map<String,String> options) throws Exception;

    public void doTask(Worker worker) throws Exception;

}
