/**
 * FileInfo.java
 */
package com.michael.corelib.fileutils;

/**
 * @author Guoqing Sun Oct 24, 201211:16:36 AM
 */
public class FileInfo {

    public String fileName;

    public String filePath;

    public long fileSize;

    public boolean isDir;

    public int count;

    public long modifiedDate;

    public boolean selected;

    public boolean canRead;

    public boolean canWrite;

    public boolean isHidden;

    public long dbId; // id in the database, if is from database

    @Override
    public String toString() {
        return "FileInfo [fileName=" + fileName + ", filePath=" + filePath + ", fileSize=" + fileSize + ", IsDir="
                + isDir + ", Count=" + count + ", ModifiedDate=" + modifiedDate + ", Selected=" + selected
                + ", canRead=" + canRead + ", canWrite=" + canWrite + ", isHidden=" + isHidden + ", dbId=" + dbId + "]";
    }
    
}
