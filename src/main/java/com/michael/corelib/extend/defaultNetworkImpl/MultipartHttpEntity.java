/**
 * Copyright 2011-2012 Renren Inc. All rights reserved.
 * － Powered by Team Pegasus. －
 */

package com.michael.corelib.extend.defaultNetworkImpl;

import android.os.Bundle;
import com.michael.corelib.config.CoreConfig;
import com.michael.corelib.internet.NetworkLog;
import com.michael.corelib.internet.core.RequestEntity;
import org.apache.http.entity.AbstractHttpEntity;
import org.apache.http.protocol.HTTP;

import java.io.*;
import java.util.ArrayList;
import java.util.Set;

public class MultipartHttpEntity extends AbstractHttpEntity {

    private static final String BOUNDARY = "-----------------------------114975832116442893661388290519";// separate line
    private byte[] beginData;
    private byte[] endData;
    private Bundle params = null;
    private long len;
    //	private File file;
//	private byte[] data;
//	private String contentType;
    private ArrayList<RequestEntity.MultipartFileItem> fileItems = null;

    private int fileItemLength;
    //	private byte[][] fileDescription;
    private byte[][] fileItemDatas;
    private File[] files;
    private byte[][] datas;
//	private String[] contentTypes;

    private RequestEntity mRequestEntity;

    public MultipartHttpEntity(RequestEntity requestEntity) {
        if (requestEntity == null) {
            throw new RuntimeException("Request entity MUST NOT be NULL");
        }
        mRequestEntity = requestEntity;
        params = requestEntity.getBasicParams();
        fileItems = requestEntity.getFileItems();
        if (params == null || fileItems == null) {
            return;
        }

        beginData = getBeginData();
        endData = getEndData();
        len = beginData.length + endData.length;
        fileItemLength = fileItems.size();
        fileItemDatas = new byte[fileItemLength][];
        files = new File[fileItemLength];
        datas = new byte[fileItemLength][];
        int index = 0;
        for (RequestEntity.MultipartFileItem fileItem : fileItems) {
            fileItemDatas[index] = getFileItemBeginData(fileItem);
            files[index] = fileItem.getFile();
            datas[index] = fileItem.getData();
            len += fileItemDatas[index].length;
            index++;
        }
        setContentType("multipart/form-data; charset=UTF-8; boundary=" + BOUNDARY);

    }

    public int getRequestSize() {
        int size = 0;
        if (params != null) {
            for (String key : params.keySet()) {
                size += key.getBytes().length;
                size += params.getString(key).getBytes().length;
            }
        }

        size += beginData.length;
        size += endData.length;

        if (files != null) {
            for (File f : files) {
                size += f.length();
            }
        }

        return size;
    }

    private byte[] getBeginData() {
        StringBuilder sb = new StringBuilder();
        Set<String> keySet = params.keySet();
        for (String key : keySet) {
            String value = params.getString(key);
            sb.append("--");
            sb.append(BOUNDARY);
            sb.append("\r\n");
            sb.append("Content-Disposition: form-data; name=\"" + key + "\"\r\n\r\n");
            sb.append(value);
            sb.append("\r\n");

        }
        String s = sb.toString();
        try {
            return s.getBytes(HTTP.UTF_8);
        } catch (UnsupportedEncodingException e) {
            return s.getBytes();
        }
    }

    private byte[] getFileItemBeginData(RequestEntity.MultipartFileItem fileItem) {
        long length = fileItem.getFile() != null ? fileItem.getFile().length() :
                (fileItem.getData() != null ? fileItem.getData().length : 0);
        len += length;
        StringBuilder sb = new StringBuilder();
        sb.append("--");
        sb.append(BOUNDARY);
        sb.append("\r\n");
        sb.append("Content-Disposition: form-data; name=\"" + fileItem.getName() + "\"; filename=\"" + fileItem.getFileName() + "\"\r\n");
        sb.append("Content-Type: " + fileItem.getContentType() + "\r\n\r\n");
        String s = sb.toString();
        try {
            return s.getBytes(HTTP.UTF_8);
        } catch (UnsupportedEncodingException e) {
            return s.getBytes();
        }
    }

