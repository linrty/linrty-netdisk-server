package top.linrty.netdisk.transfer.domain.po.operation.write;

import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import top.linrty.netdisk.transfer.domain.po.operation.write.entity.WriteFile;

import java.io.InputStream;

@Slf4j
@NoArgsConstructor
public abstract class Writer {

    public abstract void write(InputStream inputStream, WriteFile writeFile);
}
