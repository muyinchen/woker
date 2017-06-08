# 网络管理实战(子网划分、单网卡多IP、多网卡单IP、Linux路由实现)

  1、某公司申请到一个C 类IP 地址，但要连接6 个的子公司，最大的一个子 公司有26 台计算机，每个子公司在一个网段中，则子网掩码应设为？ 

​        分析过程：C类地址标准的掩码为24位，因为有6个子公司，故至少要分成6个子网，因此最少要借3位，才能实现划分至少6个子网，故掩码为27位，每个网段可容纳的主机数是2^5-2，满足最大子公司26台计算机的要求，故掩码为：255.255.255.224

​    2、一家集团公司有12家子公司，每家子公司又有4个部门。上级给出一个172.16.0.0/16的网段，让给每家子公司以及子公司的部门分配网段。

​        分析过程，12家子公司，故要划分12个子网，最少要向主机位借4位，才能满足12个子网的需求；每家子公司4个部分，故在每家子公司的子网下再划分子网，又需在每个子公司下再借2位当做网络位，故最终结果是：

​    第一家子公司，其总体的网络号是：172.16.0.0/20

​        每个部门的网络号为：

​            172.16.0.0/22

​            172.16.4.0/22

​            172.16.8.0/22

​            172.16.12.0/22

> 第二家子公司，其总体的网络号是：172.16.16.0/20
>
> ​    每个部分的网络号为：
>
> ​        172.16.16.0/22
>
> ​        172.16.20.0/22
>
> ​        172.16.24.0/22
>
> ​        172.16.28.0/22
>
> 依次类推，剩余子公司的网络号为：（在每个子公司网络下再借2位进行子网划分，部门网络位为22位）
>
> ​    172.16.32.0/20
>
> ​    172.16.48.0/20
>
> ​    172.16.64.0/20
>
> ​    172.16.80.0/20
>
> ​    172.16.96.0/20
>
> ​    172.16.112.0/20
>
> ​    172.16.128.0/20
>
> ​    172.16.144.0/20
>
> ​    172.16.160.0/20
>
> ​    172.16.176.0/20

​    3、某集团公司给下属子公司甲分配了一段IP地址192.168.5.0/24，现在甲公司有两层办公楼（1楼和2楼），统一从1楼的路由器上公网。1楼有100台电脑联网，2楼有53台电脑联网。如果你是该公司的网管，你该怎么去规划这个IP？

​    分析过程：两层，分为两个子网，故需向主机位部分借1位作为网络位，故子网掩码为25位，这样每个子网可容纳2^7-2个主机，也就是128个主机，但考虑到二层只需53台上网，而1楼为公网出口，故考虑将第二个子网再进行划分，这样，就形成了以下划分方式：

​        192.168.5.0/25  一楼

​        192.168.5.128/26  一楼

​        192.168.5.192/26  二楼

​    4、虚拟网卡实现一个网卡多个地址

