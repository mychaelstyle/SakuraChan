/**
 * 
 */
package com.mychaelstyle.sakurachan;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.ByteBuffer;

import com.trilead.ssh2.ChannelCondition;
import com.trilead.ssh2.Connection;
import com.trilead.ssh2.ConnectionInfo;
import com.trilead.ssh2.SCPClient;
import com.trilead.ssh2.Session;
import com.trilead.ssh2.StreamGobbler;

/**
 * Sakura chan SSH & SCP Node class
 * @author Masanori Nakashima
 *
 */
public class Node {

    private String hostName = null;
    private String user = null;
    private String password = null;
    private String keyFilePath = null;

    private Connection conn = null;
    private Session session = null;
    private ConnectionInfo info = null;

    private OutputStream stdin = null;
    private InputStream stdout = null;
    private InputStream stderror = null;

    private String promptStrings = null;

    private Printable printer = new SystemPrinter();

    /**
     * Constructor
     */
    public Node() {
        super();
    }

    public Node withHost(String hostName){
        this.hostName = hostName;
        return this;
    }

    public Node withUser(String user){
        this.user = user;
        return this;
    }

    public Node withPassword(String pass){
        this.password = pass;
        return this;
    }

    public Node withPrivateKey(String path){
        this.keyFilePath = path;
        return this;
    }

    public Node withPrinter(Printable printer){
        this.printer = printer;
        return this;
    }

    /**
     * connect host
     * @throws Exception
     */
    public void connect() throws Exception {
        this.conn = new Connection(this.hostName);
        this.info = this.conn.connect();
        if(null==this.keyFilePath|| this.keyFilePath.trim().length()==0){
            if(!this.conn.authenticateWithPassword(this.user, this.password)){
                throw new Exception("Fail to authenticate "+this.hostName);
            }
        } else {
            File authFile = new File(this.keyFilePath);
            if(!authFile.exists()){
                throw new Exception("pem file "+this.keyFilePath+" is not exist!");
            } else if(!this.conn.authenticateWithPublicKey(user, authFile, password)){
                throw new Exception("Fail to authenticate "+this.hostName);
            }
        }
    }

    /**
     * connect a host
     * @param hostname
     * @param user
     * @param password
     * @param authFilePath
     * @throws Exception
     */
    public void connect(String hostname, String user, String password, String authFilePath)
            throws Exception {
        this.hostName = hostname;
        this.user = user;
        this.password = password;
        this.keyFilePath = authFilePath;
        this.connect();
    }

    /**
     * close
     */
    public void close(){
        if(null!=this.conn){
            this.conn.close();
        }
    }

    public void startShell() throws IOException, InterruptedException{
        this.session.startShell();
        this.stdin = this.session.getStdin();
        this.stdout = this.session.getStdout();
        this.stderror = this.session.getStderr();
        this.streamToOut(session, stdout, stderror, this.printer);
    }

    public void closeShell() throws IOException, InterruptedException {
        if(null!=this.stdin){
            this.stdin.close();
        }
        if(null!=this.stderror){
            this.stderror.close();
        }
        if(null!=this.stdout){
            this.stdout.close();
        }
        if(null!=this.session){
            this.session.close();
        }
    }

    public void execOnShell(String command) throws IOException, InterruptedException {
        if("quit".equalsIgnoreCase(command)
            || "exit".equalsIgnoreCase(command)) {
            this.closeShell();
            return;
        } else {
            this.stdin.write(command.getBytes());
            this.stdin.write('\n');
            this.stdin.flush();
            this.streamToOut(this.session, this.stdout, this.stderror, this.printer);
        }
    }

    /**
     * execute command
     * @param conn Connection
     * @param command command strings
     * @param stdin standard in strings array
     * @throws IOException
     * @throws InterruptedException 
     */
    public void execCommand(String command, String stdin[]) throws IOException, InterruptedException {
        if(this.conn.isAuthenticationComplete()){
            Session session = this.conn.openSession();
            session.execCommand(command);
            if(null!=stdin){
                for(String str : stdin){
                    session.getStdin().write((str+"\n").getBytes());
                }
            }

            StreamGobbler outGobbler = null;
            StreamGobbler errGobbler = null;
            BufferedReader brOut = null;
            BufferedReader brErr = null;

            try {
                outGobbler = new StreamGobbler(session.getStdout());
                brOut = new BufferedReader(new InputStreamReader(outGobbler,"UTF8"));
                while (true) {
                    String lineOut = brOut.readLine();
                    if (lineOut == null) {
                        break;
                    }
                    this.printer.println(lineOut);
                }

                errGobbler = new StreamGobbler(session.getStderr());
                brErr = new BufferedReader(new InputStreamReader(errGobbler,"UTF8"));
                while (true) {
                    String lineErr = brErr.readLine();
                    if (lineErr == null) {
                        break;
                    }
                    this.printer.println("err("+session.getExitStatus()+") "+lineErr);
                }
            } finally {
                if(brOut!=null){
                    brOut.close();
                }
                if(brErr!=null){
                    brErr.close();
                }
            }
        }
    }

