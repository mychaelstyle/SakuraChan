/**
 * 
 */
package com.mychaelstyle.sakurachan.plugins;

import java.io.File;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;

import com.mychaelstyle.sakurachan.Plugin;
import com.mychaelstyle.sakurachan.Worker;
import com.mychaelstyle.util.MFile;

/**
 * @author Masanori Nakashima
 *
 */
public class Plant implements Plugin {

    private JSONObject seedJson = null;

    /**
     * 
     */
    public Plant() {
        super();
    }

    /* (non-Javadoc)
     * @see com.mychaelstyle.sakurachan.Plugin#prepare(org.json.JSONObject, java.util.Map)
     */
    @Override
    public void prepare(JSONObject config, Map<String, String> options)
            throws Exception {
        String seedFilePath = options.get("seed-file");
        if(null==seedFilePath || seedFilePath.length()==0){
            seedFilePath = "seed.json";
        }
        File f = new File(seedFilePath);
        if(!f.exists()){
            throw new Exception("The seed file "+seedFilePath+" is not exists!");
        }
        String str = MFile.fileGetContents(f, "UTF-8");
        if(null==str || str.length()==0){
            throw new Exception("The seed file is empty file!");
        }
        seedJson = new JSONObject(str);
        if(null==seedJson || seedJson.length()==0){
            throw new Exception("The seed file is empty file!");
        }
    }

    /* (non-Javadoc)
     * @see com.mychaelstyle.sakurachan.Plugin#doTask(com.mychaelstyle.sakurachan.Worker)
     */
    @Override
    public void doTask(Worker worker) throws Exception {
        JSONArray actions = seedJson.getJSONArray("actions");
        for(int num=0; num<actions.length(); num++){
            JSONObject action = actions.getJSONObject(num);
            System.out.println(action.toString());
            if(action.has("command")){
                String[] optionalStdin = null;
                if(action.has("stdin")){
                    JSONArray arr = action.getJSONArray("stdin");
                    optionalStdin = new String[arr.length()];
                    for(int n=0; n<arr.length(); n++){
                        optionalStdin[n] = arr.getString(n);
                    }
                }
                worker.run(action.getString("command"), optionalStdin);
            } else if(action.has("upload")){
                if(!action.has("dst")){
                    throw new Exception("dst is required in upload action! action :"+num);
                }
                String src = action.getString("upload");
                String dst = action.getString("dst");
                worker.upload(src, dst);
            }
        }
    }

}