```bash
[root@localhost ~]# ifconfig           //保证当前只有一个网卡
eth0      Link encap:Ethernet  HWaddr 00:0C:29:AA:19:1B  
          inet addr:10.1.32.68  Bcast:10.1.255.255  Mask:255.255.0.0
          inet6 addr: fe80::20c:29ff:feaa:191b/64 Scope:Link
          UP BROADCAST RUNNING MULTICAST  MTU:1500  Metric:1
          RX packets:12777 errors:0 dropped:0 overruns:0 frame:0
          TX packets:299 errors:0 dropped:0 overruns:0 carrier:0
          collisions:0 txqueuelen:1000 
          RX bytes:1149713 (1.0 MiB)  TX bytes:34881 (34.0 KiB)

lo        Link encap:Local Loopback  
          inet addr:127.0.0.1  Mask:255.0.0.0
          inet6 addr: ::1/128 Scope:Host
          UP LOOPBACK RUNNING  MTU:65536  Metric:1
          RX packets:49 errors:0 dropped:0 overruns:0 frame:0
          TX packets:49 errors:0 dropped:0 overruns:0 carrier:0
          collisions:0 txqueuelen:0 
          RX bytes:5430 (5.3 KiB)  TX bytes:5430 (5.3 KiB)

[root@localhost ~]# cd /etc/sysconfig/network-scripts/
[root@localhost network-scripts]# ls
ifcfg-eth0   ifdown-eth   ifdown-isdn    ifdown-sit     ifup-bnep  ifup-ipv6   ifup-post    ifup-tunnel       network-functions
ifcfg-lo     ifdown-ib    ifdown-post    ifdown-tunnel  ifup-eth   ifup-isdn   ifup-ppp     ifup-wireless     network-functions-ipv6
ifdown       ifdown-ippp  ifdown-ppp     ifup           ifup-ib    ifup-plip   ifup-routes  init.ipv6-global
ifdown-bnep  ifdown-ipv6  ifdown-routes  ifup-aliases   ifup-ippp  ifup-plusb  ifup-sit     net.hotplug
[root@localhost network-scripts]# vim ifcfg-eth0:10    //新建现有网卡的子接口配置文件，文件名为 主接口:子接口  ，可建多个子接口
[root@localhost network-scripts]# cat ifcfg-eth0:10          //子接口配置文件详细内容为
DEVICE=eth0:10
IPADDR=10.1.32.168
NETMASK=255.255.0.0
GATEWAY=10.1.0.1
DNS1=8.8.8.8
ONPARENT=yes          //表示随着主接口的启动而启动，如果不定义此项，可能造成重启后，子接口无法正常启动
[root@localhost network-scripts]# service network restart      //创建完配置文件后重启网络服务
正在关闭接口 eth0：                                        [确定]
关闭环回接口：                                             [确定]
弹出环回接口：                                             [确定]
弹出界面 eth0： Determining if ip address 10.1.32.68 is already in use for device eth0...
Determining if ip address 10.1.32.168 is already in use for device eth0...
                                                           [确定]
[root@localhost network-scripts]# ifconfig         //查看网卡配置是否成功
eth0      Link encap:Ethernet  HWaddr 00:0C:29:AA:19:1B  
          inet addr:10.1.32.68  Bcast:10.1.255.255  Mask:255.255.0.0
          inet6 addr: fe80::20c:29ff:feaa:191b/64 Scope:Link
          UP BROADCAST RUNNING MULTICAST  MTU:1500  Metric:1
          RX packets:15262 errors:0 dropped:0 overruns:0 frame:0
          TX packets:590 errors:0 dropped:0 overruns:0 carrier:0
          collisions:0 txqueuelen:1000 
          RX bytes:1377544 (1.3 MiB)  TX bytes:78739 (76.8 KiB)

eth0:10   Link encap:Ethernet  HWaddr 00:0C:29:AA:19:1B  
          inet addr:10.1.32.168  Bcast:10.1.255.255  Mask:255.255.0.0
          UP BROADCAST RUNNING MULTICAST  MTU:1500  Metric:1

lo        Link encap:Local Loopback  
          inet addr:127.0.0.1  Mask:255.0.0.0
          inet6 addr: ::1/128 Scope:Host
          UP LOOPBACK RUNNING  MTU:65536  Metric:1
          RX packets:49 errors:0 dropped:0 overruns:0 frame:0
          TX packets:49 errors:0 dropped:0 overruns:0 carrier:0
          collisions:0 txqueuelen:0 
          RX bytes:5430 (5.3 KiB)  TX bytes:5430 (5.3 KiB)

[root@localhost network-scripts]#
```

​    ping两个地址，验证是否配置成功：

