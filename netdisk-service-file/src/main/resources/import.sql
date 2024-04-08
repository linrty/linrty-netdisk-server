delete from filetype where file_type_id in (0, 1, 2, 3, 4, 5);
INSERT INTO `filetype` (`file_type_id`, `file_type_name`) VALUES (0, '全部');
INSERT INTO `filetype` (`file_type_id`, `file_type_name`) VALUES (1, '图片');
INSERT INTO `filetype` (`file_type_id`, `file_type_name`) VALUES (2, '文档');
INSERT INTO `filetype` (`file_type_id`, `file_type_name`) VALUES (3, '视频');
INSERT INTO `filetype` (`file_type_id`, `file_type_name`) VALUES (4, '音乐');
INSERT INTO `filetype` (`file_type_id`, `file_type_name`) VALUES (5, '其他');


delete from fileextend where 1 = 1;
INSERT INTO `fileextend` (`file_extend_name`) VALUES ('bmp');
INSERT INTO `fileextend` (`file_extend_name`) VALUES ('jpg');
INSERT INTO `fileextend` (`file_extend_name`) VALUES ('png');
INSERT INTO `fileextend` (`file_extend_name`) VALUES ('tif');
INSERT INTO `fileextend` (`file_extend_name`) VALUES ('gif');
INSERT INTO `fileextend` (`file_extend_name`) VALUES ('jpeg');


INSERT INTO `fileextend` (`file_extend_name`) VALUES ('doc');
INSERT INTO `fileextend` (`file_extend_name`) VALUES ('docx');
INSERT INTO `fileextend` (`file_extend_name`) VALUES ('docm');
INSERT INTO `fileextend` (`file_extend_name`) VALUES ('dot');
INSERT INTO `fileextend` (`file_extend_name`) VALUES ('dotx');
INSERT INTO `fileextend` (`file_extend_name`) VALUES ('dotm');
INSERT INTO `fileextend` (`file_extend_name`) VALUES ('odt');
INSERT INTO `fileextend` (`file_extend_name`) VALUES ('fodt');
INSERT INTO `fileextend` (`file_extend_name`) VALUES ('ott');
INSERT INTO `fileextend` (`file_extend_name`) VALUES ('rtf');
INSERT INTO `fileextend` (`file_extend_name`) VALUES ('txt');
INSERT INTO `fileextend` (`file_extend_name`) VALUES ('html');
INSERT INTO `fileextend` (`file_extend_name`) VALUES ('htm');
INSERT INTO `fileextend` (`file_extend_name`) VALUES ('mht');
INSERT INTO `fileextend` (`file_extend_name`) VALUES ('xml');
INSERT INTO `fileextend` (`file_extend_name`) VALUES ('pdf');
INSERT INTO `fileextend` (`file_extend_name`) VALUES ('djvu');
INSERT INTO `fileextend` (`file_extend_name`) VALUES ('fb2');
INSERT INTO `fileextend` (`file_extend_name`) VALUES ('epub');
INSERT INTO `fileextend` (`file_extend_name`) VALUES ('xps');

INSERT INTO `fileextend` (`file_extend_name`) VALUES ('xls');
INSERT INTO `fileextend` (`file_extend_name`) VALUES ('xlsx');
INSERT INTO `fileextend` (`file_extend_name`) VALUES ('xlsm');
INSERT INTO `fileextend` (`file_extend_name`) VALUES ('xlt');
INSERT INTO `fileextend` (`file_extend_name`) VALUES ('xltx');
INSERT INTO `fileextend` (`file_extend_name`) VALUES ('xltm');
INSERT INTO `fileextend` (`file_extend_name`) VALUES ('ods');
INSERT INTO `fileextend` (`file_extend_name`) VALUES ('fods');
INSERT INTO `fileextend` (`file_extend_name`) VALUES ('ots');
INSERT INTO `fileextend` (`file_extend_name`) VALUES ('csv');

