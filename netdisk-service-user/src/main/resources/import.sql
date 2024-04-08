delete from user where user_id = 1;
insert into user (user_id, username, telephone, salt, password, available) values (1, 'admin', 'admin', 'admin', 'df655ad8d3229f3269fad2a8bab59b6c', 1);

delete from role where role_id in (1, 2);
INSERT INTO `role` (`role_id`, `available`, `description`, `role_name`, `create_time`, `create_user_id`, `modify_time`, `modify_user_id`) VALUES (1, 1, '超级管理员', '超级管理员', NULL, NULL, '2024-04-03 17:37:06', NULL);
INSERT INTO `role` (`role_id`, `available`, `description`, `role_name`, `create_time`, `create_user_id`, `modify_time`, `modify_user_id`) VALUES (2, 1, '普通用户', '普通用户', NULL, NULL, NULL, NULL);


