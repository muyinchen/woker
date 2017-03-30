# MySQL数据恢复－－binlog



MySQL Binary Log也就是常说的bin-log,，是mysql执行改动产生的二进制日志文件，其主要作用有两个：

* 数据恢复 
* 主从数据库。用于slave端执行增删改，保持与master同步。 

## **1.开启binary log功能** 

​     需要修改mysql的配置文件，本篇的实验环境是win7，配置文件为mysql安装目录\MySQL Server 5.1下的my.ini，添加一句log_bin = mysql_bin即可
​     eg： 

```properties
  [mysqld] 

             ...... 

             log_bin = mysql_bin 

             ...... 

```

​   
​       log_bin是生成的bin-log的文件名，后缀则是6位数字的编码，从000001开始，按照上面的配置，生成的文件则为： 
​             mysql_bin.000001 
​             mysql_bin.000002 
​             ...... 

​    配置保存以后重启mysql的服务器，用**show variables like  '%bin%'**查看bin-log是否开启，如图： ![img](http://dl.iteye.com/upload/attachment/0075/0762/fd73b02a-555a-3365-a95b-f68e5192a073.png)

## 2.查看产生的binary log 

   bin-log因为是二进制文件，不能通过记事本等编辑器直接打开查看，mysql提供两种方式查看方式，在介绍之前，我们先对数据库进行一下增删改的操作，否则log里边数据有点空。
   create table bin( id int(10) primary key auto_increment,name varchar(255));(测试前我已经建表)
   insert into bin(name) values ('orange'); 

     1.在客户端中使用  show binlog events in 'mysql_bin.000001'  语句进行查看,为了排序美观，可以在结尾加\G使结果横变纵，此时结尾无需加；语句结束符。
      eg: 

 ```mysql
mysql> show binlog events in 'mysql_bin.000001'\G 

...............省略............... 

********* 3. row ********* 

   Log_name: mysql_bin.000001 

        Pos: 174 

Event_type: Intvar 

  Server_id: 1 

End_log_pos: 202 

      Info: INSERT_ID=2 

********* 4. row ********* 

   Log_name: mysql_bin.000001 

        Pos: 202 

Event_type: Query 

  Server_id: 1 

End_log_pos: 304 

       Info: use test; insert into bin(name) values ('orange') 

********* 5. row ********* 

...............省略............... 

 ```




Log_name:此条log存在那个文件中，从上面可以看出这2条log皆存在与mysql_bin.000001文件中。 
Pos:log在bin-log中的开始位置 
Event_type:log的类型信息 
Server_id:可以查看配置中的server_id,表示log是那个服务器产生 
End_log_pos：log在bin-log中的结束位置 
Info:log的一些备注信息，可以直观的看出进行了什么操作 

2.用mysql自带的工具mysqlbinlog,这是我们就需要知道bin-log存在硬盘的什么位置，win7默认存在C:\ProgramData\MySQL\MySQL Server 5.1\data文件夹下面，如果没有此文件夹，那我们可以通过配置文件中的  datadir="C:/ProgramData/MySQL/MySQL Server 5.1/Data/" 定位，如果还没有，那我就会说“各个系统的搜索功能都做的不错！”。这种查看方式就没那个美观了，如下

 ```mysql

C:\ProgramData\MySQL\MySQL Server 5.1\data>mysqlbinlog mysql_bin.000001 
/*!40019 SET @@session.max_insert_delayed_threads=0*/; 
/*!50003 SET @OLD_COMPLETION_TYPE=@@COMPLETION_TYPE,COMPLETION_TYPE=0*/; 
DELIMITER /*!*/; 

# at 4 
#140215 16:35:56 server id 1  end_log_pos 106   Start: binlog v 4, server v 5.1.51-community-log created 140215 16:35:56 at startup
ROLLBACK/*!*/; 
BINLOG ' 
7Mp7UA8BAAAAZgAAAGoAAAAAAAQANS4xLjUxLWNvbW11bml0eS1sb2cAAAAAAAAAAAAAAAAAAAAA 
AAAAAAAAAAAAAAAAAADsyntQEzgNAAgAEgAEBAQEEgAAUwAEGggAAAAICAgC 
'/*!*/; 
# at 106 
#140215 16:36:51 server id 1  end_log_pos 174   Query   thread_id=2     exec_time=0     error_code=0
SET TIMESTAMP=1350290211/*!*/; 
SET @@session.pseudo_thread_id=2/*!*/; 
SET @@session.foreign_key_checks=1, @@session.sql_auto_is_null=1, @@session.unique_checks=1, @@session.autocommit=1/*!*/;
SET @@session.sql_mode=1344274432/*!*/; 
SET @@session.auto_increment_increment=1, @@session.auto_increment_offset=1/*!*/;
/*!\C utf8 *//*!*/; 
SET @@session.character_set_client=33,@@session.collation_connection=33,@@session.collation_server=33/*!*/;
SET @@session.lc_time_names=0/*!*/; 
SET @@session.collation_database=DEFAULT/*!*/; 
BEGIN 
/*!*/; 
# at 174 
#140215 16:36:51 server id 1  end_log_pos 202   Intvar 
SET INSERT_ID=3/*!*/; 
# at 202 
#140215 16:36:51 server id 1  end_log_pos 309   Query   thread_id=2     exec_time=0     error_code=0
use test/*!*/; 
SET TIMESTAMP=1350290211/*!*/; 
insert into bin(name) values('xishizhaohua') 
/*!*/; 
# at 309 
#140215 16:36:51 server id 1  end_log_pos 336   Xid = 28 
COMMIT/*!*/; 
# at 336 
#140215 16:37:25 server id 1  end_log_pos 379   Rotate to mysql_bin.000002  pos: 4
DELIMITER ; 
# End of log file 
ROLLBACK /* added by mysqlbinlog */; 
/*!50003 SET COMPLETION_TYPE=@OLD_COMPLETION_TYPE*/; 
 ```

虽然排版有点乱，但从图中我们可以得到更多信息，如时间戳，自增的偏移，是否自动提交事务等信息。如下图为从中提取的一部分。

![img](http://dl.iteye.com/upload/attachment/0075/0764/025d529d-a557-351c-a674-32b30f9d8550.png)



## **3.利用bin_log恢复数据(date与position)**

####     1.最长用的就是恢复指定数据端的数据了，可以直接恢复到数据库中： 

​    **mysqlbinlog  --start-date**="2014-02-18 16:30:00" **--stop-date**="2014-02-18 17:00:00" mysql_bin.000001 |mysql -uroot -p123456
​      **亦可导出为sql文件**，再导入至数据库中： 
​      mysqlbinlog  --start-date="2014-02-18 16:30:00" --stop-date="2014-02-18 17:00:00" mysql_bin.000001 >d:\1.sql
​      source d:\1.sql 

###       2.指定开始\结束位置，

​    从上面的查看产生的binary log我们可以知道某个log的开始到结束的位置，我们可以在恢复的过程中指定回复从A位置到B位置的log.需要用下面两个   参数来指定：
   ** --start-positon**="50" //指定从50位置开始 
   ** --stop-postion**="100"//指定到100位置结束 

###    最后介绍几个bin_log的操作： 

   1.查看最后一个bin日志文件是那个，现在位置。

**show master status;**
![img](http://dl.iteye.com/upload/attachment/0075/0766/e4ab9924-8db3-32ad-b719-a7494d61a8e4.png)
  2.启用新的日志文件，一般备份完数据库后执行。

**flush logs;**
![img](http://dl.iteye.com/upload/attachment/0075/0768/1508c1eb-4214-36e8-8581-01b5cde242f6.png)
3.清空现有的所用bin-log 

**reset master;**
 ![img](http://dl.iteye.com/upload/attachment/0075/0770/7e5d7d7f-1bc2-30e9-8d9a-d39a46b687c3.png)

