# 文本处理工具之AWK

## 概述：

​    在之前的文章中，我们介绍过文本处理三剑客的grep、sed，本篇就简要说明下awk的用法。主要是围绕常见的一些用法进行展开，分为以下几个部分：

​    1、awk的基础语法

​    2、awk的进阶语法

​    3、awk的实际效果演示

## 第一章    awk的基础语法

###     1、awk的工作原理

​        每次读取一行，按照指定字符分隔，然后按照指定动作处理

​        第一步：执行BEGIN{action;… }语句块中的语句

​        第二步：从文件或标准输入(stdin)读取一行，然后执行pattern{action;… }语句块，它逐行扫描文件，从第一行到最后一行重复这个过程，直到文件全部被读取完毕。
​        第三步：当读至输入流末尾时，执行END{action;…}语句块

​        BEGIN语句块在awk开始从输入流中读取行之前被执行，这是一个可选的语句块，比如变量初始化、打印输出表格的表头等语句通常可以写在BEGIN语句块中
​        END语句块在awk从输入流中读取完所有的行之后即被执行，比如，打印所有行的分析结果这类信息汇总都是在END语句块中完成，它也是一个可选语句块
​        pattern语句块中的通用命令是最重要的部分，也是可选的。如果没有提供pattern语句块，则默认执行{ print }，即打印每一个读取到的行， awk读取的每一行都会执行该语句块

​        

​        如：BEGIN:所有行执行动作前的准备工作

​         `   awk -F： 'BEGIN{print "Username:\n———-"}$3>=500{print $1}' /etc/passwd`

​                输出结果为：

​                    Username：

​                    ———-

​                    nwc

​            END:所有行处理完成后，最后执行的收尾工作

​          `  awk -F： 'BEGIN{print "Username:\n———-"}$3>=500{print $1}END{print "———-"}' /etc/passwd`

​            输出结果为：

​                Username：

​                ———-

​                nwc

​                ———-

###     2、awk的基本用法：

​        awk [OPTIONS] 'SCRIPT' FILE…

​        awk [OPTIONS] '/PATTERN/{ACTION}' FILE…

​            awk 默认分隔符为空格，连续的多个空格会被当做一个空格,但是cut指定空格为分隔符时，只会认为单个空格为分隔

​        awk [options] -f programfile var=value file…

​        常用选项有：

​           -F  指定字段分隔符

​                可以指定多个分隔符，利用+号表示，例如：

​                ifconfig eth0|grep "inet addr"|awk -F "[ :]+" '{print $4,$6,$8}'，指定空格和：为分隔符

​           -f /PATH/TO/FILE   指明awk处理的程序段的处理语句所在的文件路径(也就是说awk支持将程序段的语句单独写在某个文件中，然后用-f进行指明即可)

​           -v VAR=VALUE  定义变量（变量也可以直接在动作内部直接定义，不用事先声明）          

​                例如：

​                 `   awk -v num1=30 -v num2=20 ‘BEGIN{print num1+num2}’`

​                        结果是50

​               `     awk ‘BENGIN{num1=20;num2=30;print num1+num2}’`

​               

​                   

###     3、awk常用的4种分隔符：

​        输入：

​            行分隔符：默认是\n也就是$符

​            字段分隔符：默认是空格

​        输出：

​            行分隔符：默认是$

​            字段分隔符：默认是空格

​            如：`awk -F ： '/^root\>/{print "Usernam:"$1,"\nShell:"$7}' /etc/passwd`

​                输出为：Username:root

​                        Shell:/bin/bash

​            如：`awk -F ： '/^root\>/{print "Usernam:",$1,"\nShell:",$7}' /etc/passwd`

​                输出为：Username:  root

​                        Shell:  /bin/bash

​            如：`awk -F ： '/^root\>/{print "Usernam:",$1,"Shell:",$7}' /etc/passwd`

​                输出为：Username:  root  Shell:  /bin/bash

​        

​                 可以在action动作内部定义变量：

​                    `awk  'BEGIN{FS=":";OFS="–"}$3>=500{print $1}' /etc/passwd`

​                        这样也实现了指定分隔符为：冒号

​      

###     4、awk的program段中pattern详解

​        <1>空模式：匹配每一行

​        <2>/regular expression/：仅能处理被此处的模式匹配到的行

