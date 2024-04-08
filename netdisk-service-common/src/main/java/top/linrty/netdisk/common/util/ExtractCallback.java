package top.linrty.netdisk.common.util;

import net.sf.sevenzipjbinding.*;

import java.io.*;

public class ExtractCallback implements IArchiveExtractCallback {
    private int index;
    private IInArchive inArchive;
    private String ourDir;

    public ExtractCallback(IInArchive inArchive, String ourDir) {
        this.inArchive = inArchive;
        this.ourDir = ourDir;
    }

    public void setCompleted(long arg0) throws SevenZipException {
    }

    public void setTotal(long arg0) throws SevenZipException {
    }

    public ISequentialOutStream getStream(int index, ExtractAskMode extractAskMode) throws SevenZipException {
        this.index = index;
        String path = (String)this.inArchive.getProperty(index, PropID.PATH);
        boolean isFolder = (Boolean)this.inArchive.getProperty(index, PropID.IS_FOLDER);
        return (data) -> {
            try {
                if (!isFolder) {
                    File file = new File(this.ourDir + File.separator + path);
                    save2File(file, data);
                }
            } catch (Exception var5) {
                var5.printStackTrace();
            }

            return data.length;
        };
    }

    public void prepareOperation(ExtractAskMode arg0) throws SevenZipException {
    }

    public void setOperationResult(ExtractOperationResult extractOperationResult) throws SevenZipException {
    }

    public static boolean save2File(File file, byte[] msg) {
        OutputStream fos = null;

        boolean var4;
        try {
            File parent = file.getParentFile();
            if (!parent.exists() && !parent.mkdirs()) {
                var4 = false;
                return var4;
            }

            fos = new FileOutputStream(file, true);
            fos.write(msg);
            fos.flush();
            var4 = true;
        } catch (FileNotFoundException var17) {
            var17.printStackTrace();
            var4 = false;
            return var4;
        } catch (IOException var18) {
            var18.printStackTrace();
            var4 = false;
            return var4;
        } finally {
            try {
                fos.close();
            } catch (IOException var16) {
                var16.printStackTrace();
            }

        }

        return var4;
    }
}