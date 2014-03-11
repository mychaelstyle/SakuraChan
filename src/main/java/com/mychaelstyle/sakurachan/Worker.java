/**
 * 
 */
package com.mychaelstyle.sakurachan;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Worker SSH/SCP
 * 
 * @author Masanori Nakashima
 */
public class Worker {

    private Map<String,Node> nodes = new HashMap<String,Node>();

    /**
     * Constructor
     */
    public Worker() {
    }

    /**
     * with host
     * @param host
     * @param user
     * @param password
     * @param authFilePath
     * @return
     * @throws Exception
     */
    protected Worker withHost(String host,String user,String password, String authFilePath)
            throws Exception{
        Node node = new Node();
        node.connect(host, user, password, authFilePath);
        this.nodes.put(host, node);
        return this;
    }

    public void close(){
        for(String host : this.nodes.keySet()){
            final Node node = this.nodes.get(host);
            node.close();
        }
    }

    public Worker transfer(final String src, final String dest) throws Exception {
        for(String host : this.nodes.keySet()){
            final Node node = this.nodes.get(host);
            node.scpUpload(src, dest);
        }
        return this;
    }

    /**
     * execute command in the all hosts.
     * @param command shell command line
     * @param stdins optional standard in
     * @throws IOException 
     * @throws InterruptedException 
     */
    public Worker run(final String command, final String[] stdins) throws IOException, InterruptedException{
        for(String host : this.nodes.keySet()){
            final Node node = this.nodes.get(host);
            node.execCommand(command, stdins);
        }
        return this;
    }

    /**
     * upload local file to each host.
     * @param filePath
     * @param destPath
     * @return
     * @throws IOException
     * @throws InterruptedException
     */
    public Worker upload(final String filePath, final String destPath) throws IOException, InterruptedException {
        for(String host : this.nodes.keySet()){
            final Node node = this.nodes.get(host);
            node.scpUpload(filePath, destPath);
        }
        return this;
    }

    public Worker startShell() throws IOException, InterruptedException{
        for(String host : this.nodes.keySet()){
            final Node node = this.nodes.get(host);
            node.startShell();
        }
        return this;
    }

    public Worker closeShell() throws IOException, InterruptedException {
        for(String host : this.nodes.keySet()){
            final Node node = this.nodes.get(host);
            node.closeShell();
        }
        return this;
    }

    public Worker runOnShell(String command) throws IOException, InterruptedException{
        for(String host : this.nodes.keySet()){
            final Node node = this.nodes.get(host);
            node.execOnShell(command);
        }
        return this;
    }
}
