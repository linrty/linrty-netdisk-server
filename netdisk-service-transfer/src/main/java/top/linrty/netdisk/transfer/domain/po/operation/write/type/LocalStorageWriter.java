package top.linrty.netdisk.transfer.domain.po.operation.write.type;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import top.linrty.netdisk.common.exception.FileOperationException;
import top.linrty.netdisk.common.util.FileTypeUtil;
import top.linrty.netdisk.transfer.domain.po.operation.write.Writer;
import top.linrty.netdisk.transfer.domain.po.operation.write.entity.WriteFile;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

@Slf4j
@Component
@NoArgsConstructor
@Builder
@Getter
@Setter
public class LocalStorageWriter extends Writer {

    public void write(InputStream inputStream, WriteFile writeFile) {
        try {
            FileOutputStream out = new FileOutputStream(FileTypeUtil.getStaticPath() + writeFile.getFileUrl());
            Throwable var4 = null;

            try {
                byte[] bytes = new byte[1024];

                int read;
                while((read = inputStream.read(bytes)) != -1) {
                    out.write(bytes, 0, read);
                }

                out.flush();
            } catch (Throwable var16) {
                var4 = var16;
                throw var16;
            } finally {
                if (out != null) {
                    if (var4 != null) {
                        try {
                            out.close();
                        } catch (Throwable var15) {
                            var4.addSuppressed(var15);
                        }
                    } else {
                        out.close();
                    }
                }

            }
        } catch (FileNotFoundException e) {
            throw new FileOperationException("待写入的文件不存在", e);
        } catch (IOException e) {
            throw new FileOperationException("IO异常", e);
        }
    }
}
