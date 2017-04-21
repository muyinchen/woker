# mysql常用语句大全

```mysql
/**操作数据库*/  
SHOW DATABASES;  
CREATE DATABASE db;  
SHOW DATABASES;  
DROP DATABASE db;  
  
/**操作表*/  
USE  db;  
SHOW TABLES;  
CREATE TABLE IF NOT EXISTS student(  
    stu_id  INT PRIMARY KEY NOT NULL AUTO_INCREMENT,  
    stu_name VARCHAR(20) NOT NULL DEFAULT '',  
    stu_age  INT   NOT NULL DEFAULT 0,  
    stu_birthday    DATE,  
    stu_salary FLOAT DEFAULT '0.1'  
    #外键在这里修饰  
);  
DESCRIBE  student;  
  
/**对表中的列操作*/  
ALTER TABLE student ADD COLUMN stu_grade INT NOT NULL;  
ALTER TABLE student DROP COLUMN stu_grade;  
  
/**简单的增删改表操作*/  
INSERT INTO student VALUES(1,'张三',23,'1991-01-23','');  
INSERT INTO student (stu_name,stu_age,stu_birthday) VALUES('李四',22,'1992-1-2');  
INSERT INTO student (stu_name,stu_age) VALUES('王五',22);  
UPDATE student SET stu_name='张阳阳' WHERE stu_id=1;  
UPDATE student SET stu_name='张阳阳',stu_age=23 WHERE stu_id=1;  
DELETE FROM  student WHERE stu_id=1;  
  
  
/**将表student的数据导入到表teacher中 
 注意：所要导入的字段类型一定要一致 
*/  
CREATE TABLE IF NOT EXISTS teacher(  
    tea_id  INT PRIMARY KEY NOT NULL AUTO_INCREMENT,  
    tea_name VARCHAR(20) NOT NULL DEFAULT '',  
    tea_age  INT   NOT NULL DEFAULT 0,  
    tea_birthday    DATE,  
    tea_salary FLOAT DEFAULT '0.1'  
    #外键在这里修饰  
);  
INSERT INTO teacher SELECT * FROM student;  
INSERT INTO teacher (tea_name,tea_age) SELECT stu_name,stu_age FROM student WHERE stu_age=22;  
  
  
/**简单查询操作*/  
#Mysql默认的查询方式是ASC(升序);   降序（DESC）  
/**查询格式： 
    SELECT 属性列表 
            FROM 表名和视图列表 
            [WHERE 条件表达式1] 
            [GROUP BY 属性名1] [HAVING 条件表达式2] 
            [ORDER BY 属性名[ASC|DESC]] 
 
*/  
  
/** 
    附: 理解SQL语句执行过程的核心原理:  
    下面是带有WHERE和HAVING的SELECT语句执行过程： 
    1、执行WHERE筛选数据 
    2、执行GROUP BY分组形成中间分组表 
    3、执行WITH ROLLUP/CUBE生成统计分析数据记录并加入中间分组表 
    4、执行HAVING筛选中间分组表 
    5、执行ORDER BY排序 
    知道了执行过程，神秘的GROUP/WHERE/HAVING/WITH...将不再神秘。 
*/  
/**GROUP BY 和 HAVING的理解： 
    GROUP BY 是将查询结果按照某一列或者多列的值分组，值相等的为一组。 
    对查询结果分组的目的是为了细化聚集函数的作用对象。如查询各个地区的人口，只能使用利用（GROUP BY 区域字段）来查询 
    HAVING 是在基于GROUP BY分组之后的数据中再利用所要分组的字段进行条件筛选 
*/  
SELECT * FROM student;  
SELECT * FROM student WHERE stu_age=22;  
SELECT stu_name,stu_age,stu_birthday FROM student WHERE stu_age=22;  
SELECT stu_name AS '学生姓名',stu_age AS '年龄' FROM student ORDER BY stu_age; #用AS将显示字段自定义  
SELECT * FROM student WHERE stu_name IN ('张三','王五'); #用IN限定范围查找  
SELECT * FROM student WHERE stu_age BETWEEN 0 AND 22; #用BETWEEN AND进行查找  
SELECT * FROM student WHERE stu_age>= 23;    # 用比较测试符：（包括=,<>,<,<=,>,>=） 查询  
/**(注意查询条件中有“%”，则说明是部分匹配，而且还有先后信息在里面，即查找以“李”开头的匹配项。 
所以若查询有“李”的所有对象，应该命令：'% 李%';若是第二个字为李，则应为'_李%'或'_李'或'_李_'。)*/  
SELECT * FROM student WHERE stu_name like '%李%';  
SELECT * FROM student WHERE stu_name like '_李%';  
SELECT * FROM student WHERE stu_name IS NOT NULL  # IS[NOT] NULL  
SELECT COUNT(*) FROM student;  
SELECT avg(stu_age) FROM student;  
SELECT max(stu_age) FROM student;  
SELECT min(stu_age) FROM student;  
SELECT * FROM student LIMIT X,Y;  #X代表从哪个下标开始，Y代表从X开始，查询Y个数据  
  
  
/**连接查询**/  
/**连接查询定义:连接查询是将两个或两个以上的表按某个条件连接起来， 
从中选取需要的数据。连接查询是同时查询两个或两个以上的表时使用的。 
当不同的表中存在表示相同意义的字段时，可以通过该字段来连接这几个表。*/  
CREATE TABLE employee(  
    num INT NOT NULL PRIMARY KEY AUTO_INCREMENT,  
    d_id INT NULL,  #外键（department）  
    name VARCHAR(20),  
    age INT ,  
    gender VARCHAR(20),  
    homeaddr VARCHAR(50)  
)  
INSERT INTO employee VALUES  
(NULL, 1001, '张三', 26, '男', '北京市海淀区'),  
(NULL, 1001, '李四', 24, '女', '北京市昌平区'),  
(NULL, 1002, '王五', 25, '男', '湖南长沙市'),  
(NULL, 1004, 'Aric', 15, '男', 'England');  
  
CREATE TABLE department(  
    d_id INT PRIMARY KEY NOT NULL AUTO_INCREMENT,  
    d_name VARCHAR(20) NOT NULL,  
    function VARCHAR(50),  
    address VARCHAR(50)   
)  
CREATE TABLE worker(  
    id INT PRIMARY KEY AUTO_INCREMENT,  
    num INT(10),    #员工编号  
    d_id INT(50),   #部门号（外键）  
    name VARCHAR(20), #姓名  
    gender VARCHAR(10), #性别  
    birthday DATE, #出生日期  
    address VARCHAR(50),  #家庭住址  
    FOREIGN KEY(d_id) REFERENCES department(d_id)  
)  
INSERT INTO department VALUES(1004, '人力资源部', '管理员工的信息', '2号楼3层');  
INSERT INTO employee VALUES(NULL, 1003, '刘花', 28, '女', '吉林省长春市');  
INSERT INTO employee VALUES(NULL, 1006, '王晶', 22, '女', '吉林省通化市');  
/**内连接查询*/  
/**内连接查询:内连接查询可以查询两个或两个以上的表。当两个表中存在表示相同意义的字段时， 
可以通过该字段来连接这两个表。当该字段的值相等时，就查询出该记录*/  
SELECT num AS '雇员ID',name,age,gender,homeaddr,d_name,function,address FROM employee,department  
WHERE employee.d_id=department.d_id;  
  
/**外连接查询*/  
/**外连接查询:外连接查询可以查询两个或两个以上的表。外连接查询也需要通过指定字段来进行连接。 
当该字段取值相等时，可以查询该记录。而且，该字段取值不相等的记录也可以查询出来。 
外连接查询包括左连接查询和右连接查询。*/  
/**外连接查询基本语法： 
        SELECT 属性名列表 
            FROM 表名1(左) LEFT|RIGHT JOIN 表名2(右) 
                ON 表名1.属性名1 = 表名2.属性名2; 
*/  
  
SELECT num, name, employee.d_id,age,gender, d_name, function  
                    FROM employee LEFT JOIN department  
                    ON employee.d_id=department.d_id;  
  
SELECT num, name, employee.d_id,age,gender, d_name, function  
                    FROM employee RIGHT JOIN department  
                    ON employee.d_id=department.d_id;  
  
CREATE TABLE performance(  
    id INT NOT NULL PRIMARY KEY AUTO_INCREMENT,  
    e_num INT(10) NOT NULL UNIQUE,  
    performance FLOAT NOT NULL DEFAULT 0  
) DEFAULT CHARSET=utf8;  
INSERT INTO performance VALUES  
(NULL, 2, 2000),  
(NULL, 1, 100),  
(NULL, 3, 5000),  
(NULL, 5, 8000),  
(NULL, 6, 10000);  
/**多个左右连接查询*/  
SELECT num, name, employee.d_id,age,gender, d_name, function, performance  
                FROM employee  
                LEFT JOIN department   
                ON employee.d_id=department.d_id  
                LEFT JOIN performance  
                ON employee.num=performance.id;  
  
/**复合条件连接查询*/  
SELECT num,name,employee.d_id,age,gender,d_name,function  
            FROM employee,department  
            WHERE employee.d_id=department.d_id  
            AND age>=1  
            ORDER BY age DESC;  
  
  
/**子查询*/  
/**定义：子查询是将一个查询语句嵌套在另一个查询语句中。 
内层查询语句的查询结果，可以为外层查询语句提供查询条件。 
子查询中可能包括IN、NOT IN、ANY、ALL、EXISTS、NOT EXISTS等关键字。 
子查询中还可能包含比较运算符，如'='、'!='、'>'和'<'等。        */  
  
  
SELECT * FROM employee  
            WHERE d_id IN(SELECT d_id FROM department); /**IN关键字子查询*/  
  
CREATE TABLE computer_stu(  
    id INT PRIMARY KEY,  
    name VARCHAR(20),  
    score FLOAT  
) DEFAULT CHARSET=utf8;  
INSERT INTO computer_stu VALUES(1001, 'lILY', 85);  
INSERT INTO computer_stu VALUES(1002, 'Tom', 91);  
INSERT INTO computer_stu VALUES(1003, 'Jim', 87);  
INSERT INTO computer_stu VALUES(1004, 'Aric', 77);  
INSERT INTO computer_stu VALUES(1005, 'Lucy', 65);  
INSERT INTO computer_stu VALUES(1006, 'Andy', 99);  
INSERT INTO computer_stu VALUES(1007, 'Ada', 85);  
INSERT INTO computer_stu VALUES(1008, 'jeck', 70);  
  
CREATE TABLE scholarship(  
    level INT PRIMARY KEY,  
    score INT  
) DEFAULT CHARSET=utf8;  
INSERT INTO scholarship VALUES(1, 90);    
INSERT INTO scholarship VALUES(2, 80);    
INSERT INTO scholarship VALUES(3, 70);  
  
/**在computer_stu表中查询获得一等奖学金的学生的学号、姓名和分数*/  
SELECT com.id,com.name,com.score FROM computer_stu as com   
                WHERE score>=(SELECT score FROM scholarship WHERE level=1);  
  
  
/**在department表中查询哪些部门没有年龄为24岁的员工*/  
SELECT  dep.d_name FROM department as dep   
           WHERE dep.d_id IN(SELECT emp.d_id FROM employee as emp WHERE emp.age!=24);  
  
/**EXISTS关键字的子查询*/  
/**定义:EXISTS关键字表示存在。使用EXISTS关键字时，内层查询语句不返回查询的记录。 
而是返回一个真假值。如果内层查询语句查询到满足条件的记录，就返回一个真值(TRUE)。 
否则，将返回一个假值(FALSE)。当返回的值是真值时，外层查询语句将进行查询。当返回值是假值时， 
外层查询语句不再进行查询或者查询不出任何记录*/  
  
/**如果department表中存在d_id取值为1003的记录，则查询employee表的记录*/  
SELECT * FROM employee   
        WHERE EXISTS(SELECT * FROM department WHERE d_id=1003);  
/**如果department表中存在d_id取值为1003的记录，则查询employee表中age大于24的记录*/  
SELECT * FROM employee as emp   
        WHERE emp.age>0 AND EXISTS (SELECT * FROM department WHERE d_id=1003);  
  
  
/**ANY关键字的子查询*/  
/**定义：ANY关键字表示满足其中任一条件。使用ANY关键字时，只要满足内层查询语句返回的结果中的任何一个， 
就可以通过该条件来执行外层查询语句*/  
  
/**从computer_stu表中查询哪些同学可以获得奖学金。奖学金的信息存储在scholarship表中*/  
SELECT * FROM computer_stu as coms   
        WHERE coms.score>= ANY(SELECT score FROM scholarship) ORDER BY score DESC;  
  
/**ALL关键字的子查询*/  
/**定义:ALL关键字表示满足所有条件。使用ALL关键字时，只有满足内层查询语句返回的所有结果， 
才可以执行外层查询语句*/  
/**查询哪些同学能够获得一等奖学金*/  
SELECT * FROM computer_stu as coms   
            WHERE coms.score>=ALL(SELECT score FROM scholarship)  
  
/**UNION关键字合并查询结果*/  
/**合并查询结构是将多个SELECT语句的查询结果合并到一起。 
因为某种情况下，需要将几个SELECT语句查询出来的结果合并起来显示*/  
/**使用条件：注: 使用 UNION 时 前一个 select column的个数要等于后一个select column的个数*/  
  
UNION和UNION ALL关键字都是将两个结果集合并为一个  
  
UNION在进行表链接后会筛选掉重复的记录，所以在表链接后会对所产生的结果集进行排序运算，删除重复的记录再返回结果;  
UNION ALL只是简单的将两个结果合并后就返回。这样，如果返回的两个结果集中有重复的数据，那么返回的结果集就会包含重复的数据了  
  
  
/***distinct关键字*/  
该关键字是去掉某个属性的重复操作，或者是去重操作后的计数，而且返回值只能是这一个属性的结果集，如果返回结果集里面含有多个字段，将是对多个属性同时起作用(也就达不到这个关键字的初衷),  
详细的看 http://jxtm.jzu.cn/?p=258  
  
/***REPLACE 语句替换数据*/  
/***定义:如果使用INSERT语句插入数据时，在表中已经有相同数据时(指的是PRIMARYT KEY或UNIQUE字段相同数据)会发生错误。 
而REPLACE INTO语句会删除原有相同数据而插入新的数据*/  
/***ps:此种方法目的何在？？？这种需求可以用UPDATE更新*/  
INSERT INTO product VALUES  
(1005, '头疼灵1号', '治疗头疼', 'DD制药厂', '北京市房山区');  
REPLACE INTO product VALUES  
(1005, '头疼灵1号_replace', '治疗头疼', 'DD制药厂', '北京市房山区');  
  
  
/***问题1：如何为自增字段(AUTO_INCREMENT)赋值？？*/  
/***方案：第一种方法是在INSERT语句中不为该字段赋值。第二种方法是在INSERT语句中将该字段赋值为NULL/ 
 
 
 
 
 
/**连接Mysql(小写)*/  
mysql -h(IP) -u(用户名) -p(密码)  
  
/**修改mysql密码 
 前提：进入目录mysqlbin 
*/  
mysqladmin -u用户名 -p旧密码 password 新密码  
  
/**授权新用户管理数据库*/  
  
/*1.授权用户在任意PC管理该数据库中的任意数据信息*/  
grant  update,select,insert,delete on *.* to zyy1@"%" identified by "zyy1";  
grant  all on *.* to zyy2@"%" identified by "zyy2";  
grant  update,select,insert on *.* to zyy3@"%" identified by "zyy3";  
/*2.授权用户只能在特定的PC管理该数据库中指定的db子数据库数据信息*/  
grant  update,select,insert,delete on db.* to zyy4@localhost identified by "zyy4";  
  
/**数据库的备份 
   前提：进入到bin目录下 
*/  
mysqldump -hlocalhost -uroot -p123456 db>C:\\db.sql  
mysqldump -hhostname -uusername -ppassword databasename > backupfile.sql  
/*备份MySQL数据库的命令*/  
mysqldump -hhostname -uusername -ppassword databasename > backupfile.sql  
/*备份MySQL数据库为带删除表的格式 
备份MySQL数据库为带删除表的格式，能够让该备份覆盖已有数据库而不需要手动删除原有数据库。 
*/  
mysqldump -–add-drop-table -uusername -ppassword databasename > backupfile.sql  
/*直接将MySQL数据库压缩备份*/  
mysqldump -hhostname -uusername -ppassword databasename | gzip > backupfile.sql.gz  
/*备份MySQL数据库某个(些)表*/  
mysqldump -hhostname -uusername -ppassword databasename specific_table1 specific_table2 > backupfile.sql  
/*同时备份多个MySQL数据库*/  
mysqldump -hhostname -uusername -ppassword –databases databasename1 databasename2 databasename3 > multibackupfile.sql  
/*仅仅备份数据库结构*/     
mysqldump –no-data –databases databasename1 databasename2 databasename3 > structurebackupfile.sql  
/*备份服务器上所有数据库*/  
mysqldump –all-databases > allbackupfile.sql  
/*还原MySQL数据库的命令*/  
mysql -hhostname -uusername -ppassword databasename < backupfile.sql  
/*还原压缩的MySQL数据库*/  
gunzip < backupfile.sql.gz | mysql -uusername -ppassword databasename  
/*将数据库转移到新服务器*/  
mysqldump -uusername -ppassword databasename | mysql –host=*.*.*.* -C databasename  
  
  
  
  
/***索引:索引是与表或视图关联的磁盘上结构，可以加快从表或视图中检索行的速度。 
索引由数据库表中一列或多列组合而成*/  
索引介绍：http://www.jb51.net/article/49346.htm  
/**查看某个数据库表的索引*/  
SHOW INDEX FROM app_account(表名称)  
/*** 
CREATE TABLE index_tbl_1 ( 
    id INT, 
    name VARCHAR(20), 
    gender BOOLEAN, 
    INDEX(id) 
); 
*/  
  
  
  
  
  
/**视图*/  
/** 
*视图是一种虚拟的表。视图从数据库中的一个或多个表导出来的表。视图还可以从已经存在的视图的基础上定义 
。数据库只存放了视图的定义，而并没有存放视图中的数据。这些数据存放在原来的表中。使用视图查询数据时， 
数据库系统会从原来的表中取出对应的数据。因此，视图中的数据是依赖于原来的表中的数据的。一旦表中的数据 
发生改变，显示在视图中的数据也会发生改变。 
*/  
/**视图作用： 
    1. 使操作简单化 
    2. 增加数据的安全性 
    3. 提高表的逻辑独立性 
*/  
/** 
MySQL中，创建视图是通过SQL语句CREATE VIEW实现的。其语法形式如下 ：  
        CREATE [ALGORITHM={UNDEFINED|MERGE|TEMPTABLE}] 
        VIEW 视图名 [(属性清单)] 
        AS SELECT语句 
        [WITH[CASCADED|LOCAL] CHECK OPTION]; 
ALGORITHM: 
    UNDEFINED, MySQL将自动选择所要使用的算法； 
    MERGE, 表示将使用视图的语句与视图定义合并起来，使用视图定义的某一部分取代语句的对应部分； 
    TEMPTABLE， 表示将视图的结果存入临时表，然后使用临时表执行语句。 
    CASCADED是可选参数，表示更新视图时要满足所有视图和表的条件，该参数默认值；"LOCAL"表示更新视图时要满足 
        该视图本身的定义的条件即可。 
 
*/  
  
/** 
 在单表上创建视图 
    MySQL中可以在单个表上创建视图。在department表上创建一个简单的视图，视图的名称为department_view。 
    实例一：     
        USE db; 
        CREATE VIEW department_view1 
        AS SELECT * FROM db.department; 
        DESC department_view; 
         
    实例二： 
        CREATE VIEW department_view2(name, function,location)  
        AS SELECT d_name, function, address FROM db.department; 
        DESC department_view2; 
 
*/  
  
/** 
MySQL中也可以在两个或两个以上的表上创建视图，也是使用CREATE VIEW语句实现的。 
下面在department表和worker表上创建一个名为worker_view1的视图。 
        实例一： 
            CREATE ALGORITHM=MERGE VIEW  
            worker_view1(name, department, gender, age, address)  
            AS SELECT name, department.d_name, gender, 2011-birthday, worker.address 
            FROM worker,department WHERE worker.d_id=department.d_id 
            WITH LOCAL CHECK OPTION; 
 
*/  
  
/** 
查看视图 
    查看视图是指查看数据库中已存在的视图的定义。查看视图必须要有SHOW VIEW的权限,mysql数据库下的user表 
        中保存着这个信息。查看视图的方法包括DESCRIBE语句、SHOW TABLE STATUS语句、SHOW CREATE VIEW语句和查询 
    information_schema数据库下的views表等 
 
*/  
/**注意：并不是所有的视图都可以更新的。以下这几中情况是不能更新视图的 
    1.视图中包含sum()、count()、max()、min()等函数 
    2.视图中包含union、 union all、distinct、group by、having等关键字。 
    3.常量视图  
    4.视图中的SELECT中包含子查询 
    5.由不可更新的视图导出的视图。 
    6.创建视图时，ALGORITHM为TEMPTABLE类型。 
    7.视图对应的表上存在没有默认值的列，而且该列没有包含在视图里 
  附加：一般视图只在查询的时候使用，如果在更新数据的时候使用视图，则有可能出现如果没有全面考虑在视图中更新数据的限制， 
        可能会造成数据更新失败。 
*/  
  
  
/*操作外键*/  
查看外键：  
SHOW CREATE TABLE subscriber（表名称）  
  
显示如：  
CREATE TABLE `subscriber` (  
  `subscriber_id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '订阅者身份标识',  
  `app_account_id` int(11) NOT NULL COMMENT 'app_account表主键',  
  `subscribe_status` char(1) NOT NULL COMMENT '订阅状态 0 关闭 1开启',  
  `push_url` varchar(256) DEFAULT NULL COMMENT '业务推送地址',  
  `business_flag` char(1) DEFAULT NULL COMMENT '业务标识',  
  `note` varchar(256) DEFAULT NULL COMMENT '备注',  
  `subscriber_name` varchar(64) NOT NULL COMMENT '订阅者名称',  
  PRIMARY KEY (`subscriber_id`),  
  KEY `subscription_info_app_account` (`app_account_id`),  
  CONSTRAINT `subscriber_ibfk_1` FOREIGN KEY (`app_account_id`) REFERENCES `app_account` (`app_account_id`)  
) ENGINE=InnoDB AUTO_INCREMENT=15 DEFAULT CHARSET=utf8 COMMENT='订阅者表'  
  
