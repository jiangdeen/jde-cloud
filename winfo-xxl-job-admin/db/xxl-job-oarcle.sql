
create table XXL_JOB_GROUP
(
  id           NUMBER(11) not null,
  app_name     VARCHAR2(64) not null,
  title        VARCHAR2(12) not null,
  address_type NUMBER(4) default 0 not null,
  address_list VARCHAR2(2000),
  update_time  DATE
)
;
comment on column XXL_JOB_GROUP.app_name
  is '执行器AppName';
comment on column XXL_JOB_GROUP.title
  is '执行器名称';
comment on column XXL_JOB_GROUP.address_type
  is '执行器地址类型：0=自动注册、1=手动录入';
comment on column XXL_JOB_GROUP.address_list
  is '执行器地址列表，多地址逗号分隔';
alter table XXL_JOB_GROUP
  add constraint PK_XXL_JOB_GROUP primary key (ID);


create table XXL_JOB_LOCK
(
  lock_name VARCHAR2(50) not null
)
;
comment on column XXL_JOB_LOCK.lock_name
  is '锁名称';
alter table XXL_JOB_LOCK
  add constraint PK_XXL_JOB_LOCK primary key (LOCK_NAME);


create table XXL_JOB_LOG
(
  id                        NUMBER(20) not null,
  job_group                 NUMBER(11) not null,
  job_id                    NUMBER(11) not null,
  executor_address          VARCHAR2(255),
  executor_handler          VARCHAR2(255),
  executor_param            VARCHAR2(512),
  executor_sharding_param   VARCHAR2(20),
  executor_fail_retry_count NUMBER(11) default 0 not null,
  trigger_time              DATE,
  trigger_code              NUMBER(11) not null,
  trigger_msg               VARCHAR2(4000),
  handle_time               DATE,
  handle_code               NUMBER(11) not null,
  handle_msg                VARCHAR2(4000),
  alarm_status              NUMBER(4) default 0 not null
)
;
comment on column XXL_JOB_LOG.job_group
  is '执行器主键id';
comment on column XXL_JOB_LOG.job_id
  is '任务，主键ID';
comment on column XXL_JOB_LOG.executor_address
  is '执行器地址，本次执行的地址';
comment on column XXL_JOB_LOG.executor_handler
  is '执行器任务handler';
comment on column XXL_JOB_LOG.executor_param
  is '执行器任务参数';
comment on column XXL_JOB_LOG.executor_sharding_param
  is '执行器任务分片参数，格式如 1/2';
comment on column XXL_JOB_LOG.executor_fail_retry_count
  is '失败重试次数';
comment on column XXL_JOB_LOG.trigger_time
  is '调度-时间';
comment on column XXL_JOB_LOG.trigger_code
  is '调度-结果';
comment on column XXL_JOB_LOG.trigger_msg
  is '调度-日志';
comment on column XXL_JOB_LOG.handle_time
  is '执行-时间';
comment on column XXL_JOB_LOG.handle_code
  is '执行-状态';
comment on column XXL_JOB_LOG.handle_msg
  is '执行-日志';
comment on column XXL_JOB_LOG.alarm_status
  is '告警状态：0-默认、1-无需告警、2-告警成功、3-告警失败';
create index IDX_XXL_JOB_LOG_HANDLE_CODE on XXL_JOB_LOG (HANDLE_CODE);
create index IDX_XXL_JOB_LOG_TRIGGER_TIME on XXL_JOB_LOG (TRIGGER_TIME);
alter table XXL_JOB_LOG
  add constraint PK_XXL_JOB_LOG primary key (ID);


create table XXL_JOB_LOG_REPORT
(
  id            NUMBER(11) not null,
  trigger_day   DATE,
  running_count NUMBER(11) default 0 not null,
  suc_count     NUMBER(11) default 0 not null,
  fail_count    NUMBER(11) default 0 not null,
  update_time   DATE
)
;
comment on column XXL_JOB_LOG_REPORT.trigger_day
  is '调度-时间';
comment on column XXL_JOB_LOG_REPORT.running_count
  is '运行中-日志数量';
comment on column XXL_JOB_LOG_REPORT.suc_count
  is '执行成功-日志数量';
comment on column XXL_JOB_LOG_REPORT.fail_count
  is '执行失败-日志数量';
create index IDX_XXL_JOB_LOG_REPORT_TRD on XXL_JOB_LOG_REPORT (TRIGGER_DAY);
alter table XXL_JOB_LOG_REPORT
  add constraint PK_XXL_JOB_LOG_REPORT primary key (ID);