​    ![blob.png](http://www.178linux.com/ueditor/php/upload/image/20160902/1472821787684507.png)

​    5、多网卡bond，mode1，实现多个网卡绑定同一个IP

​    centos6上利用bond技术实现多网卡绑定同一个IP

​    就是将多块网卡绑定同一IP地址对外提供服务，可以实现高可用或者负载均衡。当然，直接给两块网卡设置同一IP地址是不可能的。通过bonding，虚拟一块网卡对外提供连接，物理网卡的被修改为相同的MAC地址。

​    bond的工作模式：

​        Mode 0 (balance-rr)
​            轮转（ Round-robin）策略：从头到尾顺序的在每一个slave接口上面发送数据包。本模式提供负载均衡和容错的能力
​        Mode 1 (active-backup)
​            活动-备份（主备）策略：在绑定中，只有一个slave被激活。当且仅当活动的slave接口失败时才会激活其他slave。为了避免交换机发生混乱此时绑定的MAC地址只有一个外部端口上可见
​        Mode 3 (broadcast)
​            广播策略：在所有的slave接口上传送所有的报文，本模式提供容错能力。

​    查看bond的状态：cat /proc/net/bonding/bond接口号

​    bond的实现（mode1）：

​        <1>添加一块物理网卡

​        ![1.png](http://www.178linux.com/ueditor/php/upload/image/20160902/1472822895924796.png)

​        ![2.png](http://www.178linux.com/ueditor/php/upload/image/20160902/1472822903704554.png)

​        ![3.png](http://www.178linux.com/ueditor/php/upload/image/20160902/1472822910760899.png)

​        ![4.png](http://www.178linux.com/ueditor/php/upload/image/20160902/1472822919778480.png)

​        ![5.png](http://www.178linux.com/ueditor/php/upload/image/20160902/1472822925644696.png)

​        ![6.png](http://www.178linux.com/ueditor/php/upload/image/20160902/1472822932563956.png)

​        <2>在/etc/sysconfig/network-scripts/目录下，创建bond接口文件，文件名为 bond数字；对新增的物理网卡添加其配置文件，修改原有物理网卡的配置文件

​        ![1.png](http://www.178linux.com/ueditor/php/upload/image/20160902/1472823703257801.png)

​       

​        <3>重启网络服务

​        ![3.png](http://www.178linux.com/ueditor/php/upload/image/20160902/1472824130681788.png)

​        <4>查看bond状态，人为停掉一块网卡，验证是否成功(注意不能ifdown从逻辑层面禁用，而是应该模拟网卡物理损坏)

​        ![222.png](http://www.178linux.com/ueditor/php/upload/image/20160902/1472824901389133.png)

​        ![333.png](http://www.178linux.com/ueditor/php/upload/image/20160902/1472824917908960.png)

​        ![555.png](http://www.178linux.com/ueditor/php/upload/image/20160902/1472824936411801.png)

​        ![444.png](http://www.178linux.com/ueditor/php/upload/image/20160902/1472824966430371.png)

​        ![777.png](http://www.178linux.com/ueditor/php/upload/image/20160902/1472825106318517.png)

​    6、Linux路由实验

​        实验环境：虚拟机1模拟路由器R1、虚拟机2模拟路由器R2

​        ![blob.png](http://www.178linux.com/ueditor/php/upload/image/20160904/1472959139702771.png)

​        第一步：配置各个主机的ip地址

​        实体机ip

​        ![ipzzhuji.png](http://www.178linux.com/ueditor/php/upload/image/20160904/1472959266802946.png) 

​        R1路由器ip（虚拟机1）

​        ![ip-r1.png](http://www.178linux.com/ueditor/php/upload/image/20160904/1472959288485349.png)

​        R2路由器ip

​        ![ip-r2.png](http://www.178linux.com/ueditor/php/upload/image/20160904/1472959301676664.png)

​        虚拟机3,ip

​        ![ip-7.2.png](http://www.178linux.com/ueditor/php/upload/image/20160904/1472959327256156.png)

​        第二步：关闭各个主机防火墙，selinux、打开R1、R2的接口间转发功能

​        ![r1-iptables-open-ip-forward.png](http://www.178linux.com/ueditor/php/upload/image/20160904/1472960399868311.png)

​        ![r2-iptables-open-ip-forward.png](http://www.178linux.com/ueditor/php/upload/image/20160904/1472960411479912.png)

​        第三步：初始状态下，测试各个主机与彼此之间的连通性

​        ![zhuji-start-ping.png](http://www.178linux.com/ueditor/php/upload/image/20160904/1472960609998320.png)

​        ![r1-start-ping.png](http://www.178linux.com/ueditor/php/upload/image/20160904/1472960632221200.png)

​        ![r2-start-ping.png](http://www.178linux.com/ueditor/php/upload/image/20160904/1472960657743329.png)

​        ![7.2-start-ping1.png](http://www.178linux.com/ueditor/php/upload/image/20160904/1472960685464621.png)

​        第四步：在R1和R2上设置相关路由条目

​        ![r1-add-routetable.png](http://www.178linux.com/ueditor/php/upload/image/20160904/1472960517266706.png)

​        ![r2-add-routetable.png](http://www.178linux.com/ueditor/php/upload/image/20160904/1472960538190359.png)

​        第五步：重新验证各个主机之间的连通性

​        ![zhuji-end-ping1.png](http://www.178linux.com/ueditor/php/upload/image/20160904/1472960742600645.png)

​        ![zhuji-trace.png](http://www.178linux.com/ueditor/php/upload/image/20160904/1472960765994973.png)

​        ![7.2-end-ping.png](http://www.178linux.com/ueditor/php/upload/image/20160904/1472960964607509.png)

​        ![7.2-trace.png](http://www.178linux.com/ueditor/php/upload/image/20160904/1472960982848226.png)