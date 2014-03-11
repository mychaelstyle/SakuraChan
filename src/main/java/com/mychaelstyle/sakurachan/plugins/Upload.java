/**
 * 
 */
package com.mychaelstyle.sakurachan.plugins;

import java.util.Map;

import org.json.JSONObject;

import com.mychaelstyle.sakurachan.Plugin;
import com.mychaelstyle.sakurachan.Worker;

/**
 * @author Masanori Nakashima
 *
 */
public class Upload implements Plugin {

    private JSONObject config = null;
    private Map<String,String> options = null;

    /**
     * 
     */
    public Upload() {
        super();
    }

    /* (non-Javadoc)
     * @see com.mychaelstyle.sakurachan.Plugin#prepare(org.json.JSONObject, java.util.Map)
     */
    @Override
    public void prepare(JSONObject config, Map<String, String> options)
            throws Exception {
        this.config = config;
        this.options = options;
        System.out.println(this.config.toString());
    }

    /* (non-Javadoc)
     * @see com.mychaelstyle.sakurachan.Plugin#doTask(com.mychaelstyle.sakurachan.Worker)
     */
    @Override
    public void doTask(Worker worker) throws Exception {
        String src = options.get("src");
        String dst = options.get("dst");
        if(null==src || src.length()==0
                || null==dst || dst.length()==0){
            throw new Exception("--src and --dst option is required!");
        }
        worker.transfer(src, dst);
    }

}