​        <3>relational expression：关系表达式，结果有“真”有“假”;结果为“真”才会被处理

​            真：结果为非0值，非空字符串

​        <4>地址定界：/PAT1/，/PAT2/  从匹配到PAT1开始，到第一次匹配到PAT2结束，之间所有的行

​            不支持直接给出行号，如果要表示行号范围，可使用NR>=10&&NR<=20方式

​            /PAT/：被PAT匹配到的所有的行

​            ！/PAT/：被PAT匹配到的行之外的所有行

​        关系表达式(expression)：表达式，如大于，小于，等于之类的,支持的符号有：

​            >:大于

​            <:小于

​            >=:大于等于

​            <=:小于等于

​            ==:等于

​            !=:不等于

​            ~:模式匹配,后面模式要用/PATTERN/这种表达式

​        例如：

​            `awk -F: '$3 >= 500 && $3 <= 1000{print $1}' /etc/passwd`

​          `  awk -F： '$7=="/bin/bash"{print $1}' /etc/passwd`

​                也可写成：

​               ` awk -F: '$7~/bash$/{print $1}' /etc/passwd`

###     5、awk的program段中action详解   

​        默认为print，一个action内部有多条语句时，每个语句用;分号分隔

​        <1>print

​            print格式： print item1, item2, …
​            要点：
​                (1) 逗号分隔符
​                (2) 输出的各item可以字符串，也可以是数值；当前记录的字段、变量或awk的表达式
​                (3) 如省略item，相当于print $0
​            示例：

```bash
                awk '{print "hello,awk"}'
                awk –F: '{print}' /etc/passwd
                awk –F: ‘{print “wang”}’ /etc/passwd
                awk –F: ‘{print $1}’ /etc/passwd
                awk –F: ‘{print $0}’ /etc/passwd
                awk –F: ‘{print $1”\t”$3}’ /etc/passwd
                tail –3 /etc/fstab |awk ‘{print $2,$4}
```

​                

​        <2>printf

​            格式化输出： printf “ FORMAT” , item1, item2, …

​            要点：             

​                1)printf与print的最大不同是，printf需要指定格式

​                2)format用于指定后面的每个item的输出格式

​                3)printf语句不会自动打印换行符 \n

​                4)整个format要用双引号“”引起来

​                5)在format里面定义的格式的个数，要与后面的item个数匹配

​            format格式的指示符都以%开头，后跟一个字符，常见的指示符如下：

​                %c  显示字符的ASCII码

​                %d，%i  显示为十进制整数

​                %e，%E  科学计数法显示数值

​                %f  显示浮点数

​                %g，%G  以科学计数法的格式或浮点数的格式显示数值

​                %s  显示字符串

​                %u  无符号整数

​                %%  显示%自身

​            format修饰符：

​                N  显示宽度

​                –  左对齐

​                +  显示数值符号

​                如：%-15s

​            例如：awk -F： ‘{printf “%-15s %i\n”，$1，$3}’ /etc/passwd

​            在print和printf中也支持输出重定向和管道

​                print items > output_file

​                print items >> output_file

​                print items | command

​            例如：awk -F ：‘{printf “%-15s %i\n”，$1,$3 > “/dev/stderr”}’  /etc/passwd

###     6、awk的内置变量：

​        FS 读入行时使用的字段分隔符，默认为空白

​            指定输入分隔符：以指定：为例

​                OPTION段定义-F :

​                语句里面指定FS=":"

​                        awk  'BEGIN{FS=":"}$3>=500{print $1}' /etc/passwd

​                OPTION段定义 -v FS=":"

​        OFS  输出时指定字段分隔符,默认为空白

​            awk  'BEGIN{FS=":";OFS="–"}$3>=500{print $1}' /etc/passwd

​        RS  输入时使用的换行符

​        ORS  输出时使用的换行符

​        NF  当前行的字段个数

​        NR  文件的总行数，如果有多个文件，则是多个文件总行数之和

​        FNR  当前文件中正被处理的条目在文件中是第几行，当有多个文件时，每个文件单独计数

​        ARGV  数组，保存命令本身这个字符串，如：awk ‘{print $0}’ a.txt  b.txt这个命令中，ARGV[0]保存awk 、ARGV[1]保存a.txt、ARGV[2]保存b.txt