create table XXL_JOB_REGISTRY
(
  id             NUMBER(11) not null,
  registry_group VARCHAR2(50) not null,
  registry_key   VARCHAR2(255) not null,
  registry_value VARCHAR2(255) not null,
  update_time    DATE
)
;
create index IDX_XXL_JOB_REGISTRY on XXL_JOB_REGISTRY (REGISTRY_GROUP, REGISTRY_KEY, REGISTRY_VALUE);
alter table XXL_JOB_REGISTRY
  add constraint PK_XXL_JOB_REGISTRY primary key (ID);


create table XXL_JOB_USER
(
  id         NUMBER(11) not null,
  username   VARCHAR2(50) not null,
  password   VARCHAR2(50) not null,
  role       NUMBER(4) not null,
  permission VARCHAR2(255)
)
;
comment on column XXL_JOB_USER.username
  is '账号';
comment on column XXL_JOB_USER.password
  is '密码';
comment on column XXL_JOB_USER.role
  is '角色：0-普通用户、1-管理员';
comment on column XXL_JOB_USER.permission
  is '''权限：执行器ID列表，多个逗号分割''';
create unique index IDX_XXL_JOB_USER on XXL_JOB_USER (USERNAME);
alter table XXL_JOB_USER
  add constraint PK_XXL_JOB_USER primary key (ID);

-- Create table
create table XXL_JOB_INFO
(
  id                        NUMBER(11) not null,
  job_group                 NUMBER(11) not null,
  job_desc                  VARCHAR2(255) not null,
  add_time                  DATE,
  update_time               DATE,
  author                    VARCHAR2(64),
  alarm_email               VARCHAR2(255),
  schedule_type             VARCHAR2(50),
  schedule_conf             VARCHAR2(128),
  misfire_strategy          VARCHAR2(50),
  executor_route_strategy   VARCHAR2(50),
  executor_handler          VARCHAR2(255),
  executor_param            VARCHAR2(512),
  executor_block_strategy   VARCHAR2(50),
  executor_timeout          NUMBER(11) default 0,
  executor_fail_retry_count NUMBER(11) default 0,
  glue_type                 VARCHAR2(50) not null,
  glue_source               CLOB,
  glue_remark               VARCHAR2(128),
  glue_updatetime           DATE,
  child_jobid               VARCHAR2(255),
  trigger_status            NUMBER(4) default 0 not null,
  trigger_last_time         NUMBER(13) default 0,
  trigger_next_time         NUMBER(13) default 0
);
-- Add comments to the columns
comment on column XXL_JOB_INFO.id
  is '执行器主键';
comment on column XXL_JOB_INFO.author
  is '作者';
comment on column XXL_JOB_INFO.alarm_email
  is '报警邮件';
comment on column XXL_JOB_INFO.schedule_type
  is '调度类型';
comment on column XXL_JOB_INFO.schedule_conf
  is '调度配置，值含义取决于调度类型';
comment on column XXL_JOB_INFO.misfire_strategy
  is '调度过期策略';
comment on column XXL_JOB_INFO.executor_route_strategy
  is '执行器路由策略';
comment on column XXL_JOB_INFO.executor_handler
  is '执行器任务handler';
comment on column XXL_JOB_INFO.executor_param
  is '执行器任务参数';
comment on column XXL_JOB_INFO.executor_block_strategy
  is '阻塞处任务执行超时时间，单位秒理策略';
comment on column XXL_JOB_INFO.executor_timeout
  is '任务执行超时时间，单位秒';
comment on column XXL_JOB_INFO.executor_fail_retry_count
  is '失败重试次数';
comment on column XXL_JOB_INFO.glue_type
  is 'GLUE类型';
comment on column XXL_JOB_INFO.glue_source
  is 'GLUE源代码';
comment on column XXL_JOB_INFO.glue_remark
  is 'GLUE备注';
comment on column XXL_JOB_INFO.glue_updatetime
  is 'GLUE更新时间';
comment on column XXL_JOB_INFO.child_jobid
  is '子任务ID，多个逗号分隔';
comment on column XXL_JOB_INFO.trigger_status
  is '调度状态：0-停止，1-运行';
comment on column XXL_JOB_INFO.trigger_last_time
  is '上次调度时间';
comment on column XXL_JOB_INFO.trigger_next_time
  is '下次调度时间';
-- Create/Recreate primary, unique and foreign key constraints
alter table XXL_JOB_INFO
  add constraint PK_XXL_JOB_INFO primary key (ID);



  -- Create table
create table XXL_JOB_LOGGLUE
(
  id          NUMBER(11) not null,
  job_id      NUMBER(11) not null,
  glue_type   VARCHAR2(50),
  glue_source CLOB,
  glue_remark VARCHAR2(128) not null,
  add_time    DATE,
  update_time DATE
);
-- Add comments to the columns
comment on column XXL_JOB_LOGGLUE.job_id
  is '任务，主键ID';
comment on column XXL_JOB_LOGGLUE.glue_type
  is 'GLUE类型';
