/**
 * 
 */
package com.mychaelstyle.sakurachan;

import java.util.Map;

import org.json.JSONObject;

/**
 * 接続先ホスト情報を読み込みJSON形式で取得するインターフェース
 * 
 * @author Masanori Nakashima
 */
public interface HostsLoader {
    /**
     * コマンドラインオプションを受け取って接続先情報JSONを返す
     * <pre>
     * {
     *   "user" : "グローバル接続ユーザ名",
     *   "password" : "グローバル接続パスワード。鍵認証の場合はパスフレーズ。",
     *   "auth-file" : "鍵認証のみ指定するグローバル秘密鍵ファイルパス",
     *   "hosts" : {
     *     "ロール名" : [
     *         {
     *             "name":"ホスト名またはIPアドレス",
     *             "user":"ユーザ名。省略するとグローバルのユーザ名を利用",
     *             "password":"パスワード。省略するとグローバルパスワードを利用",
     *             "auth-file":"秘密鍵ファイルパス.省略するとグローバル秘密鍵を利用",
     *             "label": "このホストにつけるラベル"
     *          },
     *          ...
     *     ],
     *     "ロール名" : [
     *         ....
     *     ]
     *   }
     * }
     * </pre>
     * 
     * @param options
     * @return
     * @throws Exception
     */
    public JSONObject load(Map<String,String> options) throws Exception;
}
