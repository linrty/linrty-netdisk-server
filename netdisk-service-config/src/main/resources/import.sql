delete from sys_param where sys_param_id in (1, 2, 3);
insert into sys_param (sys_param_id, sys_param_key, sys_param_value, sys_param_desc) values (1, 'totalStorageSize', '1024', '总存储大小（单位M）');
insert into sys_param (sys_param_id, sys_param_key, sys_param_value, sys_param_desc) values (2, 'initDataFlag', '1', '系统初始化数据标识');
insert into sys_param (sys_param_id, sys_param_key, sys_param_value, sys_param_desc) values (3, 'version', '1.1.2', '当前脚本的版本号');