​        ARGC  awk命令的参数的个数

​            awk ‘BEGIN{print ARGC}’ a.txt  b.txt结果是3

​        FILENAME   awk命令说出你的文件的名称

​        ENVIRON  当前shell环境变量及其值的关联数组 

​        $0  表示一整行

​        $1，$2。。。表示第几个字段

​        length（$1）  获取第一个字段的字符串长度

## 第二章    awk的进阶语法

​    awk的顺序、选择、循环，因为awk默认会自动的遍历文件中的每一行，故awk选择，循环的对象一般都是被分隔开来的各个字段

###     1、awk中的操作符：

​        算数操作符：

​            -x  负值

​            +x  转换为数值

​            x^y   x的y次方

​            x**y  x的y次方

​            x*y  乘法

​            x/y  除法

​            x+y  加法

​            x-y  减法

​            x%y  取模，余数

​        赋值操作符：

​            =

​            +=

​            -=

​            *=

​            /=

​            %=

​            ^=

​            **=

​            ++

​            —

​            注意：如果某模式为=号，此时用/=/可能有语法错误，应该以/[=]/替代

​        比较操作符：

​            x < y  x小于y为真，否则为假

​            x <= y  x小于等于y为真，否则为假

​            x > y  x大于y为真，否则为假

​            x >= y  x大于等于y为真，否则为假

​            x == y  x等于y为真，不等于为假

​            x != y  x不等于y为真，等于为假

​            x ~ y  x模式匹配y为真，不匹配为假

​            x !~ y  x模式不匹配y为真，匹配为假

​            AA in array  某对象在指定的属组里面为真，不在则为假

​        布尔值：

​            在awk中，0为假，任何非0值都为真（与bash中相反）

​        表达式之间的逻辑关系:

​            &&

​            ||

###     2、三元条件表达式：

​        条件？为真表达式:为假表达式

​            相当于shell中的：

​                if 条件;then

​                    为真表达式

​                else

​                    为假表达式

​                fi

​        例如：

​            a=5

​            b=8

​            a>b？a is max:b is max

​        例如：

​            awk -v num1=20 -v num=30 ‘BEGIN{num1>num2？max=num1：max=num2;print max}’

###     3、awk中action段的if-else语句

​        语法：if （条件）{为真表达式} else {为假表达式}

```bash
 例如：
            awk '{if($3==0){print $1,"admin";}else{print $1,"user"}}' /etc/passwd
            或：
            awk -F: ‘{if（$1=="root"）print $1,"admin";else print $1,"user"}’ /etc/passwd
            awk -F: ‘{if（$1=="root"）printf "%-15:%s\n",$1，"admin"；else printf “%-15s：%s\n”，user}’ /etc/passwd
            awk -F: -v sum=0 ‘{if($3>=500)sum++}END{print sum}’ /etc/passwd
            awk -F: '{if($3>=1000)print $1,$3}' /etc/passwd
            awk -F: '{if($NF=="/bin/bash") print $1}' /etc/passwd
            awk  '{if(NF>5) print $0}' /etc/fstab
            awk -F: '{if($3>=1000) {printf "Common user: %s\n",$1} else{printf "root or Sysuser: %s\n",$1}}' /etc/passwd
            awk -F: '{if($3>=1000) printf "Common user: %s\n",$1;else printf "root or Sysuser: %s\n",$1}' /etc/passwd
            df -h|awk -F% '/^\/dev/{print $1}'|awk '$NF>=80{print $1,$5}’
            awk 'BEGIN{ test=100;if(test>90){print "very good"}else if(test>60){ print "good"}else{print "no pass"}}'
```

​       

###     4、awk中action段的while语句

​        语法：while （条件）{语句1;语句2;…}

```bash
 例如：
            awk -F : ‘{i=1;while（i<=3）{print $i;i++}}’ /etc/passwd
            awk -F : ‘{i=1;while (i<NF){if (lenth($i)>=4) {print $i};i++}}’ /etc/passwd
            awk ‘{i=1;while(i<NF){if($i>=100) print $i;i++}}’ hello.txt
                hello.txt文件的内容为一堆随机数 
            awk '/^[[:space:]]*linux16/{i=1;while(i<=NF){print $i,length($i); i++}}' /etc/grub2.cfg
            awk '/^[[:space:]]*linux16/{i=1;while(i<=NF) {if(length($i)>=10){print $i,length($i)}; i++}}' /etc/grub2.cfg
```