INSERT INTO `fileextend` (`file_extend_name`) VALUES ('pps');
INSERT INTO `fileextend` (`file_extend_name`) VALUES ('ppsx');
INSERT INTO `fileextend` (`file_extend_name`) VALUES ('ppsm');
INSERT INTO `fileextend` (`file_extend_name`) VALUES ('ppt');
INSERT INTO `fileextend` (`file_extend_name`) VALUES ('pptx');
INSERT INTO `fileextend` (`file_extend_name`) VALUES ('pptm');
INSERT INTO `fileextend` (`file_extend_name`) VALUES ('pot');
INSERT INTO `fileextend` (`file_extend_name`) VALUES ('potx');
INSERT INTO `fileextend` (`file_extend_name`) VALUES ('potm');
INSERT INTO `fileextend` (`file_extend_name`) VALUES ('odp');
INSERT INTO `fileextend` (`file_extend_name`) VALUES ('fodp');
INSERT INTO `fileextend` (`file_extend_name`) VALUES ('otp');

INSERT INTO `fileextend` (`file_extend_name`) VALUES ('hlp');
INSERT INTO `fileextend` (`file_extend_name`) VALUES ('wps');

INSERT INTO `fileextend` (`file_extend_name`) VALUES ('avi');
INSERT INTO `fileextend` (`file_extend_name`) VALUES ('mp4');
INSERT INTO `fileextend` (`file_extend_name`) VALUES ('mpg');
INSERT INTO `fileextend` (`file_extend_name`) VALUES ('mov');
INSERT INTO `fileextend` (`file_extend_name`) VALUES ('swf');
INSERT INTO `fileextend` (`file_extend_name`) VALUES ('wav');
INSERT INTO `fileextend` (`file_extend_name`) VALUES ('aif');
INSERT INTO `fileextend` (`file_extend_name`) VALUES ('au');
INSERT INTO `fileextend` (`file_extend_name`) VALUES ('mp3');
INSERT INTO `fileextend` (`file_extend_name`) VALUES ('ram');
INSERT INTO `fileextend` (`file_extend_name`) VALUES ('wma');
INSERT INTO `fileextend` (`file_extend_name`) VALUES ('mmf');
INSERT INTO `fileextend` (`file_extend_name`) VALUES ('amr');
INSERT INTO `fileextend` (`file_extend_name`) VALUES ('aac');
INSERT INTO `fileextend` (`file_extend_name`) VALUES ('flac');
INSERT INTO `fileextend` (`file_extend_name`) VALUES ('java');
INSERT INTO `fileextend` (`file_extend_name`) VALUES ('js');
INSERT INTO `fileextend` (`file_extend_name`) VALUES ('css');
INSERT INTO `fileextend` (`file_extend_name`) VALUES ('json');


delete from fileclassification where 1 = 1;
INSERT INTO `fileclassification` (`file_classification_id`, `file_type_id`, `file_extend_name`) VALUES (1, 1, 'bmp');
INSERT INTO `fileclassification` (`file_classification_id`, `file_type_id`, `file_extend_name`) VALUES (2, 1, 'jpg');
INSERT INTO `fileclassification` (`file_classification_id`, `file_type_id`, `file_extend_name`) VALUES (3, 1, 'png');
INSERT INTO `fileclassification` (`file_classification_id`, `file_type_id`, `file_extend_name`) VALUES (4, 1, 'tif');
INSERT INTO `fileclassification` (`file_classification_id`, `file_type_id`, `file_extend_name`) VALUES (5, 1, 'gif');
INSERT INTO `fileclassification` (`file_classification_id`, `file_type_id`, `file_extend_name`) VALUES (6, 1, 'jpeg');

