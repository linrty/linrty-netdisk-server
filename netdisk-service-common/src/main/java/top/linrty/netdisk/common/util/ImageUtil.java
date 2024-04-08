package top.linrty.netdisk.common.util;

import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.coobird.thumbnailator.geometry.Positions;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;

import net.coobird.thumbnailator.Thumbnails;

@Slf4j
public class ImageUtil {
    public static void leftTotation(File inFile, File outFile, int angle) throws IOException {
        Thumbnails.of(new File[]{inFile}).scale(1.0).outputQuality(1.0F).rotate((double)(-angle)).toFile(outFile);
    }

    public static void rightTotation(File inFile, File outFile, int angle) throws IOException {
        Thumbnails.of(new File[]{inFile}).scale(1.0).outputQuality(1.0F).rotate((double)angle).toFile(outFile);
    }

    public static void thumbnailsImage(File inFile, File outFile, int width, int height) throws IOException {
        Thumbnails.of(new File[]{inFile}).size(width, height).toFile(outFile);
    }

    public static InputStream thumbnailsImage(InputStream inputStream, File outFile, int width, int height) throws IOException {
        File parentFile = outFile.getParentFile();
        if (!parentFile.exists()) {
            parentFile.mkdirs();
        }

        ByteArrayOutputStream baos = cloneInputStream(inputStream);
        InputStream inputStream1 = new ByteArrayInputStream(baos.toByteArray());
        InputStream inputStream2 = new ByteArrayInputStream(baos.toByteArray());
        BufferedImage bufferedImage = ImageIO.read(inputStream1);
        if (bufferedImage == null) {
            return inputStream2;
        } else {
            int oriHeight = bufferedImage.getHeight();
            int oriWidth = bufferedImage.getWidth();
            if (oriHeight > height && oriWidth > width) {
                if (oriHeight < oriWidth) {
                    Thumbnails.of(new BufferedImage[]{bufferedImage}).outputQuality(1.0F).scale(1.0).sourceRegion(Positions.CENTER, oriHeight, oriHeight).toFile(outFile);
                } else {
                    Thumbnails.of(new BufferedImage[]{bufferedImage}).outputQuality(1.0F).scale(1.0).sourceRegion(Positions.CENTER, oriWidth, oriWidth).toFile(outFile);
                }

                Thumbnails.of(new BufferedImage[]{ImageIO.read(outFile)}).outputQuality(0.9).size(width, height).toFile(outFile);
            } else {
                ImageIO.write(bufferedImage, FilenameUtils.getExtension(outFile.getName()), outFile);
            }

            return new FileInputStream(outFile);
        }
    }

    public static InputStream thumbnailsImageForScale(InputStream inputStream, File outFile, long desFileSize) throws IOException {
        byte[] imageBytes = IOUtils.toByteArray(inputStream);
        if (imageBytes != null && imageBytes.length > 0 && (long)imageBytes.length >= desFileSize * 1024L) {
            long srcSize = (long)imageBytes.length;
            double accuracy = 0.4;

            try {
                while((long)imageBytes.length > desFileSize * 1024L) {
                    ByteArrayInputStream is = new ByteArrayInputStream(imageBytes);
                    ByteArrayOutputStream outputStream = new ByteArrayOutputStream(imageBytes.length);
                    Thumbnails.of(new InputStream[]{is}).scale(accuracy).outputQuality(accuracy).toOutputStream(outputStream);
                    imageBytes = outputStream.toByteArray();
                }
            } catch (Exception var11) {
                log.error("【图片压缩】msg=图片压缩失败!", var11);
            }

            FileUtils.writeByteArrayToFile(outFile, imageBytes);
            return new FileInputStream(outFile);
        } else {
            FileUtils.writeByteArrayToFile(outFile, imageBytes);
            return new FileInputStream(outFile);
        }
    }

    public static String getFileExtendName(String fileName) {
        return fileName.lastIndexOf(".") == -1 ? "" : fileName.substring(fileName.lastIndexOf(".") + 1);
    }

    private static ByteArrayOutputStream cloneInputStream(InputStream input) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];

            int len;
            while((len = input.read(buffer)) > -1) {
                baos.write(buffer, 0, len);
            }

            baos.flush();
            return baos;
        } catch (IOException var4) {
            var4.printStackTrace();
            return null;
        }

    }
}