//注意：在我进行操作外键时，如果不删除之前的数据，就会出现错误，删除掉数据之后，删除或者添加外键没有问题  
删掉该外键：  
  
ALTER TABLE subscriber DROP FOREIGN KEY subscriber_ibfk_1;  #记录  
  
添加外键：  
  
ALTER TABLE subscriber ADD CONSTRAINT FK_ID FOREIGN KEY(app_account_id) REFERENCES app_account(app_account_id)  
  
  
show TRIGGERS  
  
/**触发器*/  
/** 
    触发器(TRIGGER)是由事件来触发某个操作。这些事件包括INSERT语句、UPDATE语句和DELETE语句。 
    当数据库系统执行这些事件时，就会激活触发器执行相应的操作. 
    触发器触发的执行语句可能只有一个，也可能有多个. 
*/  
/**项目中的技巧：如：新闻表中每查看一下新闻，则浏览数将+1，这种情况下使用触发器，是非常方便的*/  
/** 
MySQL中，创建只有一个执行语句的触发器的基本形式如下 ： 
    CREATE TRIGGER 触发器名 BEFORE | AFTER 触发事件 
    ON 表名 FOR EACH ROW 执行语句 
    “触发器名”参数指要创建的触发器的名字。 
     
    “BEFORE”和“AFTER”参数指定了触发器执行的时间。“BEFORE”指在触发事件之前执行触发语句。“AFTER”表示在触发事件之后执行触发语句。 
    “触发事件”参数批触发的条件，其中包括INERT、UPDATE和DELETE。 
    “表名”参数指触发事件操作的表的名称。 
    “FOR EACH ROW”表示任何一条记录上的操作满足触发事件都会触发都会触发该触发器。 
    “执行语句”参数指触发器被触发执行的程序。 
 
*/  
  
/**存储过程*/  
```