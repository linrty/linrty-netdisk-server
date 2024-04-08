package top.linrty.netdisk.common.util;

import cn.hutool.core.util.StrUtil;
import net.sf.sevenzipjbinding.ArchiveFormat;
import net.sf.sevenzipjbinding.IInArchive;
import net.sf.sevenzipjbinding.SevenZip;
import net.sf.sevenzipjbinding.impl.RandomAccessFileInStream;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.*;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class FileOperation {
    private static final Logger log = LoggerFactory.getLogger(FileOperation.class);
    private static Executor executor = Executors.newFixedThreadPool(20);

    public FileOperation() {
    }

    public static File newFile(String fileUrl) {
        File file = new File(fileUrl);
        return file;
    }

    public static boolean deleteFile(File file) {
        if (file == null) {
            return false;
        } else if (!file.exists()) {
            return false;
        } else if (file.isFile()) {
            return file.delete();
        } else {
            File[] var1 = file.listFiles();
            int var2 = var1.length;

            for(int var3 = 0; var3 < var2; ++var3) {
                File newfile = var1[var3];
                deleteFile(newfile);
            }

            return file.delete();
        }
    }

    public static boolean deleteFile(String fileUrl) {
        File file = newFile(fileUrl);
        return deleteFile(file);
    }

    public static long getFileSize(String fileUrl) {
        File file = newFile(fileUrl);
        return file.exists() ? file.length() : 0L;
    }

    public static long getFileSize(File file) {
        return file == null ? 0L : file.length();
    }

    public static boolean mkdir(File file) {
        if (file == null) {
            return false;
        } else {
            return file.exists() ? true : file.mkdirs();
        }
    }

    public static boolean mkdir(String fileUrl) {
        if (fileUrl == null) {
            return false;
        } else {
            File file = newFile(fileUrl);
            return file.exists() ? true : file.mkdirs();
        }
    }

    public static void copyFile(FileInputStream fileInputStream, FileOutputStream fileOutputStream) throws IOException {
        try {
            byte[] buf = new byte[4096];

            for(int len = fileInputStream.read(buf); len != -1; len = fileInputStream.read(buf)) {
                fileOutputStream.write(buf, 0, len);
            }
        } finally {
            if (fileInputStream != null) {
                try {
                    fileInputStream.close();
                } catch (IOException var12) {
                    var12.printStackTrace();
                }
            }

            if (fileOutputStream != null) {
                try {
                    fileOutputStream.close();
                } catch (IOException var11) {
                    var11.printStackTrace();
                }
            }

        }

    }

    public static void copyFile(File src, File dest) throws IOException {
        FileInputStream in = new FileInputStream(src);
        FileOutputStream out = new FileOutputStream(dest);
        copyFile(in, out);
    }

    public static void copyFile(String srcUrl, String destUrl) throws IOException {
        if (srcUrl != null && destUrl != null) {
            File srcFile = newFile(srcUrl);
            File descFile = newFile(destUrl);
            copyFile(srcFile, descFile);
        }
    }

    public static List<String> unzip(File sourceFile, String destDirPath) throws Exception {

        System.out.println();
        RandomAccessFile randomAccessFile = new RandomAccessFile(sourceFile, "r");
        IInArchive archive = SevenZip.openInArchive((ArchiveFormat)null, new RandomAccessFileInStream(randomAccessFile));
        int[] in = new int[archive.getNumberOfItems()];

        for(int i = 0; i < in.length; in[i] = i++) {
        }

        archive.extract(in, false, new ExtractCallback(archive, destDirPath));
        File destFile = new File(destDirPath);
        Collection<File> files = FileUtils.listFiles(destFile, new IOFileFilter() {
            public boolean accept(File file) {
                return true;
            }

            public boolean accept(File file, String s) {
                return true;
            }
        }, new IOFileFilter() {
            public boolean accept(File file) {
                return true;
            }

            public boolean accept(File file, String s) {
                return true;
            }
        });
        Set<String> set = new HashSet();
        files.forEach((o) -> {
            String path = o.getAbsolutePath().replace(destFile.getAbsolutePath(), "").replace("\\", "/");
            if (StrUtil.isNotEmpty(path)) {
                set.add(path);
            }

        });
        List<String> res = new ArrayList(set);
        return res;
    }

    public static void saveDataToFile(String filePath, String fileName, String data) {
        BufferedWriter writer = null;
        new File(filePath);
        if (!filePath.endsWith(File.separator)) {
            filePath = filePath + File.separator;
        }

        File file = new File(filePath + fileName);
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException var18) {
                var18.printStackTrace();
            }
        }

        try {
            writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file, false), "UTF-8"));
            writer.write(data);
        } catch (IOException var16) {
            var16.printStackTrace();
        } finally {
            try {
                if (writer != null) {
                    writer.close();
                }
            } catch (IOException var15) {
                var15.printStackTrace();
            }

        }

        System.out.println("文件写入成功！");
    }
}