​       

###     5、awk中action段中的do-while语句：至少执行一次循环体，不管条件是否满足

​        语法：do {语句1,语句2,…} while (condition)

```bash
 例如：
            awk -F: ‘{i=1;do{print $i;i++}while(i<=3)}’ /etc/passwd
            awk -F: ‘{i=4;do{print $i;i--}while(i>4)}’ /etc/passwd
```

###     6、awk中action段中的for语句

​        语法：for（i=1;i<=5;i++）{语句1,语句2,…}

```bash
例如：
            awk -F：‘{for（i=1;i<=3;i++）print $i}’ /etc/passwd
            awk -F: ‘{for（i=1;i<=NF;i++）{if(length($i)>=4){print $i}}}’ /etc/passwd
```

​        

​        for循环还可以用来遍历数组元素，awk数组类似是关联数组，也就是下标索引不是数字，而是字符串

​        语法：for（i in array）{语句1,语句2,…}

​          `  awk -F：‘{$NF!~/^$/{BASH[$NF]++}END{for(A in BASH){printf "%15s:%i\n",A,BASH[A]}}’ /etc/passwd`

​            单独看这部分`awk -F：‘{$NF!~/^$/{BASH[$NF]++}`的内容的意义是：

​                当/etc/passwd中最后一个字段的值不为空时，将最后一个字段的值作为BASH数组的下标，然后对应BASH[$NF]的值+1，这样就形成了数组中的元素：

​                BASH[/bin/bash]  值为一共有多少个对应shell的个数

​                最终就相当于统计出来了有多少种shell然后每种shell有多少个用户使用

​                A in BASH在awk中遍历的是数组的下标，而不是具体元素的值

###     7、awk中action段中case语句

​        语法：switch（表达式）{case VALUE or /REGEXP/:语句1,语句2,…default:语句1,语句2,…}

###     8、awk中action段中的break、continue、next语句

​        break和continue：常用语循环或case语句中

​        next语句：

​            提前结束对本行文本的处理，并接着处理下一行

​            例如：

​              `  awk -F：‘{if（$3%2==0）next;print $1,$3}’ /etc/passwd`

​                作用是显示ID号为奇数的用户

​        awk的循环中，基本都是对字段进行循环，continue和break都只是进行下个字段，或者跳出本轮字段的循环，但是有些时候，需要跳出，进行下一行的循环，就需要用到next

###     9、awk中使用数组

​       ` array[index_expression]`

​            index_expression可以使用任意字符串；需要注意的是，如果某数组元素时间不存在，那么在引用其时，awk会自动创建此元素并初始化为空串，因此要判断某数组中是否存储在元素，需要使用inde in array的方式

​        awk中使用的是关联数组，也就是索引下标为字符串，当定义该数组和引用该数组时，索引下标都需要用“”双引号引起来

​        关联数组中的数据不是顺序存放的，要遍历数组，需要遍历数组索引下标来引用

​        要遍历数组中的每一个元素，需要使用如下的特殊结构：

​            for（VAR in array）{语句1,…}

​            其中VAR 用于引用数组下标，而不是元素值

​            array[VAR]  数组中某一元素的值

```bash
 如：
     netstat -tan | awk ‘/^tcp/{++S[$NF]}END{for（a in S）print a,S[a]}’
    作用是：每出现一个被/^tcp/模式匹配到的行，数组S[$NF]就加1，NF为当前匹配到的行的最后一个字段，
            此处用其值作为数组S的元素索引下标

    awk ‘{counts[$1]++};END{for(url in counts)print counts[url],url}’ /var/log/httpd/access_log
    用法与上一个例子一样，用于统计某日志文件中IP地址的访问量
```

​           

​        删除数组变量

​            从关联数组中删除数组索引需要使用delete命令，其格式为：delete array[index]

###     10、awk中的内置函数：

​        <1>split(string,array [fieldsep])

​            功能是：将string表示的字符串以fieldsep为分隔符进行分割，并将分割后的结果保存至array为名的数组中，数组下表为从1开始的序列：