INSERT INTO `fileclassification` (`file_classification_id`, `file_type_id`, `file_extend_name`) VALUES (7, 2, 'doc');
INSERT INTO `fileclassification` (`file_classification_id`, `file_type_id`, `file_extend_name`) VALUES (8, 2, 'docx');
INSERT INTO `fileclassification` (`file_classification_id`, `file_type_id`, `file_extend_name`) VALUES (9, 2, 'docm');
INSERT INTO `fileclassification` (`file_classification_id`, `file_type_id`, `file_extend_name`) VALUES (10, 2, 'dot');
INSERT INTO `fileclassification` (`file_classification_id`, `file_type_id`, `file_extend_name`) VALUES (11, 2, 'dotx');
INSERT INTO `fileclassification` (`file_classification_id`, `file_type_id`, `file_extend_name`) VALUES (12, 2, 'dotm');
INSERT INTO `fileclassification` (`file_classification_id`, `file_type_id`, `file_extend_name`) VALUES (13, 2, 'odt');
INSERT INTO `fileclassification` (`file_classification_id`, `file_type_id`, `file_extend_name`) VALUES (14, 2, 'fodt');
INSERT INTO `fileclassification` (`file_classification_id`, `file_type_id`, `file_extend_name`) VALUES (15, 2, 'ott');
INSERT INTO `fileclassification` (`file_classification_id`, `file_type_id`, `file_extend_name`) VALUES (16, 2, 'rtf');
INSERT INTO `fileclassification` (`file_classification_id`, `file_type_id`, `file_extend_name`) VALUES (17, 2, 'txt');
INSERT INTO `fileclassification` (`file_classification_id`, `file_type_id`, `file_extend_name`) VALUES (18, 2, 'html');
INSERT INTO `fileclassification` (`file_classification_id`, `file_type_id`, `file_extend_name`) VALUES (19, 2, 'htm');
INSERT INTO `fileclassification` (`file_classification_id`, `file_type_id`, `file_extend_name`) VALUES (20, 2, 'mht');
INSERT INTO `fileclassification` (`file_classification_id`, `file_type_id`, `file_extend_name`) VALUES (21, 2, 'xml');
INSERT INTO `fileclassification` (`file_classification_id`, `file_type_id`, `file_extend_name`) VALUES (22, 2, 'pdf');
INSERT INTO `fileclassification` (`file_classification_id`, `file_type_id`, `file_extend_name`) VALUES (23, 2, 'djvu');
INSERT INTO `fileclassification` (`file_classification_id`, `file_type_id`, `file_extend_name`) VALUES (24, 2, 'fb2');
INSERT INTO `fileclassification` (`file_classification_id`, `file_type_id`, `file_extend_name`) VALUES (25, 2, 'epub');
INSERT INTO `fileclassification` (`file_classification_id`, `file_type_id`, `file_extend_name`) VALUES (26, 2, 'xps');
INSERT INTO `fileclassification` (`file_classification_id`, `file_type_id`, `file_extend_name`) VALUES (27, 2, 'xls');
INSERT INTO `fileclassification` (`file_classification_id`, `file_type_id`, `file_extend_name`) VALUES (28, 2, 'xlsx');
INSERT INTO `fileclassification` (`file_classification_id`, `file_type_id`, `file_extend_name`) VALUES (29, 2, 'xlsm');
INSERT INTO `fileclassification` (`file_classification_id`, `file_type_id`, `file_extend_name`) VALUES (30, 2, 'xlt');
INSERT INTO `fileclassification` (`file_classification_id`, `file_type_id`, `file_extend_name`) VALUES (31, 2, 'xltx');
INSERT INTO `fileclassification` (`file_classification_id`, `file_type_id`, `file_extend_name`) VALUES (32, 2, 'xltm');
INSERT INTO `fileclassification` (`file_classification_id`, `file_type_id`, `file_extend_name`) VALUES (33, 2, 'ods');
INSERT INTO `fileclassification` (`file_classification_id`, `file_type_id`, `file_extend_name`) VALUES (34, 2, 'fods');
INSERT INTO `fileclassification` (`file_classification_id`, `file_type_id`, `file_extend_name`) VALUES (35, 2, 'ots');
INSERT INTO `fileclassification` (`file_classification_id`, `file_type_id`, `file_extend_name`) VALUES (36, 2, 'csv');
INSERT INTO `fileclassification` (`file_classification_id`, `file_type_id`, `file_extend_name`) VALUES (37, 2, 'pps');
INSERT INTO `fileclassification` (`file_classification_id`, `file_type_id`, `file_extend_name`) VALUES (38, 2, 'ppsx');
INSERT INTO `fileclassification` (`file_classification_id`, `file_type_id`, `file_extend_name`) VALUES (39, 2, 'ppsm');
INSERT INTO `fileclassification` (`file_classification_id`, `file_type_id`, `file_extend_name`) VALUES (40, 2, 'ppt');
INSERT INTO `fileclassification` (`file_classification_id`, `file_type_id`, `file_extend_name`) VALUES (41, 2, 'pptx');
INSERT INTO `fileclassification` (`file_classification_id`, `file_type_id`, `file_extend_name`) VALUES (42, 2, 'pptm');
INSERT INTO `fileclassification` (`file_classification_id`, `file_type_id`, `file_extend_name`) VALUES (43, 2, 'pot');
INSERT INTO `fileclassification` (`file_classification_id`, `file_type_id`, `file_extend_name`) VALUES (44, 2, 'potx');
INSERT INTO `fileclassification` (`file_classification_id`, `file_type_id`, `file_extend_name`) VALUES (45, 2, 'potm');
INSERT INTO `fileclassification` (`file_classification_id`, `file_type_id`, `file_extend_name`) VALUES (46, 2, 'odp');
INSERT INTO `fileclassification` (`file_classification_id`, `file_type_id`, `file_extend_name`) VALUES (47, 2, 'fodp');
INSERT INTO `fileclassification` (`file_classification_id`, `file_type_id`, `file_extend_name`) VALUES (48, 2, 'otp');
INSERT INTO `fileclassification` (`file_classification_id`, `file_type_id`, `file_extend_name`) VALUES (49, 2, 'hlp');
INSERT INTO `fileclassification` (`file_classification_id`, `file_type_id`, `file_extend_name`) VALUES (50, 2, 'wps');

