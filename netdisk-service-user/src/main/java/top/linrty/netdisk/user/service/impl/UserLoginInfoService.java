package top.linrty.netdisk.user.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import top.linrty.netdisk.user.domain.po.UserLoginInfo;
import top.linrty.netdisk.user.mapper.UserLoginInfoMapper;
import top.linrty.netdisk.user.service.IUserLoginInfoService;


@Slf4j
@Service
@Transactional(rollbackFor=Exception.class)
public class UserLoginInfoService extends ServiceImpl<UserLoginInfoMapper, UserLoginInfo> implements IUserLoginInfoService {


}
