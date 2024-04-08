package top.linrty.netdisk.file.listener;

import com.alibaba.fastjson2.JSON;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.rabbit.annotation.*;
import org.springframework.stereotype.Component;
import top.linrty.netdisk.common.constant.MQConstants;
import top.linrty.netdisk.common.domain.po.NetdiskFile;
import top.linrty.netdisk.file.service.IFileService;
import top.linrty.netdisk.file.service.IRecoveryFileService;
import top.linrty.netdisk.file.service.IUserFileService;
import top.linrty.netdisk.file.service.impl.FileDealService;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class FileDealListener {

    private final FileDealService fileDealService;

    private final IFileService fileService;

    private final IUserFileService userFileService;

    private final IRecoveryFileService recoveryFileService;

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(name = MQConstants.QUEUE_FILE_DEAL_RESTORE_PARENT, durable = "true"),
            exchange = @Exchange(name = MQConstants.EXCHANGE_FILE_DEAL, type = ExchangeTypes.DIRECT),
            key = {MQConstants.ROUTING_KEY_RESTORE_PARENT}
    ))
    public void listenRestoreParent(Map<String, String> map){
        // NetdiskFile netdiskFile = (NetdiskFile) JSON.parseObject(map.get("netdiskFile"), NetdiskFile.class);
        NetdiskFile netdiskFile = new NetdiskFile(map.get("path"), "1".equals(Integer.parseInt(map.get("isDir"))));
        String userId = (String) map.get("userId");
        fileDealService.restoreParentFilePath(netdiskFile, userId);
    }

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(name = MQConstants.QUEUE_FILE_DEAL_UPLOAD_ES_BY_USER_FILE_ID, durable = "true"),
            exchange = @Exchange(name = MQConstants.EXCHANGE_FILE_DEAL, type = ExchangeTypes.DIRECT),
            key = {MQConstants.ROUTING_KEY_ADD_USER_FILE_TO_ES}
    ))
    public void listenAddUserFileToES(String userFileId){
        fileDealService.uploadESByUserFileId(userFileId);
    }

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(name = MQConstants.QUEUE_FILE_DEAL_DELETE_ES_BY_USER_FILE_ID, durable = "true"),
            exchange = @Exchange(name = MQConstants.EXCHANGE_FILE_DEAL, type = ExchangeTypes.DIRECT),
            key = {MQConstants.ROUTING_KEY_DELETE_USER_FILE_FROM_ES}
    ))
    public void listenDeleteUserFileFromES(String userFileId){
        fileDealService.deleteESByUserFileId(userFileId);
    }


    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(name = MQConstants.QUEUE_USER_FILE_UPDATE_FILE_DELETE_STATE_BY_FILE_PATH, durable = "true"),
            exchange = @Exchange(name = MQConstants.EXCHANGE_USER_FILE, type = ExchangeTypes.DIRECT),
            key = {MQConstants.ROUTING_KEY_UPDATE_FILE_DELETE_STATE}
    ))
    public void listenUpdateFileDeleteStateByFilePath(Map<String, String> map){
        String filePath = (String) map.get("filePath");
        String deleteBatchNum = (String) map.get("deleteBatchNum");
        String userId = (String) map.get("userId");
        userFileService.updateFileDeleteStateByFilePath(filePath, deleteBatchNum, userId);
    }

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(name = MQConstants.QUEUE_RECOVERY_FILE_DELETE_FILE, durable = "true"),
            exchange = @Exchange(name = MQConstants.EXCHANGE_RECOVERY_FILE, type = ExchangeTypes.DIRECT),
            key = {MQConstants.ROUTING_KEY_DELETE_FILE}
    ))
    public void listenDeleteRecoveryFile(String userFileId){
        recoveryFileService.deleteRecoveryFileTask(userFileId);
    }


    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(name = MQConstants.QUEUE_FILE_DEAL_SAVE_UNZIP_FILE, durable = "true"),
            exchange = @Exchange(name = MQConstants.EXCHANGE_FILE_DEAL, type = ExchangeTypes.DIRECT),
            key = {MQConstants.ROUTING_KEY_SAVE_UNZIP_FILE}
    ))
    public void listenSaveUnzipFile(Map<String, String> map){
        String unzipDirTempUrl = map.get("unzipDirTempUrl");
        String entryName = map.get("entryFileName");
        String userId = map.get("userId");
        String savePath = map.get("savePath");
        fileDealService.saveUnzipFile(unzipDirTempUrl, entryName, userId, savePath);
    }


    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(name = MQConstants.QUEUE_FILE_DEAL_CHECK_ES_USER_FILE, durable = "true"),
            exchange = @Exchange(name = MQConstants.EXCHANGE_FILE_DEAL, type = ExchangeTypes.DIRECT),
            key = {MQConstants.ROUTING_KEY_CHECK_ES_USER_FILE}
    ))
    public void listenCheckESUserFileId(String userFileId){
        fileDealService.checkESUserFileId(userFileId);
    }

}
