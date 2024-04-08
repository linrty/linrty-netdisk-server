package top.linrty.netdisk.common.domain.po;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.apache.commons.io.FilenameUtils;
import top.linrty.netdisk.common.util.FileTypeUtil;

/**
 * @author MAC
 * @version 1.0
 * @description:
 * @date 2022/4/21 12:08
 */
@Data
@NoArgsConstructor
public class NetdiskFile {

    private String path;
    public static final String separator = "/";
    private boolean isDirectory;


    /**
     * 通过路径和是否是目录构造文件对象
     * @param pathname
     * @param isDirectory
     */
    public NetdiskFile(String pathname, boolean isDirectory) {
//        if (StringUtils.isEmpty(pathname)) {
//            throw new QiwenException("file name format error，pathname:" + pathname);
//        }
        this.path = formatPath(pathname);
        this.isDirectory = isDirectory;
    }

    public NetdiskFile(String pathname) {
        this.path = formatPath(pathname);

    }

    public NetdiskFile(String parent, String child, boolean isDirectory) {
//        if (StringUtils.isEmpty(child)) {
//            throw new QiwenException("file name format error，parent:" + parent +", child:" + child);
//        }
        if (parent != null) {
            // 如果父路径是/直接放回"",否则返回格式化后的正常路径
            String parentPath = separator.equals(formatPath(parent)) ? "" : formatPath(parent);
            String childPath = formatPath(child);
            if (childPath.startsWith(separator)) {
                // 如果子路径是/开头的，去掉/
                childPath = childPath.replaceFirst(separator, "");
            }
            // 整合父子路径
            this.path = parentPath + separator + childPath;
        } else {
            // 如果父路径是空的，直接返回格式化后的子路径
            this.path = formatPath(child);
        }
        this.isDirectory = isDirectory;
    }

    public static String formatPath(String path) {
        // 将路径中的分隔符统一为 /
        path = FileTypeUtil.pathSplitFormat(path);
        if ("/".equals(path)) {
            // 路径为/直接返回
            return path;
        }
        if (!path.startsWith(separator)) {
            // 路径不是一/开头的，加上/
            path = separator + path;
        }
        if (path.endsWith("/")) {
            // 路径以/结尾的，去掉/
            int length = path.length();
            return path.substring(0, length - 1);
        }

        return path;
    }

    /**
     * 获取当前路径的上一级路径
     * @return 上一级路径
     */
    public String getParent() {
        if (separator.equals(this.path)) {
            // 如果当前路径已经是/，没有上一级路径了直接返回null
            return null;
        }
        if (!this.path.contains("/")) {
            // 如果当前路径不包含/，也是没有上一级路径了
            return null;
        }
        int index = path.lastIndexOf(separator);
        if (index == 0) {
            return separator;
        }
        return path.substring(0, index);
    }

    /**
     * 返回一个以上一级父路径包装的对象
     * @return
     */
    public NetdiskFile getParentFile() {
        String parentPath = this.getParent();
        return new NetdiskFile(parentPath, true);
    }

    /**
     * 获取文件名带扩展名的
     * @return
     */
    public String getName() {
        int index = path.lastIndexOf(separator);
        if (!path.contains(separator)) {
            return path;
        }
        return path.substring(index + 1);
    }

    /**
     * 获取文件扩展名
     * @return
     */
    public String getExtendName() {
        return FilenameUtils.getExtension(getName());
    }

    /**
     * 获取文件名不带扩展名
     * @return
     */
    public String getNameNotExtend() {
        return FilenameUtils.removeExtension(getName());
    }

    public boolean isDirectory() {
       return isDirectory;
    }

    public boolean isFile() {
        return !isDirectory;
    }

    
}
