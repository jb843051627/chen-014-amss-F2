-- amss 农业机械安全监理与跨区机收作业服务管理 -- schema (chen-014)
-- 列名与基线实体契约（@TableName/@TableField）逐列对齐，改列必须同步实体。
-- 库：chen_014

CREATE TABLE IF NOT EXISTS t_amss_chk_line (
  id bigint NOT NULL COMMENT '主键',
  rule_code varchar(64) DEFAULT NULL COMMENT '检验结论判据代号',
  rule_name varchar(128) DEFAULT NULL COMMENT '判据名称',
  th1_max decimal(12,2) DEFAULT NULL COMMENT '一档不合格项数封顶值',
  th2_max decimal(12,2) DEFAULT NULL COMMENT '二档不合格项数封顶值',
  th3_max decimal(12,2) DEFAULT NULL COMMENT '三档不合格项数封顶值',
  eff_start datetime DEFAULT NULL COMMENT '启用之日',
  eff_end datetime DEFAULT NULL COMMENT '交棒之日(不含)',
  priority int DEFAULT NULL COMMENT '抢档顺位(数值越大越优先)',
  status int DEFAULT NULL COMMENT '判据的情形 0现行 1已停用',
  del_flag int DEFAULT '0' COMMENT '删除标记 0正常 1删除',
  create_by varchar(64) DEFAULT NULL COMMENT '创建者',
  create_time datetime DEFAULT NULL COMMENT '创建时间',
  update_by varchar(64) DEFAULT NULL COMMENT '更新者',
  update_time datetime DEFAULT NULL COMMENT '更新时间',
  remark varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='安全技术检验结论判据线';

CREATE TABLE IF NOT EXISTS t_amss_coop_doc (
  id bigint NOT NULL COMMENT '主键',
  site_no varchar(64) DEFAULT NULL COMMENT '农机服务组织底档字号',
  site_name varchar(128) DEFAULT NULL COMMENT '服务组织全称',
  site_type varchar(32) DEFAULT NULL COMMENT '组织类别类',
  road_name varchar(128) DEFAULT NULL COMMENT '管辖监理站(省—市—县)',
  th1_max decimal(12,2) DEFAULT NULL COMMENT '单卡在队面积一档上限(亩)',
  th2_max decimal(12,2) DEFAULT NULL COMMENT '二档上限(亩)',
  th3_max decimal(12,2) DEFAULT NULL COMMENT '三档上限(亩)',
  status int DEFAULT NULL COMMENT '底档情形 0在用 1已收回',
  del_flag int DEFAULT '0' COMMENT '删除标记 0正常 1删除',
  create_by varchar(64) DEFAULT NULL COMMENT '创建者',
  create_time datetime DEFAULT NULL COMMENT '创建时间',
  update_by varchar(64) DEFAULT NULL COMMENT '更新者',
  update_time datetime DEFAULT NULL COMMENT '更新时间',
  remark varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='农机服务组织底档';

CREATE TABLE IF NOT EXISTS t_amss_diff_row (
  id bigint NOT NULL COMMENT '主键',
  batch_no varchar(64) DEFAULT NULL COMMENT '通行费免收对账批次号',
  row_no int DEFAULT NULL COMMENT '原清单行次',
  item_code varchar(64) DEFAULT NULL COMMENT '差异项代号',
  qty decimal(12,2) DEFAULT NULL COMMENT '本行争议差额(元)',
  status int DEFAULT NULL COMMENT '行进展 0待核 1已核销 2挂账',
  del_flag int DEFAULT '0' COMMENT '删除标记 0正常 1删除',
  create_by varchar(64) DEFAULT NULL COMMENT '创建者',
  create_time datetime DEFAULT NULL COMMENT '创建时间',
  update_by varchar(64) DEFAULT NULL COMMENT '更新者',
  update_time datetime DEFAULT NULL COMMENT '更新时间',
  remark varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='通行费免收与作业证发放对账行';

CREATE TABLE IF NOT EXISTS t_amss_duty_task (
  id bigint NOT NULL COMMENT '主键',
  item_no varchar(64) DEFAULT NULL COMMENT '检修复查与上门服务派单事项号',
  due_at datetime DEFAULT NULL COMMENT '本项应当到场那天的最后一刻',
  amount decimal(12,2) DEFAULT NULL COMMENT '单条提前派单的自然日数',
  content varchar(255) DEFAULT NULL COMMENT '派单事由与受派班组记要',
  status int DEFAULT NULL COMMENT '条目情形 0待派 1已派 2派不出去',
  del_flag int DEFAULT '0' COMMENT '删除标记 0正常 1删除',
  create_by varchar(64) DEFAULT NULL COMMENT '创建者',
  create_time datetime DEFAULT NULL COMMENT '创建时间',
  update_by varchar(64) DEFAULT NULL COMMENT '更新者',
  update_time datetime DEFAULT NULL COMMENT '更新时间',
  remark varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='三夏三秋检修复查与上门服务派单条目';

CREATE TABLE IF NOT EXISTS t_amss_out_card (
  id bigint NOT NULL COMMENT '主键',
  biz_no varchar(64) DEFAULT NULL COMMENT '变型拖拉机牌证缴回事务卡号',
  stage int DEFAULT NULL COMMENT '当前缴回段 0..3（收牌/验车/清账/了结）',
  status int DEFAULT NULL COMMENT '卡的落定 0未起 1在办 2已了结',
  content varchar(255) DEFAULT NULL COMMENT '缴回记事',
  last_action varchar(64) DEFAULT NULL COMMENT '最近一次过口动作',
  del_flag int DEFAULT '0' COMMENT '删除标记 0正常 1删除',
  create_by varchar(64) DEFAULT NULL COMMENT '创建者',
  create_time datetime DEFAULT NULL COMMENT '创建时间',
  update_by varchar(64) DEFAULT NULL COMMENT '更新者',
  update_time datetime DEFAULT NULL COMMENT '更新时间',
  remark varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='变型拖拉机牌证缴回事务卡';

CREATE TABLE IF NOT EXISTS t_amss_pass_bill (
  id bigint NOT NULL COMMENT '主键',
  bill_no varchar(64) DEFAULT NULL COMMENT '跨区作业证核准单号',
  node_no int DEFAULT NULL COMMENT '当前所在层 0受理 1核验 2复核 3用章',
  sign_mode int DEFAULT NULL COMMENT '同层核准办法 0任一人 1两名点齐',
  need_count int DEFAULT NULL COMMENT '本层应签人数（随层配置，不由页面填）',
  sign_count int DEFAULT NULL COMMENT '本层已签人数（一律由签字流水回算后写回）',
  owner_name varchar(64) DEFAULT NULL COMMENT '机主姓名（受理四要件之一）',
  drive_code varchar(32) DEFAULT NULL COMMENT '准驾代号类别（受理四要件之一）',
  plate_no varchar(64) DEFAULT NULL COMMENT '本机在册牌证字号（受理四要件之一，核验只对字号）',
  cert_text varchar(500) DEFAULT NULL COMMENT '拟出证文（受理四要件之一）',
  dest_province varchar(64) DEFAULT NULL COMMENT '作业合同指向的目的地省（随证文外发）',
  valid_from datetime DEFAULT NULL COMMENT '作业证起日（随证文外发）',
  valid_to datetime DEFAULT NULL COMMENT '作业证讫日（随证文外发）',
  team_card_no varchar(64) DEFAULT NULL COMMENT '申报的跨区作业队在队名册卡号',
  issue_year int DEFAULT NULL COMMENT '申领年度（年检口径与当年重复抄发拦截）',
  round_no int DEFAULT '1' COMMENT '核准轮次 首轮1，市局每打回一次加一（勾销与后补不同轮，不摆在一处）',
  inspect_block int DEFAULT '0' COMMENT '核验压死标记 0正常 1年检未做或未过一次压死再齐也走不动',
  sealed int DEFAULT '0' COMMENT '是否已落本站公章 0未盖 1已落（落后各栏封死）',
  cert_no varchar(64) DEFAULT NULL COMMENT '已转对外证文号（空即尚未出证）',
  status int DEFAULT NULL COMMENT '报批情形 0在核 1已核讫 2已打回 3不予出证(终局)',
  del_flag int DEFAULT '0' COMMENT '删除标记 0正常 1删除',
  create_by varchar(64) DEFAULT NULL COMMENT '创建者',
  create_time datetime DEFAULT NULL COMMENT '创建时间',
  update_by varchar(64) DEFAULT NULL COMMENT '更新者',
  update_time datetime DEFAULT NULL COMMENT '更新时间',
  remark varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='跨区作业证逐级核准单';

-- 各道关口逐笔落写的签字名目（第三道两名并排签，各记各的，永不并笔；打回只勾销不删行）
CREATE TABLE IF NOT EXISTS t_amss_pass_sign (
  id bigint NOT NULL COMMENT '主键',
  bill_id bigint DEFAULT NULL COMMENT '所属核准单 t_amss_pass_bill.id',
  round_no int DEFAULT NULL COMMENT '落写时所在轮次（打回后新一轮另攒，旧轮整批勾销）',
  node_no int DEFAULT NULL COMMENT '落写的关口 0受理 1核验 2复核 3用章',
  seat_no int DEFAULT '0' COMMENT '关口内位次 0单人关 1名册合同位 2牌证证文合龙位',
  seat_name varchar(64) DEFAULT NULL COMMENT '本笔名目（受理/字号核验/名册合同核验/合龙核验/用印）',
  sign_kind int DEFAULT '0' COMMENT '名目类别 0核准签字 1不予出证',
  approver varchar(64) DEFAULT NULL COMMENT '经人手签（同一人顶两个位次按替签论，不计）',
  opinion varchar(500) DEFAULT NULL COMMENT '判语',
  void_flag int DEFAULT '0' COMMENT '勾销标记 0在账 1市局打回时尽数勾销（保留行，不与后补同列）',
  del_flag int DEFAULT '0' COMMENT '删除标记 0正常 1删除',
  create_by varchar(64) DEFAULT NULL COMMENT '创建者',
  create_time datetime DEFAULT NULL COMMENT '创建时间',
  update_by varchar(64) DEFAULT NULL COMMENT '更新者',
  update_time datetime DEFAULT NULL COMMENT '更新时间',
  remark varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (id),
  KEY idx_pass_sign_bill (bill_id, round_no, node_no, void_flag)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='跨区作业证核准逐道签字流水';

-- 四道走满后由核准结论一键转出的对外证文（机主/牌证/目的省/起讫日同件带出，不经第二个人外抄）
CREATE TABLE IF NOT EXISTS t_amss_pass_cert (
  id bigint NOT NULL COMMENT '主键',
  bill_id bigint DEFAULT NULL COMMENT '来源核准单 t_amss_pass_bill.id',
  cert_no varchar(64) DEFAULT NULL COMMENT '跨区作业证证号（省级统一印制字号）',
  owner_name varchar(64) DEFAULT NULL COMMENT '机主姓名（自核准单带过）',
  plate_no varchar(64) DEFAULT NULL COMMENT '在册牌证字号（自核准单带过）',
  dest_province varchar(64) DEFAULT NULL COMMENT '目的地省（自核准单带过）',
  valid_from datetime DEFAULT NULL COMMENT '作业证起日（自核准单带过）',
  valid_to datetime DEFAULT NULL COMMENT '作业证讫日（自核准单带过）',
  cert_text varchar(500) DEFAULT NULL COMMENT '证文全文（自核准单拟文稿带过）',
  issue_year int DEFAULT NULL COMMENT '发证年度（同机主同年度第二张当场拦）',
  issue_time datetime DEFAULT NULL COMMENT '出证时刻',
  del_flag int DEFAULT '0' COMMENT '删除标记 0正常 1删除',
  create_by varchar(64) DEFAULT NULL COMMENT '创建者',
  create_time datetime DEFAULT NULL COMMENT '创建时间',
  update_by varchar(64) DEFAULT NULL COMMENT '更新者',
  update_time datetime DEFAULT NULL COMMENT '更新时间',
  remark varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (id),
  UNIQUE KEY uk_pass_cert_no (cert_no),
  KEY idx_pass_cert_owner_year (owner_name, issue_year)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='跨区作业证对外证文存根';

-- 农机在册牌证底档（第二道核验只对字号不对人，并卡年度安全技术检验）
CREATE TABLE IF NOT EXISTS t_amss_mach_reg (
  id bigint NOT NULL COMMENT '主键',
  plate_no varchar(64) DEFAULT NULL COMMENT '在册牌证字号（核验唯一对得上的东西）',
  reg_owner varchar(64) DEFAULT NULL COMMENT '在册登记机主（姓名一模一样也不替字号顶用）',
  machine_model varchar(128) DEFAULT NULL COMMENT '机型名称',
  drive_code varchar(32) DEFAULT NULL COMMENT '准驾代号类别',
  inspect_year int DEFAULT NULL COMMENT '最近一次安全技术检验年度',
  inspect_result int DEFAULT NULL COMMENT '年检情形 0未做 1合格 2不合格',
  status int DEFAULT '0' COMMENT '底档情形 0在册 1已注销',
  del_flag int DEFAULT '0' COMMENT '删除标记 0正常 1删除',
  create_by varchar(64) DEFAULT NULL COMMENT '创建者',
  create_time datetime DEFAULT NULL COMMENT '创建时间',
  update_by varchar(64) DEFAULT NULL COMMENT '更新者',
  update_time datetime DEFAULT NULL COMMENT '更新时间',
  remark varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (id),
  KEY idx_mach_reg_plate (plate_no)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='农机在册牌证与年度检验底档';

CREATE TABLE IF NOT EXISTS t_amss_team_card (
  id bigint NOT NULL COMMENT '主键',
  bill_no varchar(64) DEFAULT NULL COMMENT '在队机手建档卡号',
  node_no int DEFAULT NULL COMMENT '本卡名录版本号',
  site_id int DEFAULT NULL COMMENT '所属农机服务组织底档',
  site_no varchar(64) DEFAULT NULL COMMENT '所属服务组织底档字号',
  qty decimal(12,2) DEFAULT NULL COMMENT '本卡合并后在队服务面积(亩)',
  fine_amt decimal(12,2) DEFAULT NULL COMMENT '本年度新进队服务面积(亩)',
  content varchar(255) DEFAULT NULL COMMENT '核验串（随名录版本走）',
  grade_level int DEFAULT NULL COMMENT '在队服务覆盖程度',
  status int DEFAULT NULL COMMENT '进展 0待核对 1已核对 2已压死',
  del_flag int DEFAULT '0' COMMENT '删除标记 0正常 1删除',
  create_by varchar(64) DEFAULT NULL COMMENT '创建者',
  create_time datetime DEFAULT NULL COMMENT '创建时间',
  update_by varchar(64) DEFAULT NULL COMMENT '更新者',
  update_time datetime DEFAULT NULL COMMENT '更新时间',
  remark varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='跨区作业队在机手名录建档卡';

-- 初始底档数据（跨区作业队在机手名录）
-- 单卡在队面积上限：80 / 200 / 500 亩
INSERT IGNORE INTO t_amss_coop_doc (id, site_no, site_name, site_type, road_name, th1_max, th2_max, th3_max, status, del_flag, create_by, create_time)
VALUES (0, 'ZZ00', '江原县农机服务组织底档（临川县支农农机专业合作社）', '农机合作社', '江原省—江原市—临川县', 80.00, 200.00, 500.00, 0, 0, 'seed', NOW()),
       (1, 'ZZ01', '江原县农机服务组织底档（临川县丰收跨区作业队，已收回）', '跨区作业队', '江原省—江原市—临川县', 80.00, 200.00, 500.00, 1, 0, 'seed', NOW());

-- 在册牌证底档：09 字号当年检验合格；10 字号当年未检（第二道压死样例）
INSERT IGNORE INTO t_amss_mach_reg (id, plate_no, reg_owner, machine_model, drive_code, inspect_year, inspect_result, status, del_flag, create_by, create_time)
VALUES (100, '赣F·09联收', '周麦收', '4LZ-6.0 联合收割机', 'R', 2026, 1, 0, 0, 'seed', NOW()),
       (101, '赣F·10联收', '吴耕田', '4LZ-5.0 联合收割机', 'R', 2025, 0, 0, 0, 'seed', NOW());

-- 在队机手名录：JD2026001 当年已核对在册；JD2026002 已压死（复核名册位应判不过）
INSERT IGNORE INTO t_amss_team_card (id, bill_no, node_no, site_id, site_no, qty, fine_amt, content, grade_level, status, del_flag, create_by, create_time)
VALUES (200, 'JD2026001', 1, 0, 'ZZ00', 320.00, 120.00, '临川县丰收跨区作业队2026年三夏在队名录·目的地省：河南省', 2, 1, 0, 'seed', NOW()),
       (201, 'JD2026002', 1, 0, 'ZZ00', 60.00, 60.00, '名录核验未过', 0, 2, 0, 'seed', NOW());


-- 跨区作业证核准菜单与按钮权限（授给管理员角色 488243256161730560）
INSERT IGNORE INTO t_sys_permission (id, name, descripion, url, is_blank, pid, perms, type, icon, order_num, visible, create_by, create_time)
VALUES (9000, '跨区作业证核准', '逐级核准台账', '/amssPassBill/view', 0, 0, 'amssPassBill:view', 1, 'layui-icon layui-icon-vercode', 20, 0, 'seed', NOW()),
       (9001, '核准台账', '台账分页', '/amssPassBill/list', 0, 9000, 'amssPassBill:list', 2, '', NULL, 0, 'seed', NOW()),
       (9002, '收件受理', '受理四要件', '/amssPassBill/receive', 0, 9000, 'amssPassBill:receive', 2, '', NULL, 0, 'seed', NOW()),
       (9003, '签字核准', '单人关与复核并签', '/amssPassBill/approve', 0, 9000, 'amssPassBill:approve', 2, '', NULL, 0, 'seed', NOW()),
       (9004, '不予出证', '本关终止', '/amssPassBill/reject', 0, 9000, 'amssPassBill:reject', 2, '', NULL, 0, 'seed', NOW()),
       (9005, '市局打回', '勾销重核', '/amssPassBill/rollback', 0, 9000, 'amssPassBill:rollback', 2, '', NULL, 0, 'seed', NOW()),
       (9006, '一键出证', '核准结论转对外证文', '/amssPassBill/issue', 0, 9000, 'amssPassBill:issue', 2, '', NULL, 0, 'seed', NOW());
INSERT IGNORE INTO t_sys_permission_role (id, role_id, permission_id, create_by, create_time)
SELECT 9100 + n.n, 488243256161730560, 9000 + n.n, 'seed', NOW()
FROM (SELECT 0 n UNION SELECT 1 UNION SELECT 2 UNION SELECT 3 UNION SELECT 4 UNION SELECT 5 UNION SELECT 6) n;
