package com.bodtec.module.dahuaplayer.utils;

import android.content.Context;
import android.os.Environment;
import android.os.StatFs;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class FileStorageUtil {
    /**
     * 创建文件路径
     *
     */
    public static boolean createFilePath(File file, String filePath) {
        int index = filePath.indexOf("/");
        if (index == -1) {
            return false;
        }
        if (index == 0) {
            filePath = filePath.substring(index + 1);
            index = filePath.indexOf("/");
        }
        String path = filePath.substring(0, index);
        File fPath;
        if (file == null) {
            fPath = new File(path);
        } else {
            fPath = new File(file.getPath() + "/" + path);
        }
        if (!fPath.exists()) {
            if (!fPath.mkdir()) // 没有则创建
            {
                return false;
            }
        }
        if (index < (filePath.length() - 1)) {
            String exPath = filePath.substring(index + 1);
            createFilePath(fPath, exPath);
        }
        return true;

    }

    /**
     * 用于缓存设置封面图时的抓图路径，htc手机不支持中文，因此去除摄像头名称
     */
    public static String getCaptureCopyPath(String projectName) {
        String path;
        String picType;
        Date now = new Date();
        SimpleDateFormat tf = new SimpleDateFormat("yyyyMMddHHmmss");
        picType = "image";
        path = getCaptureAndVideoFolder(projectName) + tf.format(now) + "_" + picType + "_"
                + ".jpg";
        createFilePath(null, path);
        return path;
    }

    /**
     * 项目核心文件外部存储路径
     *
     * @param projectName 项目名称
     * @param fileName    文件名称,例如：xxx.jpg
     */
    public static String getCommonPath(String projectName, String fileName) {
        String path = null;
        String commonPath = "core";
        path = getCaptureAndVideoFolder(projectName) + commonPath + File.separator
                + fileName;
        createFilePath(null, path);
        return path;
    }

    /**
     * 检查SD卡剩余容量
     *
     */
    public static boolean checkSDCard() {
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            File path = Environment.getExternalStorageDirectory();
            StatFs statFs = new StatFs(path.getPath());
            long blockSize = statFs.getBlockSize();
            long availableBlocks = statFs.getAvailableBlocks();
            long ext = availableBlocks * blockSize;
            if (ext > 30 * 1024 * 1024) // 大于30M
            {
                return true;
            } else {
                return false;
            }
        } else
            return false;

    }

    /**
     * 获取文件存储路径
     *
     */
    public static String getCaptureAndVideoFolder(String projectName, String userName) {
        String sdPath = Environment.getExternalStorageDirectory().getPath();
        return sdPath + File.separator + projectName + File.separator
                + "record" + File.separator + userName + File.separator;
    }

    /**
     * 获取文件存储路径
     *
     */
    public static String getCaptureAndVideoFolder(String projectName) {
        String sdPath = Environment.getExternalStorageDirectory().getPath();
        return sdPath + File.separator + projectName + File.separator;
    }

    private final static String DCIM = Environment.getExternalStoragePublicDirectory(
            Environment.DIRECTORY_DCIM).getPath();
    private final static String IMAGE_PATH = DCIM + "/Pictures/";
    private final static String PHOTO_END = ".jpg";
    private final static String VIDEO_PATH = DCIM + "/Records/";
    private final static String VIDEO_END = ".dav";

    /**
     * 截图保存路径
     *
     */
    public static String createSnapPath() {
        String path = IMAGE_PATH + new SimpleDateFormat("yyyyMMddHHmmss").format(new Date()) + PHOTO_END;
        createFilePath(null, path);
        return path;
    }

    /**
     * 录像保存路径
     *
     * @return [video_path,thumb_path]
     */
    public static String[] createRecordPath() {
        String date = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
        String[] paths = new String[2];
        paths[0] = VIDEO_PATH + date + VIDEO_END;
        paths[1] = VIDEO_PATH + date + PHOTO_END;
        createFilePath(null, paths[0]);
        createFilePath(null, paths[1]);
        return paths;
    }

    public static String createSnapPath(Context context) {
        String path = context.getFilesDir() + "/Pictures/" +
                new SimpleDateFormat("yyyyMMddHHmmss", Locale.getDefault()).format(new Date()) + PHOTO_END;
        createFilePath(null, path);
        return path;
    }

    /**
     * 录像保存路径
     *
     * @return [video_path, thumb_path]
     */
    public static String[] createRecordPath(Context context) {
        String date = new SimpleDateFormat("yyyyMMddHHmmss", Locale.getDefault()).format(new Date());
        String[] paths = new String[2];
        String path = context.getFilesDir() + "/Records/";
        paths[0] = path + date + VIDEO_END;
        paths[1] = path + date + PHOTO_END;
        createFilePath(null, paths[0]);
        createFilePath(null, paths[1]);
        return paths;
    }
}