    private byte[] getEndData() {
        String s = "\r\n--" + BOUNDARY + "--\r\n";
        try {
            return s.getBytes(HTTP.UTF_8);
        } catch (UnsupportedEncodingException e) {
            return s.getBytes();
        }
    }

    @Override
    public void writeTo(OutputStream outstream) throws IOException {
        if (CoreConfig.DEBUG) {
            NetworkLog.LOGD("<<<<<<<<<<<<<<< Begin write Multi Part body >>>>>>>>>>>>>>>");
        }

        FileInputStream fis = null;
        try {
            outstream.write(beginData);
            if (CoreConfig.DEBUG) {
                String beginStr = new String(beginData);
                NetworkLog.LOGD(" After write the Begin Data : \n " + beginStr + " \n\n");
            }
            int index = 0;
            for (byte[] fileItemData : fileItemDatas) {
                outstream.write(fileItemData);
                if (CoreConfig.DEBUG) {
                    String fileItemStr = new String(fileItemData);
                    NetworkLog.LOGD(" After write the file item data : \n" + fileItemStr + "\n\n");
                }

                File file = null;
                if (files != null) {
                    file = files[index];
                }
                if (file != null) {
//                    if (NetworkLog.DEBUG) {
//                        String fileItemStr = new String(fileItemData);
//                        NetworkLog.LOGD("    ((((( begin write the file : " + file.getAbsolutePath() + " ))))))");
//                    }
                    fis = new FileInputStream(file);
                    byte[] buf = new byte[4096 * 2];
                    int len = 0;
                    //debug log
                    long sendLength = 0;
                    long fileLength = file.length();

                    while ((len = fis.read(buf)) != -1) {
//                        if (NetworkLog.DEBUG) {
//                            NetworkLog.LOGD("     >>>>>> Before write part data of the file : " + file.getName()
//                                    + ", this round write data length = " + len + " >>>>>");
//                        }
                        outstream.write(buf, 0, len);
//                        if (NetworkLog.DEBUG) {
//                            sendLength += len;
//
//                            NetworkLog.LOGD("     >>>>>> After write part data of the file : " + file.getName()
//                                    + ", has write = " + sendLength
//                                    + " total file size :" + fileLength + " >>>>>");
//                        }
                    }
                } else {
                    if (datas != null) {
                        if (datas[index] != null) {
                            outstream.write(datas[index]);
                        }
                    }
                }
                index++;
            }
            outstream.write(endData);

            if (CoreConfig.DEBUG) {
                String endStr = new String(endData);
                NetworkLog.LOGD("     After write the end data : " + endStr);
                NetworkLog.LOGD("<<<<<<<<<<<<<<< end write Multi Part body >>>>>>>>>>>>>>>");
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (fis != null) {
                fis.close();
            }
        }
    }

    @Override
    public InputStream getContent() throws IOException, IllegalStateException {
        return null;
    }

    @Override
    public long getContentLength() {
        return len;
    }

    @Override
    public boolean isRepeatable() {
        return true;
    }

    @Override
    public boolean isStreaming() {
        return false;
    }


    private String parseContentType(String fileName) {
        String contentType = "image/jpg";
        fileName = fileName.toLowerCase();
        if (fileName.endsWith(".jpg")) {
            contentType = "image/jpg";
        } else if (fileName.endsWith(".png")) {
            contentType = "image/png";
        } else if (fileName.endsWith(".jpeg")) {
            contentType = "image/jpeg";
        } else if (fileName.endsWith(".gif")) {
            contentType = "image/gif";
        } else if (fileName.endsWith(".bmp")) {
            contentType = "image/bmp";
        } else {
            throw new RuntimeException("不支持的文件类型'" + fileName + "'(或没有文件扩展名)");
        }
        return contentType;
    }

    public RequestEntity getRequestEntity() {
        return mRequestEntity;
    }

    public void setRequestEntity(RequestEntity mRequestEntity) {
        this.mRequestEntity = mRequestEntity;
    }
}
