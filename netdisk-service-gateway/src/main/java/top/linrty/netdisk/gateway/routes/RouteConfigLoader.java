package top.linrty.netdisk.gateway.routes;

import cn.hutool.json.JSONUtil;
import com.alibaba.cloud.nacos.NacosConfigManager;
import com.alibaba.nacos.api.config.listener.Listener;
import com.alibaba.nacos.api.exception.NacosException;
import lombok.RequiredArgsConstructor;
import org.springframework.cloud.gateway.route.RouteDefinition;
import org.springframework.cloud.gateway.route.RouteDefinitionWriter;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import javax.annotation.PostConstruct;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;


/**
 * 负责从Nacos配置中心加载和更新网关路由配置
 */
@Component
@RequiredArgsConstructor
public class RouteConfigLoader {

    // Nacos配置管理器
    private final NacosConfigManager nacosConfigManager;

    private final static String DATA_ID = "gateway-routes.json";

    private final static String GROUP = "DEFAULT_GROUP";

    private final RouteDefinitionWriter writer;

    private static Set<String> routeIds=new HashSet<>();

    /**
     * 初始化路由配置
     * @throws NacosException
     */
    @PostConstruct
    public void initRouteConfiguration() throws NacosException {
        String configInfo = nacosConfigManager.getConfigService().getConfigAndSignListener(DATA_ID, GROUP, 1000, new Listener() {
            @Override
            public Executor getExecutor() {
                return Executors.newSingleThreadExecutor();
            }

            @Override
            public void receiveConfigInfo(String configInfo) {
                //监听路由变更,更新路由
                updateRouteConfigInfo(configInfo);
            }
        });
        //写入路由表
        updateRouteConfigInfo(configInfo);;
        
    }

    /**
     * 更新路由配置信息
     * @param configInfo
     */
    private void updateRouteConfigInfo(String configInfo) {
        List<RouteDefinition> routeDefinitions = JSONUtil.toList(configInfo, RouteDefinition.class);
        //删除旧路由
        for (String routeId : routeIds) {
            writer.delete(Mono.just(routeId)).subscribe();
        }
        routeIds.clear();
        //判断空
        if (routeDefinitions==null||routeDefinitions.isEmpty()){
            return;
        }
        //更新路由表
        for (RouteDefinition routeDefinition : routeDefinitions) {
            //写入
            writer.save(Mono.just(routeDefinition)).subscribe();
            routeIds.add(routeDefinition.getId());
        }

    }
}