```bash
 例如：
 netstat -tan | awk ‘/:80\>/{split($5,clients,":");IP[clients[1]]++}END{for(i in IP){print IP[i],i}}’|sort -rn|head -50
功能是显示每个客户端IP，及其请求的连接个数

netstat -tan | awk ‘/:80\>/{split($5,clients,":");ip[clients[4]]++}END{for(a in ip){print ip[a],a}}’|sort -rn|head -50
df -lh|awk '!/^File/{split($5,percnt,"%");if(percent[1]>=20){print $1}}'
显示当前系统上磁盘空间占用率超过20%的分区
```

​            

​        <2>length（[string]）

​            功能是：返回string字符串中字符的个数

​        <3>substr（string，start [,length]）

​            功能是：取string字符串中的子串，从start开始，取length个，start从1开始计数

​        <4>sub(r,s,[t]):以r表示的模式来查找t所表示的字符串中的匹配的内容，并将其第一次出现替换为s所表示的字符串

​        <5>gsub(r,s,[t]):以r表示的模式来查找t所表示的字符串中的匹配的内容，并将其所有出现的位置均替换为s所表示的字符串

​        <6>system（"command"）

​            功能是：执行系统command命令，并将结果返回至awk命令

​        <7>systime（）    功能是:取系统当前时间

​        <8>tolower(s)   功能是：将s中的所有字母转为小写

​        <9>toupper（s）   功能是：将s中的所有字母转为大写

​        <10>rand()  返回0和1之间的一个随机数

​            awk 'BEGIN{srand(); for (i=1;i<=10;i++)print int(rand()*100) }'

###     11、补充：time命令测试命令执行消耗时间，判断命令执行效率

```bash
time (awk 'BEGIN{total=0;for(i=0;i<=10000;i++){total+=i;};print total;}')        
time (total=0;for i in {1..10000};do total=$(($total+i));done;echo $total)        
time (for ((i=0;i<=10000;i++));do let total+=i;done;echo $total)
```

## 第三章    awk的实际效果演示

###     1、显示GID小于500的组

```bash
[root@web ~]# awk -F: '{if($3>=500)printf "GroupName: %-10s GID: %-5i \n",$1,$3}' /etc/group
GroupName: nfsnobody  GID: 65534 
GroupName: nnn        GID: 500   
GroupName: natasha    GID: 506   
GroupName: harry      GID: 507   
GroupName: sarah      GID: 508   
GroupName: mysql      GID: 509   
GroupName: sysadmins  GID: 4331  
GroupName: g1         GID: 4333  
GroupName: nwc        GID: 510   
GroupName: bash       GID: 511   
GroupName: testbash   GID: 512   
GroupName: basher     GID: 513   
GroupName: nologin    GID: 514   
GroupName: gdm        GID: 515   
GroupName: testfind   GID: 516   
GroupName: sales      GID: 4336  
GroupName: mw         GID: 3001
```

   

###     2、显示默认shell为nologin的用户

```bash
[root@web ~]# awk -F: '/\/nologin$/{print $1"====="$7}' /etc/passwd
bin=====/sbin/nologin
daemon=====/sbin/nologin
adm=====/sbin/nologin
lp=====/sbin/nologin
mail=====/sbin/nologin
uucp=====/sbin/nologin
operator=====/sbin/nologin
games=====/sbin/nologin
gopher=====/sbin/nologin
ftp=====/sbin/nologin

====================================================
 awk -F: '$7~/nologin$/{print $1}' /etc/passwd
 awk -v FS=":" '/nologin$/{print $1}' /etc/passwd
```

​       

###     3、显示eth0网卡文件的配置信息，注意只显示等号后面的值

```bash
[root@web ~]# awk -F "=" '{print $2}' /etc/sysconfig/network-scripts/ifcfg-eth0 
eth0
10.1.32.68
255.255.0.0
10.1.0.1
```

​    

###     4、显示/etc/sysctl.conf文件中定义的内核参数，只显示名称

```bash
[root@web ~]# awk -F "=" '/^[^#]/{print $1}' /etc/sysctl.conf 
net.ipv4.ip_forward 
net.ipv4.conf.default.rp_filter 
net.ipv4.conf.default.accept_source_route 
kernel.sysrq 
kernel.core_uses_pid 
net.ipv4.tcp_syncookies 
kernel.msgmnb 
kernel.msgmax 
kernel.shmmax 
kernel.shmall 
[root@web ~]#
```

