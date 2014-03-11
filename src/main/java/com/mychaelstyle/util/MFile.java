/**
 * ファイルを扱うユーティリティクラス
 * @author Masanori Nakashima
 */
package com.mychaelstyle.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

import org.json.JSONObject;

/**
 * ファイルを扱うユーティリティクラス
 * @author Masanori Nakashima
 */
public class MFile {

    /**
     * constructor
     */
    private MFile(){
        super();
    }
    /**
     * UTF-8で一時ファイルに文字列を書き込んで保存しFileオブジェクトを返す
     * @param body
     * @param prefix
     * @param suffix
     * @return
     * @throws IOException
     */
    public static File tmp(String body, String prefix, String suffix) throws IOException{
        String encBody = new String(body.getBytes("UTF-8"), "UTF-8");
        return tmp(encBody, prefix, suffix, "UTF-8");
    }

    /**
     * 一時ファイルに文字列を書き込んで保存しFileオブジェクトを返す
     * @param body
     * @param prefix
     * @param suffix
     * @param charset
     * @return
     * @throws IOException
     */
    public static File tmp(String body, String prefix, String suffix, String charset) throws IOException{
        File tmpFile = File.createTempFile(prefix,suffix);
        filePutContents(tmpFile,body,charset);
        return tmpFile;
    }

    /**
     * ファイルパスを指定して文字列を書き込む
     * @param filePath
     * @param body
     * @param charset
     * @throws IOException
     */
    public static void filePutContents(String filePath, String body, String charset) throws IOException {
        File file = new File(filePath);
        filePutContents(file,body,charset);
    }

    /**
     * ファイルを指定して文字列を書き込む
     * @param file
     * @param body
     * @param charset
     * @throws IOException
     */
    public static void filePutContents(File file, String body, String charset) throws IOException {
        FileOutputStream fos = null;
        OutputStreamWriter osw = null;
        BufferedWriter writer = null;
        try {
            fos = new FileOutputStream(file);
            osw = new OutputStreamWriter(fos,charset);
            writer = new BufferedWriter(osw);
            writer.write(body);
        } finally {
            if(null!=writer){
                writer.close();
            }
            if(null!=osw){
                osw.close();
            }
            if(null!=fos){
                fos.close();
            }
        }
    }

    public static String fileGetContents(File file, String charset)
            throws IOException {
        FileInputStream is = null;
        InputStreamReader streamReader = null;
        BufferedReader reader = null;
        try {
            is = new FileInputStream(file);
            streamReader = new InputStreamReader(is,charset);
            reader = new BufferedReader(streamReader);
            StringBuffer buf = new StringBuffer();
            String line = reader.readLine();
            while(null!=line){
                buf.append(line);
                line = reader.readLine();
            }
            return buf.toString();
        } finally {
            if(null!=reader){
                try{
                reader.close();
                } finally{
                    try {
                        if(null!=streamReader){
                            streamReader.close();
                        }
                    } finally{
                        if(null!=is){
                            is.close();
                        }
                    }
                }
            }
        }
    }

    /**
     * 
     * @param resourceName
     * @return
     * @throws IOException 
     */
    
    public static JSONObject getResourceAsJSON(String resourceName) throws IOException{
        String content = getResourceAsString(resourceName);
        if(null!=content && content.length()>0){
            return new JSONObject(content);
        }
        return null;
    }

    /**
     * リソースファイルを文字列として取得
     * @param resourceName
     * @return
     * @throws IOException
     */
    public static String getResourceAsString(String resourceName) throws IOException{
        InputStream is = null;
        InputStreamReader ir = null;
        BufferedReader reader = null;
        StringBuffer buf = new StringBuffer();
        try {
            is = MFile.class.getClassLoader().getResourceAsStream(resourceName);
            ir = new InputStreamReader(is);
            reader = new BufferedReader(ir);
            String line = reader.readLine();
            while(null!=line){
                buf.append(line);
                line = reader.readLine();
            }
            return buf.toString();
        } finally {
            try {
                if(null!=reader){
                    reader.close();
                }
            } finally {
                try{
                    if(null!=ir){
                        ir.close();
                    }
                } finally {
                    if(null!=is){
                        is.close();
                    }
                }
            }
        }

    }
}