    /**
     * SCP
     * @param conn
     * @param srcPath
     * @param destPath
     * @throws IOException
     */
    public void scpUpload(String srcPath, String destPath) throws IOException {
        SCPClient scp = this.conn.createSCPClient();
        scp.put(srcPath, destPath);
    }

    private boolean isReulstEnd(String tail){
        if(tail!=null && promptStrings!=null
                && tail.equalsIgnoreCase(this.promptStrings)){
            return true;
        }
        return false;
    }

    private void streamToOut(Session session, InputStream stdout,
            InputStream stderr, Printable printer) throws IOException, InterruptedException {
        int timeoutInMillis = 2000;
        int conditions = ChannelCondition.STDOUT_DATA | 
                                 ChannelCondition.STDERR_DATA | 
                                 ChannelCondition.CLOSED | 
                                 ChannelCondition.EOF;
        ByteBuffer bufStd = ByteBuffer.allocate(8096);
        ByteBuffer bufErr = ByteBuffer.allocate(8096);
        int outLen = 1;
        int errLen = 1;
        String tail = "";
        int condition = session.waitForCondition(conditions, timeoutInMillis);
        while ((outLen>0 || errLen>0) && !this.isReulstEnd(tail) && condition!=ChannelCondition.EOF){
            byte[] resOut = new byte[1024];
            byte[] resErr = new byte[1024];
            outLen = 0;
            errLen = 0;
            int counter = 0;
            while(stdout.available()<=0 && counter<5){
                Thread.sleep(300);
                counter++;
            }
            while(stdout.available()>0 && (outLen=stdout.read(resOut))>0){
                bufStd.put(resOut);
                Thread.sleep(300);
            }
            counter = 0;
            while(stderr.available()<=0 && counter<5){
                Thread.sleep(300);
                counter++;
            }
            while(stderr.available()>0 && (errLen=stderr.read(resErr))>0){
                bufErr.put(resErr);
            }
            String str = new String(bufStd.array(),"UTF-8");
            String[] lines = str.trim().replace("\r\n", "\n")
                    .replace("\r", "\n").split("\n");
            tail = lines[lines.length-1];
            if(!this.isReulstEnd(tail) && condition!=ChannelCondition.EOF){
                condition = session.waitForCondition(conditions, timeoutInMillis);
            } else {
                break;
            }
        }
        this.promptStrings = tail;
        String str = new String(bufStd.array(),"UTF-8");
        if(str.length()>0){
            String[] lines = str.trim().replace("\r\n", "\n")
                .replace("\r", "\n").split("\n");
            for(String line :lines){
                printer.println(line);
            }
        }
        str = new String(bufErr.array(),"UTF-8");
        if(str.length()>0){
            String[] lines = str.trim().replace("\r\n", "\n")
                .replace("\r", "\n").split("\n");
            for(String line :lines){
                printer.printlnError(line);
            }
        }
    }

    public interface Printable {
        void print(String str) throws IOException;
        void println(String str) throws IOException;
        void printError(String str) throws IOException;
        void printlnError(String str) throws IOException;
    }

    public class SystemPrinter implements Printable {
        @Override
        public void print(String str) throws IOException {
            System.out.print("["+user+"@"+hostName+"] " + str);
        }
        @Override
        public void println(String str) throws IOException {
            System.out.println("["+user+"@"+hostName+"] " + str);
        }
        @Override
        public void printError(String str) throws IOException {
            System.err.print("["+user+"@"+hostName+"] " + str);
        }
        @Override
        public void printlnError(String str) throws IOException {
            System.err.println("["+user+"@"+hostName+"] " + str);
        }
    }

    /**
     * @return the info
     */
    public ConnectionInfo getInfo() {
        return info;
    }

    /**
     * @return the hostName
     */
    public String getHostName() {
        return hostName;
    }

}
