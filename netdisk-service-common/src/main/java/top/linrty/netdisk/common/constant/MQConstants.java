package top.linrty.netdisk.common.constant;

public interface MQConstants {

    /**
     * exchange
     */

    String EXCHANGE_FILE_DEAL = "netdisk.file-deal";


    String EXCHANGE_FILE = "netdisk.file";

    String EXCHANGE_USER_FILE = "netdisk.user-file";


    String EXCHANGE_RECOVERY_FILE = "netdisk.recovery-file";

    /**
     * queue
     */

    String QUEUE_FILE_DEAL_RESTORE_PARENT = "file-deal.restore-parent";

    String QUEUE_FILE_DEAL_UPLOAD_ES_BY_USER_FILE_ID = "file-deal.upload-es-by-user-file-id";

    String QUEUE_FILE_DEAL_DELETE_ES_BY_USER_FILE_ID = "file-deal.delete-es-by-user-file-id";

    String QUEUE_USER_FILE_UPDATE_FILE_DELETE_STATE_BY_FILE_PATH = "user-file.update-file-delete-state-by-file-path";

    String QUEUE_RECOVERY_FILE_DELETE_FILE = "recovery-file.delete-file";

    String QUEUE_FILE_DEAL_SAVE_UNZIP_FILE = "file-deal.save-unzip-file";

    String QUEUE_FILE_DEAL_CHECK_ES_USER_FILE = "file-deal.check-es-user-file";
    /**
     * routing key
     */

    String ROUTING_KEY_RESTORE_PARENT = "restore-parent";

    String ROUTING_KEY_ADD_USER_FILE_TO_ES = "add-user-file-to-es";

    String ROUTING_KEY_DELETE_USER_FILE_FROM_ES = "delete-user-file-from-es";

    String ROUTING_KEY_UPDATE_FILE_DELETE_STATE = "update-file-delete-state";

    String ROUTING_KEY_DELETE_FILE = "delete-file";

    String ROUTING_KEY_SAVE_UNZIP_FILE = "save-unzip-file";

    String ROUTING_KEY_CHECK_ES_USER_FILE = "check-es-user-file";
}
