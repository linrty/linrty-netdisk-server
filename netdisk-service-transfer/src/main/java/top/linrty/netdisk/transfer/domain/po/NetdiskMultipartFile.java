package top.linrty.netdisk.transfer.domain.po;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.io.FilenameUtils;
import org.springframework.web.multipart.MultipartFile;
import top.linrty.netdisk.common.util.FileTypeUtil;

import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NetdiskMultipartFile {
    MultipartFile multipartFile = null;

    public String getFileName() {
        String originalName = this.getMultipartFile().getOriginalFilename();
        return !originalName.contains(".") ? originalName : originalName.substring(0, originalName.lastIndexOf("."));
    }
    public String getExtendName() {
        String originalName = this.getMultipartFile().getOriginalFilename();
        return FilenameUtils.getExtension(originalName);
    }

    public String getFileUrl() {
        String uuid = UUID.randomUUID().toString();
        return FileTypeUtil.getUploadFileUrl(uuid, this.getExtendName());
    }

    public String getFileUrl(String identify) {
        return FileTypeUtil.getUploadFileUrl(identify, this.getExtendName());
    }


    public InputStream getUploadInputStream() throws IOException {
        return this.getMultipartFile().getInputStream();
    }

    public byte[] getUploadBytes() throws IOException {
        return this.getMultipartFile().getBytes();
    }

    public long getSize() {
        return this.getMultipartFile().getSize();
    }

    public MultipartFile getMultipartFile() {
        return this.multipartFile;
    }
}
