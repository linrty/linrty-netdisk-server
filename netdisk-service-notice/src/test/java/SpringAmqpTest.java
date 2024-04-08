
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import top.linrty.netdisk.common.util.RabbitMqHelper;
import top.linrty.netdisk.notice.NoticeApplication;

import javax.annotation.Resource;

@SpringBootTest(classes = NoticeApplication.class)
public class SpringAmqpTest {

    @Resource
    private RabbitMqHelper rabbitMqHelper;

    @Test
    public void testSimpleQueue(){
        String queueName = "test.queue";

        String message = "Hello, RabbitMQ!";

        rabbitMqHelper.sendMessage("test.exchange", "test", message);
    }
}
