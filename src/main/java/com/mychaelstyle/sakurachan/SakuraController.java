/**
 * 
 */
package com.mychaelstyle.sakurachan;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONObject;

import com.mychaelstyle.util.MFile;

/**
 * Main class of Sakura Chan SSH
 * 
 * @author Masanori Nakashima
 *
 */
public class SakuraController {

    public static final String HOSTS_LOADER_DEFAULT = "com.mychaelstyle.sakurachan.DefaultHostsLoader";

    public static final String OPTION_CONFIG_PATH = "config-file";
    public static final String OPTION_ROLES = "roles";

    public static final String FILE_NAME_CONFIG = "config.json";
    public static final String PATH_CONFIG_DEFAULT = System.getenv("HOME")+"/.sakurachan/"+FILE_NAME_CONFIG;
    public static final String JSON_ITEM_PLUGINS = "plugins";
    public static final String JSON_ITEM_USER = "user";
    public static final String JSON_ITEM_PASSWORD = "password";
    public static final String JSON_ITEM_AUTH_KEY = "auth-file";
    public static final String JSON_ITEM_NAME = "name";
    public static final String JSON_ITEM_HOSTS = "hosts";
    public static final String JSON_ITEM_HOSTS_LOADER = "hosts-loader";

    public static final Map<String,String> PLUGINS = new HashMap<String,String>(){
        private static final long serialVersionUID = 1348352435338175750L;
        {
            put("shell", "com.mychaelstyle.sakurachan.plugins.Shell");
            put("invoke", "com.mychaelstyle.sakurachan.plugins.Invoke");
            put("upload", "com.mychaelstyle.sakurachan.plugins.Upload");
            put("plant", "com.mychaelstyle.sakurachan.plugins.Plant");
        }
    };

    /**
     * Constructor
     */
    public SakuraController() {
        super();
    }

