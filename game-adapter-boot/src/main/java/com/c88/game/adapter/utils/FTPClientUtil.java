package com.c88.game.adapter.utils;

import com.c88.game.adapter.pojo.vo.TCGameBetInfoVO;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializer;
import com.google.gson.stream.JsonReader;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPReply;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

/**
 * @description: TODO
 * @author: marcoyang
 * @date: 2023/1/9
 **/
public class FTPClientUtil implements AutoCloseable{
    private FTPClient client;

    public FTPClientUtil(String serverIp,String userName,String pwd)throws IOException {
        client =  new FTPClient();
        client.connect(serverIp,21);
        int retCode = client.getReplyCode();
        if(!FTPReply.isPositiveCompletion(retCode)){
            client.disconnect();
            throw new IOException("Exception in connecting to FTP Server");
        }
        client.login(userName,pwd);
    }

    public InputStream downLoad(String fileName) throws IOException{
        return client.retrieveFileStream(fileName);
    }

    public void disConnect() throws IOException{
        client.disconnect();
    }

    @Override
    public void close() throws IOException {
        client.disconnect();
    }
}