​       

###     5、显示eth0网卡的ip地址，通过ifconfig的命令结果进行过滤

```bash
[root@web ~]# ifconfig eth0
eth0      Link encap:Ethernet  HWaddr 00:0C:29:AA:19:1B  
          inet addr:10.1.32.68  Bcast:10.1.255.255  Mask:255.255.0.0
          inet6 addr: fe80::20c:29ff:feaa:191b/64 Scope:Link
          UP BROADCAST RUNNING MULTICAST  MTU:1500  Metric:1
          RX packets:388706 errors:0 dropped:0 overruns:0 frame:0
          TX packets:3755 errors:0 dropped:0 overruns:0 carrier:0
          collisions:0 txqueuelen:1000 
          RX bytes:45528070 (43.4 MiB)  TX bytes:396651 (387.3 KiB)

[root@web ~]# ifconfig eth0|awk -F "[ :]+" '/inet addr/{print $4}'
10.1.32.68
[root@web ~]#
```

​        

###     6、利用awk打印奇数行、偶数行

```bash
[root@web ~]# cat testfile1 
1
2
3
4
5
6
7
8
9
10
[root@web ~]# awk 'a=!a{print $0}' testfile1 
1
3
5
7
9
[root@web ~]# awk '!(a=!a){print $0}' testfile1 
2
4
6
8
10
[root@web ~]# awk '{if(NR%2==0)print $0}' testfile1 
2
4
6
8
10
[root@web ~]# awk '{if(NR%2==1)print $0}' testfile1 
1
3
5
7
9
[root@web ~]#
```

###     7、统计ps aux命令执行结果中，系统上各状态的进程的个数：

```bash
[root@web ~]# ps aux | awk '/^[^USER]/{stat[$8]++}END{for(i in stat){print i,stat[i]}}'
S< 2
S<sl 1
Ss 18
SN 2
S 110
Ss+ 7
Ssl 2
R+ 1
S+ 1
Sl 1
S<s 1
[root@web ~]#
```

###     8、统计ps aux执行结果中，当前系统上各用户的进程的个数

```bash
[root@localhost lib]# ps aux|awk 'NR>1{COUNT[$1]++}END{for(i in COUNT){print i,COUNT[i]}}'
colord 1
chrony 1
rtkit 1
polkitd 1
dbus 1
nobody 1
gdm 3
libstor+ 1
avahi 2
postfix 2
root 412
[root@localhost lib]#
```

​       

###     9、某成绩单为：利用awk求每个班的平均分   

​        姓名 分数 班级

​          a   10   1

​          b   20   2

​          c   30   1

​          d   40   3

​          e   50   1

​          f   60   2

​          g   70   1

​          h   80   4

​          i   90   1

​          j   100  2

​          k   110  3

​          l   120  2

```bash
[root@web ~]# cat file1
a	10	1
b	20	2
c	30	1
d	40	3
e	50	1
f	60	2
g	70	1
h	80	4
i	90	1
j	100	2
k	110	3
l	120	2
[root@web ~]# awk '{score[$3]+=$2;count[$3]++}END{for(i in score){for(j in count){if(i==j)
{print "The Class: ",i," ","AVG_SCORE_IS: ",score[i]/count[j]}}}}' file1
The Class:  4   AVG_SCORE_IS:  80
The Class:  1   AVG_SCORE_IS:  50
The Class:  2   AVG_SCORE_IS:  75
The Class:  3   AVG_SCORE_IS:  75
[root@web ~]#
```

###     10、统计/etc/fstab文件中每个文件系统类型出现的次数

```bash
[root@web ~]# awk '/^[^#[:space:]]/{type[$3]++}END{for(i in type){print i,type[i]}}' /etc/fstab
devpts 1
swap 1
sysfs 1
proc 1
tmpfs 1
ext4 4
[root@web ~]#
```

​    

###     11、统计/etc/fstab文件中每个单词出现的次数

```bash
[root@web ~]# cat /etc/fstab|tr -c '[a-z]' ' '|awk '{for(i=1;i<=NF;i++){count[$i]++}}END{for(j in count){print j,count[j]}}'
cb 1
pages 1
ed 1
ee 2
info 1
disk 1
add 1
devpts 2
mode 1
tmpfs 2
ext 4
ccessible 1
```
