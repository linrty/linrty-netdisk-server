package top.linrty.netdisk.transfer.domain.po.operation.read;

import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import top.linrty.netdisk.transfer.domain.po.operation.read.entity.ReadFile;

@Slf4j
@NoArgsConstructor
public abstract class Reader {

    public abstract String read(ReadFile readFile);
}

