package top.linrty.netdisk.transfer.domain.po.operation.copy;

import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import top.linrty.netdisk.transfer.domain.po.operation.copy.entity.CopyFile;

import java.io.InputStream;

@NoArgsConstructor
@Slf4j
public abstract class Copier {

    public abstract String copy(InputStream inputStream, CopyFile copyFile);
}

