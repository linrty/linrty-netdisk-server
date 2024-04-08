import cn.hutool.core.bean.BeanUtil;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import top.linrty.netdisk.file.FileApplication;
import top.linrty.netdisk.file.domain.po.UserFile;

import java.util.Map;

@SpringBootTest(classes = FileApplication.class)
public class TestBean {
    @Test
    public void test() {
        UserFile userFile = new UserFile();
        userFile.setIsDir(1);
        Map<String, Object> userFileMap = BeanUtil.beanToMap(userFile);
        if(userFileMap.get("isDir").equals(1)){
            System.out.println("isDir is 1");
        }else{
            System.out.println("isDir is not 1");
        }

    }
}
