package top.linrty.netdisk.common.util;

import cn.hutool.core.util.StrUtil;
import org.springframework.util.ResourceUtils;
import top.linrty.netdisk.common.exception.FileOperationException;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.URLDecoder;
import java.text.SimpleDateFormat;
import java.util.Date;

public class FileTypeUtil {
    public static String LOCAL_STORAGE_PATH;
    // 上传文件的父目录
    public static String ROOT_PATH = "upload";
    public static final String[] IMG_FILE = new String[]{"bmp", "jpg", "png", "tif", "gif", "jpeg"};
    public static final String[] DOC_FILE = new String[]{"doc", "docx", "ppt", "pptx", "xls", "xlsx", "txt", "hlp", "wps", "rtf", "html", "pdf"};
    public static final String[] VIDEO_FILE = new String[]{"avi", "mp4", "mpg", "mov", "swf"};
    public static final String[] MUSIC_FILE = new String[]{"wav", "aif", "au", "mp3", "ram", "wma", "mmf", "amr", "aac", "flac"};
    public static final String[] TXT_FILE = new String[]{"txt", "html", "java", "xml", "js", "css", "json", "sql"};
    public static final int IMAGE_TYPE = 1;
    public static final int DOC_TYPE = 2;
    public static final int VIDEO_TYPE = 3;
    public static final int MUSIC_TYPE = 4;
    public static final int OTHER_TYPE = 5;
    public static final int SHARE_FILE = 6;
    public static final int RECYCLE_FILE = 7;

    public FileTypeUtil() {
    }

    public static boolean isImageFile(String extendName) {
        String[] var1 = IMG_FILE;
        int var2 = var1.length;

        for(int var3 = 0; var3 < var2; ++var3) {
            String extend = var1[var3];
            if (extendName.equalsIgnoreCase(extend)) {
                return true;
            }
        }

        return false;
    }


    public static boolean isMusicFile(String extendName) {
        String[] var1 = MUSIC_FILE;
        int var2 = var1.length;

        for(int var3 = 0; var3 < var2; ++var3) {
            String extend = var1[var3];
            if (extendName.equalsIgnoreCase(extend)) {
                return true;
            }
        }

        return false;
    }

    public static boolean isVideoFile(String extendName) {
        String[] var1 = VIDEO_FILE;
        int var2 = var1.length;

        for(int var3 = 0; var3 < var2; ++var3) {
            String extend = var1[var3];
            if (extendName.equalsIgnoreCase(extend)) {
                return true;
            }
        }

        return false;
    }

    public static String pathSplitFormat(String filePath) {
        return filePath.replace("///", "/").replace("//", "/").replace("\\\\\\", "/").replace("\\\\", "/");
    }

    public static File getLocalSaveFile(String fileUrl) {
        String localSavePath = getStaticPath() + fileUrl;
        return new File(localSavePath);
    }

    public static File getCacheFile(String fileUrl) {
        String cachePath = getStaticPath() + "cache" + File.separator + fileUrl;
        return new File(cachePath);
    }

    public static File getTempFile(String fileUrl) {
        String tempPath = getStaticPath() + "temp" + File.separator + fileUrl;
        File tempFile = new File(tempPath);
        File parentFile = tempFile.getParentFile();
        if (!parentFile.exists()) {
            parentFile.mkdirs();
        }

        return tempFile;
    }

    public static File getProcessFile(String fileUrl) {
        String processPath = getStaticPath() + "temp" + File.separator + "process" + File.separator + fileUrl;
        File processFile = new File(processPath);
        File parentFile = processFile.getParentFile();
        if (!parentFile.exists()) {
            parentFile.mkdirs();
        }

        return processFile;
    }

    public static String getProjectRootPath() {
        try {
            String url = ResourceUtils.getURL("classpath:").getPath();
            String absolutePath = urlDecode((new File(url)).getAbsolutePath()) + File.separator;
            return absolutePath;
        } catch (FileNotFoundException e) {
            throw new FileOperationException("获取项目根路径失败", e);
        }
    }

    public static String urlDecode(String url) {
        try {
            String decodeUrl = URLDecoder.decode(url, "utf-8");
            return decodeUrl;
        } catch (UnsupportedEncodingException e) {
            throw new FileOperationException("不支持的编码格式", e);
        }
    }

    public static String getStaticPath() {
        String localStoragePath = LOCAL_STORAGE_PATH;
        if (StrUtil.isNotEmpty(localStoragePath)) {
            return (new File(localStoragePath)).getPath() + File.separator;
        } else {
            String projectRootAbsolutePath = getProjectRootPath();
            int index = projectRootAbsolutePath.indexOf("file:");
            if (index != -1) {
                projectRootAbsolutePath = projectRootAbsolutePath.substring(0, index);
            }

            return (new File(projectRootAbsolutePath + "static")).getPath() + File.separator;
        }
    }

    public static String getUploadFileUrl(String identifier, String extendName) {
        SimpleDateFormat formater = new SimpleDateFormat("yyyyMMdd");
        String path = ROOT_PATH + "/" + formater.format(new Date()) + "/";
        File dir = new File(getStaticPath() + path);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        path = path + identifier + "." + extendName;
        return path;
    }

    public static String getAliyunObjectNameByFileUrl(String fileUrl) {
        if (fileUrl.startsWith("/") || fileUrl.startsWith("\\")) {
            fileUrl = fileUrl.substring(1);
        }

        return fileUrl;
    }

    public static String formatPath(String path) {
        path = pathSplitFormat(path);
        if ("/".equals(path)) {
            return path;
        } else if (path.endsWith("/")) {
            int length = path.length();
            return path.substring(0, length - 1);
        } else {
            return path;
        }
    }
}
