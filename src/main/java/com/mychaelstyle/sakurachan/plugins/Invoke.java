/**
 * 
 */
package com.mychaelstyle.sakurachan.plugins;

import java.util.Map;

import org.json.JSONObject;

import com.mychaelstyle.sakurachan.Plugin;
import com.mychaelstyle.sakurachan.Worker;

/**
 * Invoke plugin
 * 
 * @author Masanori Nakashima
 */
public class Invoke implements Plugin {

    private Map<String,String> options = null;

    /**
     * Constructor
     */
    public Invoke() {
        super();
    }

    /* (non-Javadoc)
     * @see com.mychaelstyle.sakurachan.Plugin#prepare(org.json.JSONObject)
     */
    @Override
    public void prepare(JSONObject config, Map<String, String> options) throws Exception {
        this.options = options;
    }

    /* (non-Javadoc)
     * @see com.mychaelstyle.sakurachan.Plugin#doTask(com.mychaelstyle.sakurachan.Worker, java.util.Map)
     */
    @Override
    public void doTask(Worker worker) throws Exception {
        String cmd = options.get("CMD");
        if(null==cmd || cmd.length()==0){
            throw new Exception("--CMD option is required!");
        }
        worker.run(cmd, null);
    }

}
