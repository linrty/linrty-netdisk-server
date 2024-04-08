package top.linrty.netdisk.common.util;

import top.linrty.netdisk.common.exception.FileOperationException;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class CharsetUtils {
    public CharsetUtils() {
    }

    public static byte[] convertTxtCharsetToGBK(byte[] bytes, String extendName) {
        if (Arrays.asList(FileTypeUtil.TXT_FILE).contains(extendName)) {
            ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes);

            try {
                String str = new String(bytes, getFileCharsetName(byteArrayInputStream));
                return str.getBytes("GBK");
            } catch (IOException e) {
                throw new FileOperationException("文件编码失败", e);
            }
        } else {
            return bytes;
        }
    }

    public static byte[] convertTxtCharsetToUTF8(byte[] bytes, String extendName) {
        if (Arrays.asList(FileTypeUtil.TXT_FILE).contains(extendName)) {
            ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes);

            try {
                String str = new String(bytes, getFileCharsetName(byteArrayInputStream));
                return str.getBytes(StandardCharsets.UTF_8);
            } catch (IOException e) {
                throw new FileOperationException("文件编码失败", e);
            }
        } else {
            return bytes;
        }
    }

    public static String getFileCharsetName(InputStream inputStream) {
        String charset = "GBK";
        byte[] first3Bytes = new byte[3];

        try {
            boolean checked = false;
            BufferedInputStream bis = new BufferedInputStream(inputStream);
            bis.mark(0);
            int read = bis.read(first3Bytes, 0, 3);
            if (read == -1) {
                bis.close();
                return charset;
            }

            if (first3Bytes[0] == -1 && first3Bytes[1] == -2) {
                charset = "UTF-16LE";
                checked = true;
            } else if (first3Bytes[0] == -2 && first3Bytes[1] == -1) {
                charset = "UTF-16BE";
                checked = true;
            } else if (first3Bytes[0] == -17 && first3Bytes[1] == -69 && first3Bytes[2] == -65) {
                charset = "UTF-8";
                checked = true;
            }

            bis.reset();
            if (!checked) {
                label72:
                do {
                    do {
                        if ((read = bis.read()) == -1 || read >= 240 || 128 <= read && read <= 191) {
                            break label72;
                        }

                        if (192 <= read && read <= 223) {
                            read = bis.read();
                            continue label72;
                        }
                    } while(224 > read || read > 239);

                    read = bis.read();
                    if (128 <= read && read <= 191) {
                        read = bis.read();
                        if (128 <= read && read <= 191) {
                            charset = "UTF-8";
                        }
                    }
                    break;
                } while(128 <= read && read <= 191);
            }

            bis.close();
        } catch (Exception var6) {
            var6.printStackTrace();
        }

        return charset;
    }

    public static void main(String[] args) {
        System.out.println(Charset.forName("GB2312").newEncoder().canEncode("ÎÄ¼þ¼ÐÑ¹Ëõ"));
        System.out.println(StandardCharsets.ISO_8859_1.newEncoder().canEncode("ÎÄ¼þ¼ÐÑ¹Ëõ"));
        System.out.println(StandardCharsets.UTF_8.newEncoder().canEncode("ÎÄ¼þ¼ÐÑ¹Ëõ"));
        System.out.println(StandardCharsets.US_ASCII.newEncoder().canEncode("ÎÄ¼þ¼ÐÑ¹Ëõ"));
        byte[] e = "ÎÄ¼þ¼ÐÑ¹Ëõ".getBytes(StandardCharsets.ISO_8859_1);

        try {
            System.out.println(new String("ÎÄ¼þ¼ÐÑ¹Ëõ".getBytes("GBK"), "UTF-8"));
        } catch (UnsupportedEncodingException var3) {
            throw new RuntimeException(var3);
        }

        System.out.println(getFileCharsetName(new ByteArrayInputStream("ÎÄ¼þ¼ÐÑ¹Ëõ".getBytes())));
    }
}