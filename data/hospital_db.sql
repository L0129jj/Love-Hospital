
-- ============================================
-- 医院管理信息系统 - MySQL 建表脚本
-- ============================================

CREATE DATABASE IF NOT EXISTS hospital_db
  DEFAULT CHARACTER SET utf8mb4
  DEFAULT COLLATE utf8mb4_unicode_ci;

USE hospital_db;

SET FOREIGN_KEY_CHECKS = 0;
DROP TABLE IF EXISTS admin;
DROP TABLE IF EXISTS department;
DROP TABLE IF EXISTS doctor;
DROP TABLE IF EXISTS patient;
DROP TABLE IF EXISTS ward;
DROP TABLE IF EXISTS bed;
DROP TABLE IF EXISTS schedule;
DROP TABLE IF EXISTS registration;
DROP TABLE IF EXISTS drug;
DROP TABLE IF EXISTS prescription;
DROP TABLE IF EXISTS prescription_detail;
DROP TABLE IF EXISTS payment;
DROP TABLE IF EXISTS hospitalization_archive;
DROP TABLE IF EXISTS inpatient_record;
DROP TABLE IF EXISTS inpatient_fee;
SET FOREIGN_KEY_CHECKS = 1;

-- 1. 管理员表
CREATE TABLE admin (
  admin_id    INT AUTO_INCREMENT PRIMARY KEY COMMENT '管理员编号',
  username    VARCHAR(50)  NOT NULL UNIQUE   COMMENT '登录用户名',
  password    VARCHAR(100) NOT NULL          COMMENT '登录密码',
  name        VARCHAR(50)                    COMMENT '管理员姓名',
  role        VARCHAR(20)  DEFAULT 'ROLE_ADMIN' COMMENT '角色：ROLE_ADMIN / ROLE_DOCTOR',
  status      TINYINT      DEFAULT 1         COMMENT '状态：0=禁用，1=启用',
  login_status TINYINT     DEFAULT 0         COMMENT '登录状态：0=离线，1=在线'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='管理员表';

-- 2. 科室表
CREATE TABLE department (
  dept_id     INT AUTO_INCREMENT PRIMARY KEY COMMENT '科室编号',
  dept_name   VARCHAR(50) NOT NULL           COMMENT '科室名称',
  dept_type   ENUM('门诊','住院','门诊住院') NOT NULL COMMENT '科室类型',
  location    VARCHAR(100)                   COMMENT '科室位置',
  description TEXT                           COMMENT '科室描述'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='科室表';

-- 3. 医生表
CREATE TABLE doctor (
  doctor_id        INT AUTO_INCREMENT PRIMARY KEY COMMENT '医生编号',
  work_no          VARCHAR(50) UNIQUE             COMMENT '登录工号',
  name             VARCHAR(50) NOT NULL           COMMENT '医生姓名',
  gender           ENUM('男','女')               COMMENT '性别',
  title            ENUM('主任医师','副主任医师',
                    '主治医师','住院医师') NOT NULL COMMENT '职称',
  phone            VARCHAR(11)                    COMMENT '联系电话',
  password         VARCHAR(100) NOT NULL          COMMENT '登录密码',
  dept_id          INT NOT NULL                   COMMENT '所属科室',
  consultation_fee DECIMAL(10,2) NOT NULL         COMMENT '诊疗费',
  login_status     TINYINT      DEFAULT 0         COMMENT '登录状态：0=离线，1=在线',
  FOREIGN KEY (dept_id) REFERENCES department(dept_id) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='医生表';

-- 4. 病人表
CREATE TABLE patient (
  patient_id INT AUTO_INCREMENT PRIMARY KEY COMMENT '病案号',
  name       VARCHAR(50) NOT NULL           COMMENT '患者姓名',
  gender     ENUM('男','女')               COMMENT '性别',
  address    VARCHAR(200)                   COMMENT '地址',
  phone      VARCHAR(20)                    COMMENT '联系电话',
  username   VARCHAR(50) NOT NULL UNIQUE    COMMENT '登录用户名',
  password   VARCHAR(100) NOT NULL          COMMENT '登录密码',
  login_status TINYINT     DEFAULT 0         COMMENT '登录状态：0=离线，1=在线'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='病人表';

-- 5. 病房表
CREATE TABLE ward (
  ward_id      INT AUTO_INCREMENT PRIMARY KEY COMMENT '病房编号',
  ward_no      VARCHAR(20) NOT NULL           COMMENT '病房号',
  location     VARCHAR(100)                   COMMENT '病房位置',
  fee_standard DECIMAL(10,2) NOT NULL         COMMENT '床位收费标准（元/天）',
  dept_id      INT NOT NULL                   COMMENT '所属科室',
  FOREIGN KEY (dept_id) REFERENCES department(dept_id) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='病房表';

-- 6. 病床表
CREATE TABLE bed (
  bed_id  INT AUTO_INCREMENT PRIMARY KEY COMMENT '病床编号',
  ward_id INT NOT NULL                    COMMENT '所属病房',
  bed_no  VARCHAR(10) NOT NULL            COMMENT '床位号',
  status  ENUM('空闲','占用') DEFAULT '空闲' COMMENT '病床状态',
  UNIQUE KEY uk_ward_bed (ward_id, bed_no),
  FOREIGN KEY (ward_id) REFERENCES ward(ward_id) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='病床表';

-- 7. 排班表
CREATE TABLE schedule (
  schedule_id   INT AUTO_INCREMENT PRIMARY KEY COMMENT '排班编号',
  doctor_id     INT NOT NULL                   COMMENT '医生编号',
  schedule_date DATE NOT NULL                  COMMENT '排班日期',
  shift_type    ENUM('门诊坐诊','住院巡诊') NOT NULL COMMENT '班次类型',
  start_time    TIME NOT NULL                  COMMENT '开始时间',
  end_time      TIME NOT NULL                  COMMENT '结束时间',
  dept_id       INT NOT NULL                   COMMENT '排班科室',
  CONSTRAINT chk_time CHECK (end_time > start_time),
  INDEX idx_doctor_date (doctor_id, schedule_date),
  FOREIGN KEY (doctor_id) REFERENCES doctor(doctor_id) ON DELETE RESTRICT ON UPDATE CASCADE,
  FOREIGN KEY (dept_id) REFERENCES department(dept_id) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='排班表';

-- 8. 挂号表
CREATE TABLE registration (
  reg_id     INT AUTO_INCREMENT PRIMARY KEY COMMENT '挂号编号',
  patient_id INT NOT NULL                   COMMENT '患者病案号',
  doctor_id  INT NOT NULL                   COMMENT '接诊医生',
  dept_id    INT NOT NULL                   COMMENT '挂号科室',
  reg_time   DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '挂号时间',
  visit_type ENUM('初诊','复诊') NOT NULL COMMENT '就诊类型',
  status     ENUM('待就诊','就诊中','已完成','已取消')
             DEFAULT '待就诊'                    COMMENT '挂号状态',
  INDEX idx_patient (patient_id),
  INDEX idx_doctor (doctor_id),
  FOREIGN KEY (patient_id) REFERENCES patient(patient_id) ON DELETE RESTRICT ON UPDATE CASCADE,
  FOREIGN KEY (doctor_id) REFERENCES doctor(doctor_id) ON DELETE RESTRICT ON UPDATE CASCADE,
  FOREIGN KEY (dept_id) REFERENCES department(dept_id) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='挂号表';

-- 9. 药品表
CREATE TABLE drug (
  drug_id       INT AUTO_INCREMENT PRIMARY KEY COMMENT '药品编号',
  drug_name     VARCHAR(100) NOT NULL          COMMENT '药品名称',
  specification VARCHAR(100)                   COMMENT '药品规格',
  unit          VARCHAR(20)                    COMMENT '计量单位',
  price         DECIMAL(10,2) NOT NULL         COMMENT '单价',
  stock         INT NOT NULL DEFAULT 0         COMMENT '库存数量',
  description   TEXT                           COMMENT '药品描述'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='药品表';

-- 10. 处方表
CREATE TABLE prescription (
  prescription_id INT AUTO_INCREMENT PRIMARY KEY COMMENT '处方编号',
  reg_id          INT NOT NULL UNIQUE             COMMENT '关联挂号',
  doctor_id       INT NOT NULL                   COMMENT '开处方医生',
  patient_id      INT NOT NULL                   COMMENT '患者',
  symptoms        TEXT NOT NULL                   COMMENT '症状描述',
  diagnosis       VARCHAR(200)                   COMMENT '诊断结果',
  consultation_fee DECIMAL(10,2) NOT NULL        COMMENT '诊疗费',
  total_fee       DECIMAL(10,2) NOT NULL         COMMENT '总费用',
  create_time     DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '开具时间',
  status          ENUM('未缴费','已缴费','已取药')
                   DEFAULT '未缴费'                   COMMENT '处方状态',
  INDEX idx_patient (patient_id),
  FOREIGN KEY (reg_id) REFERENCES registration(reg_id) ON DELETE RESTRICT ON UPDATE CASCADE,
  FOREIGN KEY (doctor_id) REFERENCES doctor(doctor_id) ON DELETE RESTRICT ON UPDATE CASCADE,
  FOREIGN KEY (patient_id) REFERENCES patient(patient_id) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='处方表';

-- 11. 处方明细表
CREATE TABLE prescription_detail (
  detail_id       INT AUTO_INCREMENT PRIMARY KEY COMMENT '明细编号',
  prescription_id INT NOT NULL                  COMMENT '处方编号',
  drug_id         INT NOT NULL                   COMMENT '药品编号',
  quantity        INT NOT NULL                   COMMENT '数量',
  price           DECIMAL(10,2) NOT NULL         COMMENT '单价',
  dosage          VARCHAR(100)                   COMMENT '用法',
  subtotal        DECIMAL(10,2) NOT NULL         COMMENT '小计金额',
  INDEX idx_prescription (prescription_id),
  FOREIGN KEY (prescription_id) REFERENCES prescription(prescription_id) ON DELETE RESTRICT ON UPDATE CASCADE,
  FOREIGN KEY (drug_id) REFERENCES drug(drug_id) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='处方明细表';

-- 12. 缴费记录表
CREATE TABLE payment (
  payment_id      INT AUTO_INCREMENT PRIMARY KEY COMMENT '缴费编号',
  prescription_id INT NOT NULL UNIQUE            COMMENT '关联处方',
  patient_id      INT NOT NULL                   COMMENT '缴费患者',
  amount          DECIMAL(10,2) NOT NULL         COMMENT '缴费金额',
  pay_time        DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '缴费时间',
  pay_method      VARCHAR(20) DEFAULT '线上支付'  COMMENT '支付方式',
  FOREIGN KEY (prescription_id) REFERENCES prescription(prescription_id) ON DELETE RESTRICT ON UPDATE CASCADE,
  FOREIGN KEY (patient_id) REFERENCES patient(patient_id) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='缴费记录表';

-- 13. 住院档案表
CREATE TABLE hospitalization_archive (
  archive_id          INT AUTO_INCREMENT PRIMARY KEY COMMENT '档案号',
  patient_id          INT NOT NULL                   COMMENT '患者病案号',
  dept_id             INT NOT NULL                   COMMENT '住院科室',
  ward_id             INT NOT NULL                   COMMENT '入住病房',
  bed_id              INT NOT NULL                   COMMENT '分配病床',
  attending_doctor_id INT NOT NULL                  COMMENT '主治医生',
  admission_time      DATETIME NOT NULL              COMMENT '入院时间',
  discharge_time      DATETIME                       COMMENT '出院时间',
  prepaid_amount      DECIMAL(10,2) NOT NULL        COMMENT '预缴金额',
  balance             DECIMAL(10,2) DEFAULT 0       COMMENT '账户余额',
  status              ENUM('住院中','已出院') DEFAULT '住院中' COMMENT '住院状态',
  INDEX idx_patient (patient_id),
  INDEX idx_doctor (attending_doctor_id),
  FOREIGN KEY (patient_id) REFERENCES patient(patient_id) ON DELETE RESTRICT ON UPDATE CASCADE,
  FOREIGN KEY (dept_id) REFERENCES department(dept_id) ON DELETE RESTRICT ON UPDATE CASCADE,
  FOREIGN KEY (ward_id) REFERENCES ward(ward_id) ON DELETE RESTRICT ON UPDATE CASCADE,
  FOREIGN KEY (bed_id) REFERENCES bed(bed_id) ON DELETE RESTRICT ON UPDATE CASCADE,
  FOREIGN KEY (attending_doctor_id) REFERENCES doctor(doctor_id) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='住院档案表';

-- 14. 住院记录表
CREATE TABLE inpatient_record (
  record_id         INT AUTO_INCREMENT PRIMARY KEY COMMENT '记录编号',
  archive_id        INT NOT NULL                   COMMENT '关联住院档案',
  doctor_id         INT NOT NULL                   COMMENT '巡诊医生',
  record_date       DATE NOT NULL                  COMMENT '记录日期',
  symptoms          TEXT                           COMMENT '患者当日症状',
  treatment_plan    TEXT NOT NULL                  COMMENT '治疗方案',
  prescription_note TEXT                           COMMENT '诊疗处方',
  INDEX idx_archive (archive_id),
  FOREIGN KEY (archive_id) REFERENCES hospitalization_archive(archive_id) ON DELETE RESTRICT ON UPDATE CASCADE,
  FOREIGN KEY (doctor_id) REFERENCES doctor(doctor_id) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='住院记录表';

-- 15. 住院费用明细表
CREATE TABLE inpatient_fee (
  fee_id      INT AUTO_INCREMENT PRIMARY KEY COMMENT '费用编号',
  archive_id  INT NOT NULL                   COMMENT '关联住院档案',
  record_id   INT NOT NULL                   COMMENT '关联住院记录',
  fee_date    DATE NOT NULL                  COMMENT '费用日期',
  fee_type    ENUM('床位费','诊疗费','药品费','其他') NOT NULL COMMENT '费用类型',
  amount      DECIMAL(10,2) NOT NULL         COMMENT '费用金额',
  description VARCHAR(200)                   COMMENT '费用说明',
  INDEX idx_archive (archive_id),
  INDEX idx_record (record_id),
  FOREIGN KEY (archive_id) REFERENCES hospitalization_archive(archive_id) ON DELETE RESTRICT ON UPDATE CASCADE,
  FOREIGN KEY (record_id) REFERENCES inpatient_record(record_id) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='住院费用明细表';


-- ============================================
-- 医院管理信息系统 - 测试数据脚本
-- 按外键依赖顺序插入，可直接执行
-- ============================================

USE hospital_db;

-- ========================================
-- 1. 管理员表 (admin)
-- ========================================
INSERT INTO admin (username, password, name, role, status) VALUES
                                                 ('admin001', 'e10adc3949ba59abbe56e057f20f883e', '赵建国', 'ROLE_ADMIN', 1),
                                                 ('admin002', 'e10adc3949ba59abbe56e057f20f883e', '钱敏华', 'ROLE_ADMIN', 1),
                                                 ('admin003', 'e10adc3949ba59abbe56e057f20f883e', '孙丽萍', 'ROLE_ADMIN', 1);

-- 说明：密码统一使用 MD5('123456') = e10adc3949ba59abbe56e057f20f883e

-- ========================================
-- 2. 科室表 (department)
-- ========================================
INSERT INTO department (dept_name, dept_type, location, description) VALUES
                                                                         ('内科', '门诊住院', '门诊楼3楼/住院部5楼', '涵盖心血管、呼吸、消化等亚专业，承担门诊接诊与住院治疗双重职能'),
                                                                         ('外科', '门诊住院', '门诊楼4楼/住院部6楼', '涵盖普外科、骨科、泌尿外科等，开展各类常规及微创手术'),
                                                                         ('儿科', '门诊', '门诊楼2楼', '面向14岁以下儿童，提供常见病诊疗与预防保健服务'),
                                                                         ('妇产科', '门诊住院', '门诊楼2楼/住院部3楼', '提供妇科疾病诊疗、产前检查、分娩及产后康复全流程服务'),
                                                                         ('眼科', '门诊', '门诊楼5楼', '开展白内障、青光眼、屈光不正等眼科疾病的诊断与治疗'),
                                                                         ('皮肤科', '门诊', '门诊楼5楼', '诊治湿疹、银屑病、痤疮、过敏性皮炎等常见皮肤疾病'),
                                                                         ('骨科', '住院', '住院部7楼', '专注骨折创伤、关节置换、脊柱疾病等骨科住院手术治疗'),
                                                                         ('神经内科', '住院', '住院部4楼', '收治脑梗死、帕金森病、癫痫等神经系统疾病住院患者'),
                                                                         ('急诊科', '门诊', '急诊楼1楼', '24小时接诊急危重症患者，提供快速分诊与抢救服务');

-- ========================================
-- 3. 医生表 (doctor)
-- ========================================
INSERT INTO doctor (work_no, name, gender, title, phone, password, dept_id, consultation_fee) VALUES
-- 内科医生
('doc001', '李明轩', '男', '主任医师', '13800100001', 'e10adc3949ba59abbe56e057f20f883e', 1, 80.00),
('doc002', '王秀兰', '女', '副主任医师', '13800100002', 'e10adc3949ba59abbe56e057f20f883e', 1, 60.00),
('doc003', '张伟', '男', '主治医师', '13800100003', 'e10adc3949ba59abbe56e057f20f883e', 1, 40.00),
-- 外科医生
('doc004', '陈志强', '男', '主任医师', '13800100004', 'e10adc3949ba59abbe56e057f20f883e', 2, 90.00),
('doc005', '刘芳', '女', '副主任医师', '13800100005', 'e10adc3949ba59abbe56e057f20f883e', 2, 65.00),
-- 儿科医生
('doc006', '赵丽娜', '女', '主任医师', '13800100006', 'e10adc3949ba59abbe56e057f20f883e', 3, 70.00),
('doc007', '孙鹏飞', '男', '主治医师', '13800100007', 'e10adc3949ba59abbe56e057f20f883e', 3, 35.00),
-- 妇产科医生
('doc008', '周慧敏', '女', '主任医师', '13800100008', 'e10adc3949ba59abbe56e057f20f883e', 4, 80.00),
('doc009', '吴雪梅', '女', '副主任医师', '13800100009', 'e10adc3949ba59abbe56e057f20f883e', 4, 55.00),
-- 眼科医生
('doc010', '郑国栋', '男', '副主任医师', '13800100010', 'e10adc3949ba59abbe56e057f20f883e', 5, 60.00),
-- 皮肤科医生
('doc011', '黄丽华', '女', '主治医师', '13800100011', 'e10adc3949ba59abbe56e057f20f883e', 6, 40.00),
-- 骨科医生
('doc012', '林志远', '男', '主任医师', '13800100012', 'e10adc3949ba59abbe56e057f20f883e', 7, 100.00),
('doc013', '杨建军', '男', '副主任医师', '13800100013', 'e10adc3949ba59abbe56e057f20f883e', 7, 70.00),
-- 神经内科医生
('doc014', '何文博', '男', '主任医师', '13800100014', 'e10adc3949ba59abbe56e057f20f883e', 8, 90.00),
('doc015', '曹丽娟', '女', '主治医师', '13800100015', 'e10adc3949ba59abbe56e057f20f883e', 8, 45.00),
-- 急诊科医生
('doc016', '马超', '男', '副主任医师', '13800100016', 'e10adc3949ba59abbe56e057f20f883e', 9, 60.00);

-- ========================================
-- 4. 病人表 (patient)
-- ========================================
INSERT INTO patient (name, gender, address, phone, username, password) VALUES
                                                                           ('陈小明', '男', '北京市海淀区中关村南大街5号', '13900001001', 'patient001', 'e10adc3949ba59abbe56e057f20f883e'),
                                                                           ('李秀英', '女', '北京市朝阳区建国路88号', '13900001002', 'patient002', 'e10adc3949ba59abbe56e057f20f883e'),
                                                                           ('王大山', '男', '北京市西城区德胜门外大街10号', '13900001003', 'patient003', 'e10adc3949ba59abbe56e057f20f883e'),
                                                                           ('张翠花', '女', '北京市东城区东四十条22号', '13900001004', 'patient004', 'e10adc3949ba59abbe56e057f20f883e'),
                                                                           ('刘志伟', '男', '北京市丰台区南三环西路16号', '13900001005', 'patient005', 'e10adc3949ba59abbe56e057f20f883e'),
                                                                           ('赵美玲', '女', '北京市石景山区古城南路8号', '13900001006', 'patient006', 'e10adc3949ba59abbe56e057f20f883e'),
                                                                           ('孙国强', '男', '北京市通州区新华西街12号', '13900001007', 'patient007', 'e10adc3949ba59abbe56e057f20f883e'),
                                                                           ('周晓燕', '女', '北京市昌平区回龙观东大街6号', '13900001008', 'patient008', 'e10adc3949ba59abbe56e057f20f883e'),
                                                                           ('吴明辉', '男', '北京市大兴区黄村东大街55号', '13900001009', 'patient009', 'e10adc3949ba59abbe56e057f20f883e'),
                                                                           ('郑婉婷', '女', '北京市顺义区仁和镇府前街3号', '13900001010', 'patient010', 'e10adc3949ba59abbe56e057f20f883e'),
                                                                           ('钱浩然', '男', '北京市海淀区清华园1号', '13900001011', 'patient011', 'e10adc3949ba59abbe56e057f20f883e'),
                                                                           ('冯雅琴', '女', '北京市西城区金融街7号', '13900001012', 'patient012', 'e10adc3949ba59abbe56e057f20f883e');

-- ========================================
-- 5. 病房表 (ward)
-- ========================================
INSERT INTO ward (ward_no, location, fee_standard, dept_id) VALUES
-- 内科病房
('内科-501', '住院部5楼', 80.00, 1),
('内科-502', '住院部5楼', 80.00, 1),
-- 外科病房
('外科-601', '住院部6楼', 100.00, 2),
('外科-602', '住院部6楼', 100.00, 2),
-- 妇产科病房
('妇产-301', '住院部3楼', 120.00, 4),
('妇产-302', '住院部3楼', 120.00, 4),
-- 骨科病房
('骨科-701', '住院部7楼', 90.00, 7),
('骨科-702', '住院部7楼', 90.00, 7),
-- 神经内科病房
('神内-401', '住院部4楼', 85.00, 8),
('神内-402', '住院部4楼', 85.00, 8);

-- ========================================
-- 6. 病床表 (bed)
-- ========================================
INSERT INTO bed (ward_id, bed_no, status) VALUES
-- 内科-501 (ward_id=1)
-- bed_id=1: 空闲 | bed_id=2: 占用(已出院患者陈小明) | bed_id=3: 空闲 | bed_id=4: 空闲
(1, '01', '空闲'), (1, '02', '占用'), (1, '03', '空闲'), (1, '04', '空闲'),
-- 内科-502 (ward_id=2)
(2, '01', '空闲'), (2, '02', '空闲'), (2, '03', '空闲'), (2, '04', '空闲'),
-- 外科-601 (ward_id=3)
-- bed_id=9: 占用(住院中王大山) | bed_id=10: 占用(已出院刘志伟) | bed_id=12: 占用
(3, '01', '占用'), (3, '02', '占用'), (3, '03', '空闲'), (3, '04', '占用'),
-- 外科-602 (ward_id=4)
(4, '01', '空闲'), (4, '02', '空闲'), (4, '03', '空闲'), (4, '04', '空闲'),
-- 妇产-301 (ward_id=5)
-- bed_id=17: 占用(住院中张翠花) | bed_id=18: 占用(住院中郑婉婷)
(5, '01', '占用'), (5, '02', '占用'), (5, '03', '空闲'),
-- 妇产-302 (ward_id=6)
(6, '01', '空闲'), (6, '02', '空闲'), (6, '03', '空闲'),
-- 骨科-701 (ward_id=7)
-- bed_id=24: 占用 | bed_id=25: 占用(住院中钱浩然)
(7, '01', '空闲'), (7, '02', '占用'), (7, '03', '占用'), (7, '04', '空闲'),
-- 骨科-702 (ward_id=8)
(8, '01', '空闲'), (8, '02', '空闲'), (8, '03', '空闲'),
-- 神内-401 (ward_id=9)
-- bed_id=30: 占用(住院中周晓燕)
(9, '01', '占用'), (9, '02', '空闲'), (9, '03', '空闲'), (9, '04', '空闲'),
-- 神内-402 (ward_id=10)
(10, '01', '空闲'), (10, '02', '空闲'), (10, '03', '空闲');

-- ========================================
-- 7. 排班表 (schedule)
-- ========================================
INSERT INTO schedule (doctor_id, schedule_date, shift_type, start_time, end_time, dept_id) VALUES
-- 内科排班
(1, '2026-05-25', '门诊坐诊', '08:00:00', '12:00:00', 1),
(2, '2026-05-25', '门诊坐诊', '14:00:00', '17:30:00', 1),
(3, '2026-05-25', '住院巡诊', '09:00:00', '11:00:00', 1),
(1, '2026-05-26', '门诊坐诊', '08:00:00', '12:00:00', 1),
(3, '2026-05-26', '住院巡诊', '09:00:00', '11:00:00', 1),
-- 外科排班
(4, '2026-05-25', '门诊坐诊', '08:00:00', '12:00:00', 2),
(5, '2026-05-25', '门诊坐诊', '14:00:00', '17:30:00', 2),
(4, '2026-05-26', '住院巡诊', '09:00:00', '11:30:00', 2),
-- 儿科排班
(6, '2026-05-25', '门诊坐诊', '08:00:00', '12:00:00', 3),
(7, '2026-05-25', '门诊坐诊', '14:00:00', '17:30:00', 3),
(6, '2026-05-26', '门诊坐诊', '08:00:00', '12:00:00', 3),
-- 妇产科排班
(8, '2026-05-25', '门诊坐诊', '08:00:00', '12:00:00', 4),
(9, '2026-05-25', '住院巡诊', '09:00:00', '11:00:00', 4),
(8, '2026-05-26', '门诊坐诊', '08:00:00', '12:00:00', 4),
-- 眼科排班
(10, '2026-05-25', '门诊坐诊', '08:30:00', '12:00:00', 5),
(10, '2026-05-26', '门诊坐诊', '08:30:00', '12:00:00', 5),
-- 皮肤科排班
(11, '2026-05-25', '门诊坐诊', '08:00:00', '12:00:00', 6),
(11, '2026-05-26', '门诊坐诊', '14:00:00', '17:30:00', 6),
-- 骨科排班
(12, '2026-05-25', '住院巡诊', '09:00:00', '11:00:00', 7),
(13, '2026-05-25', '住院巡诊', '14:00:00', '16:00:00', 7),
(12, '2026-05-26', '住院巡诊', '09:00:00', '11:00:00', 7),
-- 神经内科排班
(14, '2026-05-25', '住院巡诊', '09:00:00', '11:30:00', 8),
(15, '2026-05-25', '住院巡诊', '14:00:00', '16:00:00', 8),
(14, '2026-05-26', '住院巡诊', '09:00:00', '11:30:00', 8),
-- 急诊科排班
(16, '2026-05-25', '门诊坐诊', '08:00:00', '20:00:00', 9),
(16, '2026-05-26', '门诊坐诊', '08:00:00', '20:00:00', 9);

-- ========================================
-- 8. 挂号表 (registration)
-- ========================================
INSERT INTO registration (patient_id, doctor_id, dept_id, reg_time, visit_type, status) VALUES
-- 已完成的挂号
(1, 1, 1, '2026-05-20 08:30:00', '初诊', '已完成'),
(2, 6, 3, '2026-05-20 09:00:00', '初诊', '已完成'),
(3, 4, 2, '2026-05-21 08:15:00', '初诊', '已完成'),
(4, 8, 4, '2026-05-21 09:30:00', '初诊', '已完成'),
(5, 10, 5, '2026-05-21 10:00:00', '初诊', '已完成'),
-- 已缴费待取药的挂号
(6, 2, 1, '2026-05-22 14:10:00', '初诊', '已完成'),
(7, 11, 6, '2026-05-22 08:45:00', '初诊', '已完成'),
-- 就诊中的挂号
(8, 1, 1, '2026-05-23 08:20:00', '复诊', '就诊中'),
(9, 6, 3, '2026-05-23 09:00:00', '初诊', '就诊中'),
-- 待就诊的挂号
(1, 1, 1, '2026-05-25 08:00:00', '复诊', '待就诊'),
(3, 4, 2, '2026-05-25 08:00:00', '复诊', '待就诊'),
(10, 8, 4, '2026-05-25 08:00:00', '初诊', '待就诊'),
(11, 10, 5, '2026-05-25 08:30:00', '初诊', '待就诊'),
(12, 2, 1, '2026-05-25 14:00:00', '初诊', '待就诊'),
(4, 9, 4, '2026-05-26 08:00:00', '复诊', '待就诊'),
-- 已取消的挂号
(5, 7, 3, '2026-05-22 14:05:00', '初诊', '已取消');

-- ========================================
-- 9. 药品表 (drug)
-- ========================================
INSERT INTO drug (drug_name, specification, unit, price, stock, description) VALUES
                                                                                 ('阿莫西林胶囊', '0.5g*24粒/盒', '盒', 12.80, 500, '广谱半合成青霉素类抗生素，用于敏感菌所致的呼吸道、泌尿道等感染'),
                                                                                 ('布洛芬缓释胶囊', '0.3g*20粒/盒', '盒', 15.50, 600, '非甾体抗炎药，用于缓解轻至中度疼痛及退热'),
                                                                                 ('对乙酰氨基酚片', '0.5g*12片/盒', '盒', 8.00, 800, '解热镇痛药，适用于普通感冒或流感引起的发热'),
                                                                                 ('阿托伐他汀钙片', '20mg*7片/盒', '盒', 35.60, 300, '调血脂药，用于降低胆固醇和低密度脂蛋白'),
                                                                                 ('硝苯地平控释片', '30mg*7片/盒', '盒', 28.90, 250, '钙通道阻滞剂，用于高血压和心绞痛的治疗'),
                                                                                 ('奥美拉唑肠溶胶囊', '20mg*14粒/盒', '盒', 22.00, 450, '质子泵抑制剂，用于胃溃疡、反流性食管炎等'),
                                                                                 ('头孢克洛缓释片', '0.375g*6片/盒', '盒', 32.50, 350, '第二代头孢菌素，用于呼吸道、皮肤软组织感染'),
                                                                                 ('氯雷他定片', '10mg*6片/盒', '盒', 18.80, 500, '第二代抗组胺药，用于过敏性鼻炎、荨麻疹等'),
                                                                                 ('复方甘草片', '100片/瓶', '瓶', 6.50, 700, '镇咳祛痰药，用于上呼吸道感染引起的咳嗽'),
                                                                                 ('蒙脱石散', '3g*10袋/盒', '盒', 19.80, 400, '肠道吸附剂，用于急慢性腹泻的治疗'),
                                                                                 ('甲硝唑片', '0.2g*100片/瓶', '瓶', 5.20, 600, '抗厌氧菌药，用于肠道及泌尿生殖道感染'),
                                                                                 ('维生素C片', '0.1g*100片/瓶', '瓶', 3.80, 1000, '维生素类药，用于坏血病的预防及辅助治疗'),
                                                                                 ('红霉素软膏', '10g/支', '支', 4.50, 500, '大环内酯类抗生素外用制剂，用于脓疱疮、毛囊炎等'),
                                                                                 ('开塞露', '20ml*2支/盒', '盒', 7.60, 300, '润滑性通便药，用于小儿及老年体弱者的便秘'),
                                                                                 ('双氯芬酸钠肠溶片', '25mg*30片/盒', '盒', 14.20, 350, '非甾体抗炎药，用于类风湿关节炎、骨关节炎等'),
                                                                                 ('盐酸二甲双胍片', '0.5g*20片/盒', '盒', 9.80, 280, '降血糖药，用于2型糖尿病的一线治疗'),
                                                                                 ('硝化甘油贴片', '5mg*7贴/盒', '盒', 42.00, 150, '血管扩张剂，用于冠心病的长期治疗和预防心绞痛'),
                                                                                 ('地西泮片', '2.5mg*30片/盒', '盒', 11.50, 200, '苯二氮卓类药，用于焦虑、失眠及抗惊厥');

-- ========================================
-- 10. 处方表 (prescription)
-- ========================================
-- 对应已完成的挂号（reg_id 1~5, 6~7）
INSERT INTO prescription (reg_id, doctor_id, patient_id, symptoms, diagnosis, consultation_fee, total_fee, create_time, status) VALUES
                                                                                                                                    (1, 1, 1, '咳嗽、咽痛3天，伴低热37.8°C', '急性上呼吸道感染', 80.00, 118.30, '2026-05-20 09:15:00', '已取药'),
                                                                                                                                    (2, 6, 2, '发热2天，体温38.5°C，流涕、食欲减退', '小儿上呼吸道感染', 70.00, 117.30, '2026-05-20 09:45:00', '已取药'),
                                                                                                                                    (3, 4, 3, '右下腹疼痛6小时，伴恶心', '急性阑尾炎（需手术）', 90.00, 137.80, '2026-05-21 08:40:00', '已取药'),
                                                                                                                                    (4, 8, 4, '停经45天，下腹隐痛，少量阴道出血', '先兆流产', 80.00, 135.80, '2026-05-21 10:00:00', '已取药'),
                                                                                                                                    (5, 10, 5, '双眼干涩、视物模糊1周', '干眼症', 60.00, 68.00, '2026-05-21 10:30:00', '已缴费'),
                                                                                                                                    (6, 2, 6, '头晕、乏力1月余，血压150/95mmHg', '高血压2级', 60.00, 141.80, '2026-05-22 14:30:00', '已缴费'),
                                                                                                                                    (7, 11, 7, '全身皮疹3天，瘙痒明显', '急性荨麻疹', 40.00, 58.80, '2026-05-22 09:10:00', '已缴费');

-- ========================================
-- 11. 处方明细表 (prescription_detail)
-- ========================================
-- 处方1：急性上呼吸道感染
INSERT INTO prescription_detail (prescription_id, drug_id, quantity, price, dosage, subtotal) VALUES
                                                                                                  (1, 7, 2, 32.50, '每次1片，每日2次，饭后服', 65.00),
                                                                                                  (1, 9, 1, 6.50, '每次3片，每日3次', 6.50),
-- 处方2：小儿上呼吸道感染
                                                                                                  (2, 1, 2, 12.80, '每次1粒，每日3次', 25.60),
                                                                                                  (2, 3, 1, 8.00, '每次半片，体温>38.5°C时服用', 8.00),
                                                                                                  (2, 10, 1, 19.80, '每次1袋，每日3次', 19.80),
-- 处方3：急性阑尾炎术前用药
                                                                                                  (3, 7, 1, 32.50, '每次1片，每日2次', 32.50),
                                                                                                  (3, 11, 1, 5.20, '每次2片，每日3次', 5.20),
-- 处方4：先兆流产保胎
                                                                                                  (4, 11, 1, 5.20, '每次2片，每日3次', 5.20),
                                                                                                  (4, 12, 2, 3.80, '每次1片，每日2次', 7.60),
-- 处方5：干眼症
                                                                                                  (5, 13, 1, 4.50, '涂眼，每日2次', 4.50),
-- 处方6：高血压2级
                                                                                                  (6, 5, 2, 28.90, '每次1片，每日1次，晨起服用', 57.80),
                                                                                                  (6, 4, 1, 35.60, '每次1片，每晚1次', 35.60),
-- 处方7：急性荨麻疹
                                                                                                  (7, 8, 2, 18.80, '每次1片，每日1次', 37.60);

-- ========================================
-- 12. 缴费记录表 (payment)
-- ========================================
INSERT INTO payment (prescription_id, patient_id, amount, pay_time, pay_method) VALUES
                                                                                    (1, 1, 118.30, '2026-05-20 09:20:00', '线上支付'),
                                                                                    (2, 2, 117.30, '2026-05-20 09:50:00', '线上支付'),
                                                                                    (3, 3, 137.80, '2026-05-21 08:50:00', '线上支付'),
                                                                                    (4, 4, 135.80, '2026-05-21 10:10:00', '线上支付'),
                                                                                    (5, 5, 68.00, '2026-05-21 10:35:00', '线上支付'),
                                                                                    (6, 6, 141.80, '2026-05-22 14:35:00', '线上支付'),
                                                                                    (7, 7, 58.80, '2026-05-22 09:15:00', '线上支付');

-- ========================================
-- 13. 住院档案表 (hospitalization_archive)
-- ========================================
INSERT INTO hospitalization_archive (patient_id, dept_id, ward_id, bed_id, attending_doctor_id, admission_time, discharge_time, prepaid_amount, balance, status) VALUES
-- 住院中
-- 住院中患者（病床状态应为占用）
(3, 2, 3, 9, 4, '2026-05-21 10:00:00', NULL, 20000.00, 19280.00, '住院中'),   -- 王大山-外科601-01
(4, 4, 5, 17, 8, '2026-05-21 11:00:00', NULL, 15000.00, 14620.00, '住院中'),   -- 张翠花-妇产301-01
(9, 8, 9, 30, 14, '2026-05-22 08:00:00', NULL, 25000.00, 24230.00, '住院中'),  -- 周晓燕-神内401-01
(10, 4, 5, 18, 9, '2026-05-23 09:00:00', NULL, 12000.00, 11720.00, '住院中'),   -- 郑婉婷-妇产301-02
(11, 7, 7, 25, 12, '2026-05-20 14:00:00', NULL, 30000.00, 28560.00, '住院中'), -- 钱浩然-骨科701-03
-- 已出院患者
(1, 1, 1, 2, 1, '2026-05-10 08:00:00', '2026-05-18 10:00:00', 10000.00, 3200.00, '已出院'),   -- 陈小明-内科501-02(已出院但bed未释放)
(5, 2, 3, 10, 5, '2026-04-28 09:00:00', '2026-05-08 14:00:00', 18000.00, 5600.00, '已出院'); -- 刘志伟-外科601-02(已出院但bed未释放)

-- ========================================
-- 14. 住院记录表 (inpatient_record)
-- ========================================
-- 住院中患者的记录
-- 患者王大山(archive_id=1): 阑尾炎术后
INSERT INTO inpatient_record (archive_id, doctor_id, record_date, symptoms, treatment_plan, prescription_note) VALUES
                                                                                                                   (1, 4, '2026-05-21', '术后第一天，切口轻微疼痛，体温37.2°C', '抗感染、补液支持治疗，观察切口愈合', '头孢克洛缓释片 0.375g bid + 甲硝唑片 0.2g tid'),
                                                                                                                   (1, 4, '2026-05-22', '术后第二天，疼痛减轻，体温正常，已排气', '继续抗感染治疗，逐步恢复流质饮食', '头孢克洛缓释片 0.375g bid'),
                                                                                                                   (1, 5, '2026-05-23', '术后第三天，饮食恢复良好，切口无红肿', '停用静脉抗生素，改口服抗生素巩固', '头孢克洛缓释片 0.375g bid'),

-- 患者张翠花(archive_id=2): 先兆流产保胎
                                                                                                                   (2, 8, '2026-05-21', '阴道出血减少，下腹隐痛缓解', '继续保胎治疗，卧床休息，监测胎心', '黄体酮胶囊 100mg bid + 维生素C 0.1g tid'),
                                                                                                                   (2, 9, '2026-05-22', '阴道出血停止，无腹痛', '继续保胎，B超复查胚胎发育', '黄体酮胶囊 100mg bid'),

-- 患者周晓燕(archive_id=3): 脑梗死
                                                                                                                   (3, 14, '2026-05-22', '左侧肢体乏力，言语含糊', '抗血小板聚集、改善循环、营养神经', '阿司匹林肠溶片 100mg qd + 阿托伐他汀 20mg qn'),
                                                                                                                   (3, 14, '2026-05-23', '左侧肢体肌力较前改善，言语较前清晰', '继续当前治疗方案，加用康复训练', '阿司匹林肠溶片 100mg qd + 奥美拉唑 20mg qd'),

-- 患者郑婉婷(archive_id=4): 待产
                                                                                                                   (4, 9, '2026-05-23', '孕38周，规律宫缩4小时，胎心正常', '待产观察，监测胎心及宫缩情况', '暂无特殊用药'),

-- 患者钱浩然(archive_id=5): 股骨颈骨折
                                                                                                                   (5, 12, '2026-05-20', '右髋部疼痛、活动受限，外伤后2小时', '术前准备，完善检查，拟行关节置换术', '双氯芬酸钠 25mg tid + 头孢克洛 0.375g bid'),
                                                                                                                   (5, 12, '2026-05-21', '术后第一天，切口疼痛可耐受', '抗感染、镇痛、预防深静脉血栓', '头孢克洛 0.375g bid + 双氯芬酸钠 25mg tid'),
                                                                                                                   (5, 13, '2026-05-22', '术后第二天，疼痛减轻，开始被动活动', '继续抗感染镇痛，指导康复锻炼', '头孢克洛 0.375g bid + 双氯芬酸钠 25mg tid'),

-- 已出院患者陈小明(archive_id=6): 肺炎
                                                                                                                   (6, 1, '2026-05-10', '咳嗽、咳痰1周，发热3天，体温38.2°C', '抗感染、化痰止咳，完善胸部CT', '阿莫西林 0.5g tid + 复方甘草片 3片 tid'),
                                                                                                                   (6, 2, '2026-05-12', '体温正常，咳嗽减轻，CT示炎症吸收', '继续抗感染治疗，复查血常规', '阿莫西林 0.5g tid'),
                                                                                                                   (6, 1, '2026-05-16', '症状明显好转，复查CT炎症基本吸收', '巩固治疗2天，准备出院', '阿莫西林 0.5g tid'),

-- 已出院患者刘志伟(archive_id=7): 胆囊炎
                                                                                                                   (7, 5, '2026-04-28', '右上腹疼痛伴恶心呕吐1天', '禁食、抗感染、补液，拟行腹腔镜胆囊切除', '头孢克洛 0.375g bid + 甲硝唑 0.2g tid'),
                                                                                                                   (7, 5, '2026-04-30', '术后第一天，切口轻微疼痛', '抗感染、补液支持，逐步恢复饮食', '头孢克洛 0.375g bid'),
                                                                                                                   (7, 4, '2026-05-05', '术后恢复良好，切口拆线，饮食正常', '出院观察，门诊随访', '无');

-- ========================================
-- 15. 住院费用明细表 (inpatient_fee)
-- ========================================
-- 患者王大山(archive_id=1)费用
INSERT INTO inpatient_fee (archive_id, record_id, fee_date, fee_type, amount, description) VALUES
                                                                                               (1, 1, '2026-05-21', '床位费', 100.00, '外科-601-01床位费'),
                                                                                               (1, 1, '2026-05-21', '诊疗费', 90.00, '陈志强主任医师巡诊费'),
                                                                                               (1, 1, '2026-05-21', '药品费', 37.70, '头孢克洛缓释片+甲硝唑片'),
                                                                                               (1, 1, '2026-05-21', '其他', 50.00, '静脉输液操作费'),
                                                                                               (1, 2, '2026-05-22', '床位费', 100.00, '外科-601-01床位费'),
                                                                                               (1, 2, '2026-05-22', '诊疗费', 90.00, '陈志强主任医师巡诊费'),
                                                                                               (1, 2, '2026-05-22', '药品费', 32.50, '头孢克洛缓释片'),
                                                                                               (1, 3, '2026-05-23', '床位费', 100.00, '外科-601-01床位费'),
                                                                                               (1, 3, '2026-05-23', '药品费', 32.50, '头孢克洛缓释片'),

-- 患者张翠花(archive_id=2)费用
                                                                                               (2, 4, '2026-05-21', '床位费', 120.00, '妇产-301-01床位费'),
                                                                                               (2, 4, '2026-05-21', '诊疗费', 80.00, '周慧敏主任医师巡诊费'),
                                                                                               (2, 4, '2026-05-21', '药品费', 12.80, '黄体酮胶囊+维生素C'),
                                                                                               (2, 5, '2026-05-22', '床位费', 120.00, '妇产-301-01床位费'),
                                                                                               (2, 5, '2026-05-22', '诊疗费', 55.00, '吴雪梅副主任医师巡诊费'),

-- 患者周晓燕(archive_id=3)费用
                                                                                               (3, 6, '2026-05-22', '床位费', 85.00, '神内-401-01床位费'),
                                                                                               (3, 6, '2026-05-22', '诊疗费', 90.00, '何文博主任医师巡诊费'),
                                                                                               (3, 6, '2026-05-22', '药品费', 64.50, '阿司匹林+阿托伐他汀'),
                                                                                               (3, 7, '2026-05-23', '床位费', 85.00, '神内-401-01床位费'),
                                                                                               (3, 7, '2026-05-23', '诊疗费', 90.00, '何文博主任医师巡诊费'),
                                                                                               (3, 7, '2026-05-23', '药品费', 57.00, '阿司匹林+奥美拉唑'),
                                                                                               (3, 7, '2026-05-23', '其他', 50.00, '康复训练费'),

-- 患者郑婉婷(archive_id=4)费用
                                                                                               (4, 8, '2026-05-23', '床位费', 120.00, '妇产-301-02床位费'),
                                                                                               (4, 8, '2026-05-23', '诊疗费', 55.00, '吴雪梅副主任医师巡诊费'),

-- 患者钱浩然(archive_id=5)费用
                                                                                               (5, 9, '2026-05-20', '床位费', 90.00, '骨科-701-02床位费'),
                                                                                               (5, 9, '2026-05-20', '诊疗费', 100.00, '林志远主任医师巡诊费'),
                                                                                               (5, 9, '2026-05-20', '药品费', 46.70, '双氯芬酸钠+头孢克洛'),
                                                                                               (5, 9, '2026-05-20', '其他', 500.00, '术前检查费（CT+血常规+凝血）'),
                                                                                               (5, 10, '2026-05-21', '床位费', 90.00, '骨科-701-02床位费'),
                                                                                               (5, 10, '2026-05-21', '诊疗费', 100.00, '林志远主任医师巡诊费'),
                                                                                               (5, 10, '2026-05-21', '药品费', 46.70, '头孢克洛+双氯芬酸钠'),
                                                                                               (5, 10, '2026-05-21', '其他', 2800.00, '关节置换手术费'),
                                                                                               (5, 11, '2026-05-22', '床位费', 90.00, '骨科-701-02床位费'),
                                                                                               (5, 11, '2026-05-22', '诊疗费', 70.00, '杨建军副主任医师巡诊费'),
                                                                                               (5, 11, '2026-05-22', '药品费', 46.70, '头孢克洛+双氯芬酸钠'),

-- 已出院患者陈小明(archive_id=6)费用
                                                                                               (6, 12, '2026-05-10', '床位费', 80.00, '内科-501-02床位费'),
                                                                                               (6, 12, '2026-05-10', '诊疗费', 80.00, '李明轩主任医师巡诊费'),
                                                                                               (6, 12, '2026-05-10', '药品费', 19.30, '阿莫西林+复方甘草片'),
                                                                                               (6, 12, '2026-05-10', '其他', 200.00, '胸部CT检查费'),
                                                                                               (6, 13, '2026-05-12', '床位费', 160.00, '内科-501-02床位费（2天）'),
                                                                                               (6, 13, '2026-05-12', '诊疗费', 60.00, '王秀兰副主任医师巡诊费'),
                                                                                               (6, 13, '2026-05-12', '药品费', 12.80, '阿莫西林'),
                                                                                               (6, 14, '2026-05-16', '床位费', 320.00, '内科-501-02床位费（4天）'),
                                                                                               (6, 14, '2026-05-16', '诊疗费', 80.00, '李明轩主任医师巡诊费'),
                                                                                               (6, 14, '2026-05-16', '药品费', 12.80, '阿莫西林'),

-- 已出院患者刘志伟(archive_id=7)费用
                                                                                               (7, 15, '2026-04-28', '床位费', 100.00, '外科-601-04床位费'),
                                                                                               (7, 15, '2026-04-28', '诊疗费', 65.00, '刘芳副主任医师巡诊费'),
                                                                                               (7, 15, '2026-04-28', '药品费', 37.70, '头孢克洛+甲硝唑'),
                                                                                               (7, 16, '2026-04-30', '床位费', 200.00, '外科-601-04床位费（2天）'),
                                                                                               (7, 16, '2026-04-30', '诊疗费', 65.00, '刘芳副主任医师巡诊费'),
                                                                                               (7, 16, '2026-04-30', '药品费', 32.50, '头孢克洛'),
                                                                                               (7, 16, '2026-04-30', '其他', 3500.00, '腹腔镜胆囊切除术手术费'),
                                                                                               (7, 17, '2026-05-05', '床位费', 500.00, '外科-601-04床位费（5天）'),
                                                                                               (7, 17, '2026-05-05', '诊疗费', 90.00, '陈志强主任医师巡诊费');
