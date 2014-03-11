/**
 * 
 */
package com.mychaelstyle.sakurachan.plugins;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Map;

import org.json.JSONObject;

import com.mychaelstyle.sakurachan.Plugin;
import com.mychaelstyle.sakurachan.Worker;

/**
 * @author Masanori Nakashima
 *
 */
public class Shell implements Plugin {

    /**
     * Constructor
     */
    public Shell() {
        super();
    }

    /* (non-Javadoc)
     * @see com.mychaelstyle.sakurachan.Plugin#prepare(org.json.JSONObject, java.util.Map)
     */
    @Override
    public void prepare(JSONObject config, Map<String, String> options) throws Exception {
        // nothing to do
    }

    /* (non-Javadoc)
     * @see com.mychaelstyle.sakurachan.Plugin#doTask(com.mychaelstyle.sakurachan.Worker)
     */
    @Override
    public void doTask(Worker worker) throws Exception {
        System.out.print("sakura-chan > ");
        BufferedReader stdReader =
            new BufferedReader(new InputStreamReader(System.in));
        String line;
        try {
            while ((line = stdReader.readLine()) != null) { // ユーザの一行入力を待つ
                if ("".equalsIgnoreCase(line)){
                } else if("quit".equalsIgnoreCase(line)
                        || "exit".equalsIgnoreCase(line)) {
                    System.out.println("sakura-chan > bye bye!");
                    System.out.println();
                    break;
                } else if(line.startsWith("upload ")){
                    String[] elms = line.split(" ");
                    if(elms == null || elms.length<3){
                        System.err.println("upload require local file path and destination dir path.");
                        continue;
                    }
                    String srcPath = elms[1];
                    String dstPath = elms[2];
                    worker.upload(srcPath, dstPath);
                } else {
                    worker.run(line,null);
                }
                System.out.print("sakura-chan > ");
            }
        } finally {
        }
    }

}
