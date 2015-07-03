package com.michael.corelib.fileutils;

import android.text.TextUtils;

import java.io.*;
import java.util.LinkedList;

public class FileOperatorHelper {

    public static void DeleteFile(FileInfo f) {
        if (f == null) {
            return;
        }

        File file = new File(f.filePath);
        boolean directory = file.isDirectory();
        if (directory) {
            for (File child : file.listFiles()) {
                if (FileUtil.isNormalFile(child.getAbsolutePath())) {
                    DeleteFile(FileUtil.getFileInfo(child));
                }
            }
        }

        file.delete();
    }

    public static final long getDirectorySize(File dir) {
        long retSize = 0;
        if ((dir == null) || !dir.isDirectory()) {
            return retSize;
        }
        File[] entries = dir.listFiles();
        int count = entries.length;
        for (int i = 0; i < count; i++) {
            if (entries[i].isDirectory()) {
                retSize += getDirectorySize(entries[i]);
            } else {
                retSize += entries[i].length();
            }
        }
        return retSize;
    }

    public static final LinkedList<FileInfo> getFileInfoUnderDir(String dir) {
        if (TextUtils.isEmpty(dir)) {
            return null;
        }

        File file = new File(dir);
        if (file.exists()) {
            return getFileInfoUnderDir(file);
        }

        return null;
    }

    public static final LinkedList<FileInfo> getFileInfoUnderDir(File dir) {
        if (dir == null || !dir.isDirectory()) {
            return null;
        }

        LinkedList<FileInfo> ret = new LinkedList<FileInfo>();
        File[] entries = dir.listFiles();
        int count = entries.length;
        for (int i = 0; i < count; ++i) {
            if (entries[i].isDirectory()) {
                ret.addAll(getFileInfoUnderDir(entries[i]));
            } else {
                ret.add(FileUtil.getFileInfo(entries[i]));
            }
        }

        return ret;
    }

    public static final void createDirectory(String strDir) {
        File file = new File(strDir);
        if (!file.isDirectory()) {
            file.mkdir();
        }
    }

    public static final boolean moveFile(String strOriginal, String strDest) {
        try {
            File fileOriginal = new File(strOriginal);
            File fileDest = new File(strDest);
            fileOriginal.renameTo(fileDest);
        } catch (Exception e) {
            return false;
        }

        return true;
    }

    /**
     * 增量写一个文件
     * 
     * @param targetPath
     * @param is
     * @return
     */
    public static String saveFileByISSupportAppend(String targetPath, InputStream is) {
        byte[] buffer = new byte[4096 * 2];
        File f = new File(targetPath);
        int len;
        OutputStream os = null;

        try {
            os = new FileOutputStream(f, true);
            while ((len = is.read(buffer)) != -1) {
                os.write(buffer, 0, len);
            }
            return targetPath;
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        } finally {
            if (os != null) {
                try {
                    os.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            buffer = null;
        }
    }

    public static String saveFileByBytes(String targetPath, byte[] bytes) {
        if (TextUtils.isEmpty(targetPath)) {
            return null;
        }

        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(targetPath);
            fos.write(bytes);
            fos.flush();
            return targetPath;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (fos != null) {
                    fos.close();
                    fos = null;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return null;
    }

    public static void copyFile(FileInfo f, String dest) {
        if (f == null || dest == null) {
            return;
        }

        File file = new File(f.filePath);
        if (file.isDirectory()) {
            // directory exists in destination, rename it
            String destPath = FileUtil.makePath(dest, f.fileName);
            File destFile = new File(destPath);
            int i = 1;
            while (destFile.exists()) {
                destPath = FileUtil.makePath(dest, f.fileName + " " + i++);
                destFile = new File(destPath);
            }

            for (File child : file.listFiles()) {
                if (!child.isHidden() && FileUtil.isNormalFile(child.getAbsolutePath())) {
                    copyFile(FileUtil.getFileInfo(child), destPath);
                }
            }
        } else {
            copyFile(f.filePath, dest);
        }
    }

    
    /**
     * 将src所指向的文件copy到targetFullPath所指向的文件，只支持文件copy，不支持文件夹copy
     * 
     * @param src
     * @param targetFullPath
     * @return
     */
    public static String copyFile(String src, String targetFullPath) {
        File file = new File(src);
        if (!file.exists() || file.isDirectory()) {
            return null;
        }
        FileInputStream fi = null;
        FileOutputStream fo = null;
        try {
            fi = new FileInputStream(file);
            File targetDir = new File(targetFullPath).getParentFile();
            if (targetDir == null) {
            	return null;
            }
            if (!targetDir.exists()) {
                if (!targetDir.mkdirs())
                    return null;
            }

            File targetFile = new File(targetFullPath);
            if (targetFile.exists()) {
            	targetFile.delete();
            }

            if (!targetFile.createNewFile()) {
                return null;
            }

            fo = new FileOutputStream(targetFile);
            int count = 102400;
            byte[] buffer = new byte[count];
            int read = 0;
            while ((read = fi.read(buffer, 0, count)) != -1) {
                fo.write(buffer, 0, read);
            }

            // TODO: set access privilege

            return targetFullPath;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (fi != null)
                    fi.close();
                if (fo != null)
                    fo.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return null;
    }

    public static boolean copyFile(BufferedReader reader, String destPath) {
        boolean ret = false;
        if (reader == null) {
            return false;
        }
        File destFile = new File(destPath);
        if (destFile.exists()) {
            destFile.delete();
        }
        BufferedWriter writer = null;
        try {
            writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(destFile)));
            String read = "";
            while ((read = reader.readLine()) != null) {
                writer.write(read);
                writer.newLine();
                writer.flush();
            }
            ret = true;
        } catch (FileNotFoundException e1) {
            e1.printStackTrace();
        } catch (IOException e2) {
            e2.printStackTrace();
        } finally {
            try {
                if (writer != null) {
                    writer.close();
                }
                if (reader != null) {
                    reader.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return ret;
    }

}
