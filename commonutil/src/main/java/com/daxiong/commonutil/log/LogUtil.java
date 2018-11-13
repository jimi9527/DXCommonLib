package com.daxiong.commonutil.log;

import com.winom.olog.Log;
import com.winom.olog.LogImpl;

/**
 * author: tonydeng
 * mail : tonydeng@hxy.com
 * 2018/11/13
 */
public class LogUtil {

    public void initLog(String filePath, String fileName){
        Log.setLogImpl(new LogImpl(filePath, fileName, ".olog"));
    }
}