    /**
     * @param args
     */
    public static void main(String[] args) {
        Map<String,String> options = argsToMap(args);
        List<String> singleOptions = pickSingleOptions(args);
        if(singleOptions.size()==0){
            System.out.println("Invalid arguments!");
            System.exit(1);
        }
        String pluginName = singleOptions.get(0);

        // load configuration
        JSONObject config = null;
        try {
            config = SakuraController.loadConfig(options);
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }

        // load targets hosts configuration
        String loaderName = HOSTS_LOADER_DEFAULT;
        HostsLoader loader = null;
        if(config.has(JSON_ITEM_HOSTS_LOADER)
                && config.getString(JSON_ITEM_HOSTS_LOADER).trim().length()>0){
            loaderName = config.getString(JSON_ITEM_HOSTS_LOADER);
        }
        try {
            Class<?> c = Class.forName(loaderName);
            loader = (HostsLoader) c.newInstance();
        } catch(Exception e){
            System.out.println("HostsLoader : "+loaderName+" is not found!");
            System.exit(1);
        }

        JSONObject hosts = null;
        try {
            hosts = loader.load(options);
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
        Map<String,String> plugins = mergePlugins(config);

        // create worker
        Worker worker = new Worker();
        String user = hosts.getString(JSON_ITEM_USER);
        String password = hosts.getString(JSON_ITEM_PASSWORD);
        String authKeyPath = null;
        if(hosts.has(JSON_ITEM_AUTH_KEY)){
            authKeyPath = hosts.getString(JSON_ITEM_AUTH_KEY);
        }

        String targetRoleStr = options.get(OPTION_ROLES);
        Collection<String> targetRoles = null;
        if(null!=targetRoleStr){
            targetRoles = Arrays.asList(targetRoleStr.split(","));
        }

        JSONObject hostsMap = hosts.getJSONObject(JSON_ITEM_HOSTS);
        @SuppressWarnings("unchecked")
        Collection<String> roles = hostsMap.keySet();
        System.out.println("Connectiong hosts ... ");
        try {
            for(String role : roles){
                if(targetRoles==null || targetRoles.contains(role)){
                    System.out.print("Role "+role+" hosts ... ");
                    JSONArray hostArray = hostsMap.getJSONArray(role);
                    for(int num=0; num<hostArray.length(); num++){
                        JSONObject host = hostArray.getJSONObject(num);
                        String name = host.getString(JSON_ITEM_NAME);
                        String us = user;
                        if(host.has(JSON_ITEM_USER)){
                            us = host.getString(JSON_ITEM_USER);
                        }
                        String pw = password;
                        if(host.has(JSON_ITEM_PASSWORD)){
                            pw = host.getString(JSON_ITEM_PASSWORD);
                        }
                        String authPath = authKeyPath;
                        if(host.has(JSON_ITEM_AUTH_KEY)){
                            authPath = host.getString(JSON_ITEM_AUTH_KEY);
                        }
                        if(num%5==0){
                            System.out.println();
                        }
                        System.out.print(name+", ");
                        worker = worker.withHost(name, us, pw, authPath);
                    }
                    System.out.println();
                }
            }
        } catch(Exception e){
            e.printStackTrace();
            System.exit(1);
        }
        System.out.println();

        // exec plugin on worker
        String cname = plugins.get(pluginName);
        Plugin plugin = null;
        try {
            Class<?> clz = Class.forName(cname);
            plugin = (Plugin) clz.newInstance();
            plugin.prepare(config,options);
            plugin.doTask(worker);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            System.exit(1);
        } catch (InstantiationException e) {
            e.printStackTrace();
            System.exit(1);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            System.exit(1);
        } catch (Throwable e) {
            e.printStackTrace();
            System.exit(1);
        } finally {
            worker.close();
        }
    }

    protected static JSONObject loadConfig(Map<String,String> options) throws IOException{
        // read config json
        String configFilePath = null;
        if(options.containsKey(OPTION_CONFIG_PATH)){
            configFilePath = options.get(OPTION_CONFIG_PATH);
            if(!(new File(configFilePath)).exists()){
                configFilePath = null;
            }
        }
        if(null==configFilePath){
            configFilePath = "config.json";
            if(!(new File(configFilePath)).exists()){
                configFilePath = PATH_CONFIG_DEFAULT;
                if(!(new File(configFilePath)).exists()){
                    System.out.println("config.json is not found!");
                    System.exit(1);
                }
            }
        }
        System.out.println("use config "+(new File(configFilePath)).getAbsolutePath());
        JSONObject config = null;
        String str = MFile.fileGetContents(new File(configFilePath), "UTF-8");
        config = new JSONObject(str);
        return config;
    }

    protected static Map<String,String> mergePlugins(JSONObject config){
        Map<String,String> map = new HashMap<String,String>();
        for(String key : PLUGINS.keySet()){
            String name = PLUGINS.get(key);
            map.put(key, name);
        }
        if(config.has(JSON_ITEM_PLUGINS)){
            JSONObject plugins = config.getJSONObject(JSON_ITEM_PLUGINS);
            @SuppressWarnings("unchecked")
            Set<String> keys = plugins.keySet();
            for(String key : keys){
                String name = plugins.getString(key);
                map.put(key, name);
            }
        }
        return map;
    }

    protected static List<String> pickSingleOptions(String[] args){
        List<String> options = new ArrayList<String>();
        for(String arg:args){
            if(!arg.startsWith("--")){
                options.add(arg);
            }
        }
        return options;
    }

    protected static Map<String,String> argsToMap(String[] args){
        Map<String,String> options = new HashMap<String,String>();
        for(String arg:args){
            if(arg.startsWith("--")){
                String elm = arg.substring(2);
                if(elm.contains("=")){
                    options.put(elm.substring(0,elm.indexOf("=")),
                            elm.substring(elm.indexOf("=")+1));
                } else {
                    options.put(elm,elm);
                }
            }
        }
        return options;
    }
}