comment on column XXL_JOB_LOGGLUE.glue_source
  is 'GLUE源代码';
comment on column XXL_JOB_LOGGLUE.glue_remark
  is 'GLUE备注';
-- Create/Recreate primary, unique and foreign key constraints
alter table XXL_JOB_LOGGLUE
  add constraint PK_XXL_JOB_LOGGLUE primary key (ID);




insert into xxl_job_info (ID, JOB_GROUP, JOB_DESC, ADD_TIME, UPDATE_TIME, AUTHOR, ALARM_EMAIL, SCHEDULE_TYPE, SCHEDULE_CONF, MISFIRE_STRATEGY, EXECUTOR_ROUTE_STRATEGY, EXECUTOR_HANDLER, EXECUTOR_PARAM, EXECUTOR_BLOCK_STRATEGY, EXECUTOR_TIMEOUT, EXECUTOR_FAIL_RETRY_COUNT, GLUE_TYPE, GLUE_SOURCE, GLUE_REMARK, GLUE_UPDATETIME, CHILD_JOBID, TRIGGER_STATUS, TRIGGER_LAST_TIME, TRIGGER_NEXT_TIME)
values (1, 1, '测试任务1', to_date('03-11-2018 22:21:31', 'dd-mm-yyyy hh24:mi:ss'), to_date('03-11-2018 22:21:31', 'dd-mm-yyyy hh24:mi:ss'), 'XXL', null, 'CRON', '0 0 0 * * ? *', 'DO_NOTHING', 'FIRST', 'demoJobHandler', null, 'SERIAL_EXECUTION', 0, 0, 'BEAN', '<CLOB>', 'GLUE代码初始化', to_date('03-11-2018 22:21:31', 'dd-mm-yyyy hh24:mi:ss'), null, 0, 0, 0);



insert into XXL_JOB_GROUP (id, app_name, title, address_type, address_list, update_time)
values (1, 'xxl-job-executor-sample', '示例执行器', 0, null, to_date('17-05-2021 09:58:57', 'dd-mm-yyyy hh24:mi:ss'));
commit;

insert into XXL_JOB_LOCK (lock_name)
values ('schedule_lock');
commit;

insert into XXL_JOB_LOG_REPORT (id, trigger_day, running_count, suc_count, fail_count, update_time)
values (10000000041, to_date('12-05-2021', 'dd-mm-yyyy'), 0, 0, 0, null);
insert into XXL_JOB_LOG_REPORT (id, trigger_day, running_count, suc_count, fail_count, update_time)
values (10000000042, to_date('11-05-2021', 'dd-mm-yyyy'), 0, 0, 0, null);
insert into XXL_JOB_LOG_REPORT (id, trigger_day, running_count, suc_count, fail_count, update_time)
values (10000000060, to_date('17-05-2021', 'dd-mm-yyyy'), 0, 0, 0, null);
insert into XXL_JOB_LOG_REPORT (id, trigger_day, running_count, suc_count, fail_count, update_time)
values (10000000061, to_date('16-05-2021', 'dd-mm-yyyy'), 0, 0, 0, null);
insert into XXL_JOB_LOG_REPORT (id, trigger_day, running_count, suc_count, fail_count, update_time)
values (10000000062, to_date('15-05-2021', 'dd-mm-yyyy'), 0, 0, 0, null);
insert into XXL_JOB_LOG_REPORT (id, trigger_day, running_count, suc_count, fail_count, update_time)
values (10000000040, to_date('13-05-2021', 'dd-mm-yyyy'), 0, 0, 0, null);
commit;

insert into XXL_JOB_USER (id, username, password, role, permission)
values (1, 'admin', 'e10adc3949ba59abbe56e057f20f883e', 1, null);
commit;



---创建索引

create sequence XXL_JOB_GROUP_ID
minvalue 100000000
maxvalue 999999999
start with 100000020
increment by 1
cache 20;

create sequence XXL_JOB_INFO_ID
minvalue 100000000
maxvalue 999999999
start with 100000020
increment by 1
cache 20;


create sequence XXL_JOB_LOGGLUE_ID
minvalue 100000000
maxvalue 999999999
start with 100000000
increment by 1
cache 20;


create sequence XXL_JOB_LOG_ID
minvalue 100000000
maxvalue 999999999
start with 100000020
increment by 1
cache 20;


create sequence XXL_JOB_LOG_REPORT_ID
minvalue 100000000
maxvalue 999999999
start with 100000000
increment by 1
cache 20;


create sequence XXL_JOB_REGISTRY_ID
minvalue 100000000
maxvalue 999999999
start with 100000020
increment by 1
cache 20;


create sequence XXL_JOB_USER_ID
minvalue 100000000
maxvalue 999999999
start with 100000000
increment by 1
cache 20;