INSERT INTO `fileclassification` (`file_classification_id`, `file_type_id`, `file_extend_name`) VALUES (51, 2, 'java');
INSERT INTO `fileclassification` (`file_classification_id`, `file_type_id`, `file_extend_name`) VALUES (52, 2, 'js');
INSERT INTO `fileclassification` (`file_classification_id`, `file_type_id`, `file_extend_name`) VALUES (53, 2, 'css');
INSERT INTO `fileclassification` (`file_classification_id`, `file_type_id`, `file_extend_name`) VALUES (54, 2, 'json');


INSERT INTO `fileclassification` (`file_classification_id`, `file_type_id`, `file_extend_name`) VALUES (55, 3, 'avi');
INSERT INTO `fileclassification` (`file_classification_id`, `file_type_id`, `file_extend_name`) VALUES (56, 3, 'mp4');
INSERT INTO `fileclassification` (`file_classification_id`, `file_type_id`, `file_extend_name`) VALUES (57, 3, 'mpg');
INSERT INTO `fileclassification` (`file_classification_id`, `file_type_id`, `file_extend_name`) VALUES (58, 3, 'mov');
INSERT INTO `fileclassification` (`file_classification_id`, `file_type_id`, `file_extend_name`) VALUES (59, 3, 'swf');

INSERT INTO `fileclassification` (`file_classification_id`, `file_type_id`, `file_extend_name`) VALUES (60, 4, 'wav');
INSERT INTO `fileclassification` (`file_classification_id`, `file_type_id`, `file_extend_name`) VALUES (61, 4, 'aif');
INSERT INTO `fileclassification` (`file_classification_id`, `file_type_id`, `file_extend_name`) VALUES (62, 4, 'au');
INSERT INTO `fileclassification` (`file_classification_id`, `file_type_id`, `file_extend_name`) VALUES (63, 4, 'mp3');
INSERT INTO `fileclassification` (`file_classification_id`, `file_type_id`, `file_extend_name`) VALUES (64, 4, 'ram');
INSERT INTO `fileclassification` (`file_classification_id`, `file_type_id`, `file_extend_name`) VALUES (65, 4, 'wma');
INSERT INTO `fileclassification` (`file_classification_id`, `file_type_id`, `file_extend_name`) VALUES (66, 4, 'mmf');
INSERT INTO `fileclassification` (`file_classification_id`, `file_type_id`, `file_extend_name`) VALUES (67, 4, 'amr');
INSERT INTO `fileclassification` (`file_classification_id`, `file_type_id`, `file_extend_name`) VALUES (68, 4, 'aac');
INSERT INTO `fileclassification` (`file_classification_id`, `file_type_id`, `file_extend_name`) VALUES (69, 4, 'flac');

INSERT INTO `fileclassification` (`file_classification_id`, `file_type_id`, `file_extend_name`) VALUES (70, 2, 'md');
INSERT INTO `fileclassification` (`file_classification_id`, `file_type_id`, `file_extend_name`) VALUES (71, 2, 'markdown');
