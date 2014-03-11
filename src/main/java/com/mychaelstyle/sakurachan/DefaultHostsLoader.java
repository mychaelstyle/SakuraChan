/**
 * 
 */
package com.mychaelstyle.sakurachan;

import java.io.File;
import java.util.Map;

import org.json.JSONObject;

import com.mychaelstyle.util.MFile;

/**
 * Default host loader.
 * 
 * <ol>
 * <li>command line option, --hosts-file="File path"</li>
 * <li>./hosts.json</li>
 * <li>$HOME/.sakurachan/hosts.json</li>
 * </ol>
 * 
 * @author Masanori Nakashima
 */
public class DefaultHostsLoader implements HostsLoader {

    /** default hosts configuration file name */
    public static final String FILE_NAME_HOSTS = "hosts.json";
    /** default hosts.json file path */
    public static final String PATH_HOSTS_DEFAULT = System.getenv("HOME")+"/.sakurachan/"+FILE_NAME_HOSTS;
    /** command line option : hosts file path */
    public static final String OPTION_HOSTS_PATH = "hosts-file";

    /**
     * Constructor
     */
    public DefaultHostsLoader() {
        super();
    }

    /* (non-Javadoc)
     * @see com.mychaelstyle.sakurachan.HostsLoader#load(java.util.Map)
     */
    @Override
    public JSONObject load(Map<String, String> options) throws Exception {
        String hostsFilePath = null;
        if(options.containsKey(OPTION_HOSTS_PATH)){
            hostsFilePath = options.get(OPTION_HOSTS_PATH);
            if(!(new File(hostsFilePath)).exists()){
                throw new Exception("hosts json "+hostsFilePath+" is not found!");
            }
        }
        if(null==hostsFilePath){
            hostsFilePath = "hosts.json";
            if(!(new File(hostsFilePath)).exists()){
                hostsFilePath = PATH_HOSTS_DEFAULT;
                if(!(new File(hostsFilePath)).exists()){
                    System.out.println("hosts.json is not found!");
                    throw new Exception("hosts.json is not found!");
                }
            }
        }
        System.out.println("use hosts "+(new File(hostsFilePath)).getAbsolutePath());
        JSONObject hosts = null;
        String str = MFile.fileGetContents(new File(hostsFilePath), "UTF-8");
        hosts = new JSONObject(str);
        return hosts;
    }

}
