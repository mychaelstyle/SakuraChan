mychaelstyle / SakuraChan
=====

Sakura-chan is a SSH/SCP client for operating on many servers at once.
Similer to capistrano shell command.

# Get started

## requirement

+ git <http://git-scm.com/>
+ gradle <http://www.gradle.org/> or maven2 <http://maven.apache.org/>

## Get started using gradle

    mkdir $HOME/bin $HOME/lib ; cd $HOME/lib
    git clone git@github.com:mychaelstyle/SakuraChan.git ; cd SakuraChan ; gradle jar
    ln -s $HOME/lib/SakuraChan/sakurachan $HOME/bin/sakurachan
    echo 'export PATH="$PATH:$HOME/bin" >> $HOME/.bashrc

## Configuration

Sakura-chan search the config.json in current working folder, next, $HOME/.sakurachan/config.json.

### e.g. config.json

You can customize and use your own class for these configurations, implements HostsLoader or Plugin.
But can use default.

    {
      "hosts-loader" : "com.mychaelstyle.sakurachan.DefaultHostsLoader",
      "plugins" : {
        "shell" : "com.mychaelstyle.sakurachan.plugins.Shell",
        "invoke" : "com.mychaelstyle.sakurachan.plugins.Invoke",
        "upload" : "com.mychaelstyle.sakurachan.plugins.Upload",
        "plant" : "com.mychaelstyle.sakurachan.plugins.Plant"
      }
    }

### e.g. hosts.json

You must write your own hosts.

    {
        "user" : "foo",
        "password" : "var",
        "hosts" : {
            "crawler" : [
                {"name":"192.168.0.2"},
                {"name":"192.168.0.3"},
                {"name":"192.168.0.4"},
                {"name":"192.168.0.5", "user": "me", "password": "var2", "auth-file" : "/me/id_rsa" },
                {"name":"192.168.0.6"}
            ]
        }
    }

## How to use shell command

    $ sakurachan shell
    ...
    sakura-chan > ls -la
    ...
    sakura-chan > exit

## How to upload a file to all hosts at once

    $ sakurachan upload --src=test.txt --dst=/home/hoge/

## How to invoke command onliner

    $ sakurachan invoke --CMD="ls -la"

## How to execute your own scenario commands

    $ sakurachan plant --seed-file=./seed.json

e.g. seed.json

    {
        "actions": [
            /* make directory */
            {"command" : "mkdir .ssh"},
            /** change permission */
            {"command" : "chmod go-rwx .ssh"},
            /** upload ssh files */
            {"upload" : "datas/.ssh/config", "dst" : ".ssh/"},
            {"upload" : "datas/.ssh/authorized_keys", "dst" : ".ssh/"},
            {"upload" : "datas/.ssh/known_hosts", "dst" : ".ssh/"},
            {"upload" : "datas/.ssh/id_rsa", "dst" : ".ssh/"}
        ]
    }


