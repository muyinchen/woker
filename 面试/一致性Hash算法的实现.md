# 一致性Hash算法的实现

一致性hash作为一个负载均衡算法，可以用在分布式缓存、数据库的分库分表等场景中，还可以应用在负载均衡器中作为作为负载均衡算法。在有多台服务器时，对于某个请求资源通过hash算法，映射到某一个台服务器，当增加或减少一台服务器时，可能会改变这些资源对应的hash值，这样可能导致一部分缓存或数据失效了。一致性hash就是尽可能在将同一个资源请求路由到同一台服务器中。

本篇文章将模拟实现一个分布式缓存系统来探讨在使用了一致性hash以及普通hash在增加、删除节点之后，对数据分布、缓存命中率的影响

### 节点&集群设计

在一个分布式缓存系统中，每台机器可以认为是一个节点，节点作为数据存储的地方，由一些节点来组成一个集群。我们先来设计我们的节点和集群。

#### 节点

```java
@Data
public class Node {
  
    private String domain;

    private String ip;

    private Map<String, Object> data;

    public <T> void put(String key, T value) {
        data.put(key, value);
    }

    public void remove(String key){
        data.remove(key);
    }

    public <T> T get(String key) {
        return (T) data.get(key);
    }
}
```

一个节点包括domain(域名)，ip(IP地址)，data(节点存储数据)，节点可以存放、删除、获取数据。

#### 集群

```java
public abstract class Cluster {
  
    protected List<Node> nodes;

    public Cluster() {
        this.nodes = new ArrayList<>();
    }
  
    public abstract void addNode(Node node);

    public abstract void removeNode(Node node);

    public abstract Node get(String key);
}
```

在一个集群中包含多个节点，可以在一个集群中，增加、删除节点。还可以通过key获取数据存储的节点。

### 取模

在使用取模的场景中，当一个请求资源，请求某个集群时，通过对请求资源进行hash得到的值，然后对存储集群的节点数取模来得到，该请求资源，应该存储到哪一个存储节点。

具体实现如下：

```java
public class NormalHashCluster extends Cluster {

    public NormalHashCluster() {
        super();
    }

    @Override
    public void addNode(Node node) {
        this.nodes.add(node);
    }

    @Override
    public void removeNode(Node node) {
        this.nodes.removeIf(o -> o.getIp().equals(node.getIp()) ||
                o.getDomain().equals(node.getDomain()));
    }

    @Override
    public Node get(String key) {
        long hash = hash(key);
        long index =  hash % nodes.size();
        return nodes.get((int)index);
    }
}
```

下面我们对该算法，在数据分布、增加一个节点、删除一个节点对缓存的命中率影响做一个测试

```java
Cluster cluster = new NormalHashCluster();
cluster.addNode(new Node("c1.yywang.info", "192.168.0.1"));
cluster.addNode(new Node("c2.yywang.info", "192.168.0.2"));
cluster.addNode(new Node("c3.yywang.info", "192.168.0.3"));
cluster.addNode(new Node("c4.yywang.info", "192.168.0.4"));

IntStream.range(0, DATA_COUNT)
        .forEach(index -> {
            Node node = cluster.get(PRE_KEY + index);
            node.put(PRE_KEY + index, "Test Data");
        });

System.out.println("数据分布情况：");
cluster.nodes.forEach(node -> {
    System.out.println("IP:" + node.getIp() + ",数据量:" + node.getData().size());
});

//缓存命中率
long hitCount = IntStream.range(0, DATA_COUNT)
        .filter(index -> cluster.get(PRE_KEY + index).get(PRE_KEY + index) != null)
        .count();
System.out.println("缓存命中率：" + hitCount * 1f / DATA_COUNT);
```

在初始状态下，数据的分布和缓存命中率如下：

```java
数据分布情况：
IP:192.168.0.1,数据量:12499
IP:192.168.0.2,数据量:12501
IP:192.168.0.3,数据量:12499
IP:192.168.0.4,数据量:12501
缓存命中率：1.0
```

从以上数据可以看出，数据分布较均匀，在不增不减节点的情况下，缓存全部命中

我们新增一个节点

```java
//增加一个节点
cluster.addNode(new Node("c5.yywang.info", "192.168.0.5"));
```

这时缓存命中率

```java
增加一个节点的缓存命中率：0.19808
```

我们来删除一个节点

```java
cluster.removeNode(new Node("c4.yywang.info", "192.168.0.4"));
```

这时缓存命中率

```java
删除缓存命中率：0.25196
```

从以上可以看出，通过取模算法，在增加节点、删除节点时，将对缓存命中率产生极大的影响，所以在该场景中如果使用取模运算将会产生很多的数据迁移量。

### 一致性hash

为了解决以上取模运算的缺点，我们引入一致性hash算法，一致性hash算法的原理如下：

首先我们把2的32次方想象成一个环，比如：

[![hash1](http://yywang.qiniudn.com/hash1.png)](http://yywang.qiniudn.com/hash1.png)

假如我们有四台服务器分布这个环上，其中Node1,Node2,Node3,Node4就表示这四台服务器在环上的位置，一致性hash算法就是，在缓存的Key的值计算后得到的hash值，映射到这个环上的点，然后这些点按照顺时针方向找，找到离自己最近的一个物理节点就是自己要存储的节点。

当我们增加了一个节点如下：

[![hash2](http://yywang.qiniudn.com/hash2.png)](http://yywang.qiniudn.com/hash2.png)

我们增加了Node5放在Node3和Node4之间，这时我们可以看到增加了一个节点只会影响Node3至Node5之间的数据，其他节点的数据不会受到影响。同时我们还可以看到，Node4和Node5的压力要小于其他节点，大约是其他节点的一半。这样就带了压力分布均匀的情况，假定Node4和Node5的机器配置和其它的节点机器配置相同，那么Node4和Node5的机器资源就浪费了一半，那么怎么解决这个问题呢？

我们引入虚拟节点，简单来说，虚拟节点就是不存在的点，这些虚拟节点尽量的分布在环上，需要做的就是把这些虚拟节点需要映射到物理节点。

[![hash3](http://yywang.qiniudn.com/hash3.png)](http://yywang.qiniudn.com/hash3.png)

在引入虚拟节点后，我们把虚拟节点上均匀的分布到环上，然后把虚拟节点映射到物理节点，当增加了新的机器后，我们只需要把虚拟节点映射到新的机器即可，这样就解决了机器压力分布不均匀的情况

上面我们说了一致性hash的基本算法，下面我们来看下具体实现

```java
public class ConsistencyHashCluster extends Cluster {

    private SortedMap<Long, Node> virNodes = new TreeMap<Long, Node>();

    private static final int VIR_NODE_COUNT = 512;

    private static final String SPLIT = "#";

    public ConsistencyHashCluster() {
        super();
    }

    @Override
    public void addNode(Node node) {
        this.nodes.add(node);
        IntStream.range(0, VIR_NODE_COUNT)
                .forEach(index -> {
                    long hash = hash(node.getIp() + SPLIT + index);
                    virNodes.put(hash, node);
                });
    }

    @Override
    public void removeNode(Node node) {
        nodes.removeIf(o -> node.getIp().equals(o.getIp()));
        IntStream.range(0, VIR_NODE_COUNT)
                .forEach(index -> {
                    long hash = hash(node.getIp() + SPLIT + index);
                    virNodes.remove(hash);
                });
    }

    @Override
    public Node get(String key) {
        long hash = hash(key);
        SortedMap<Long, Node> subMap = hash >= virNodes.lastKey() ?
                virNodes.tailMap(0L) : virNodes.tailMap(hash);
        if (subMap.isEmpty()) {
            return null;
        }
        return subMap.get(subMap.firstKey());
    }
}
```

下面我们同样对一致性hash算法，在数据分布、增加一个节点、删除一个节点对缓存的命中率影响做一个测试

测试代码很简单，我们只需要把以上的代码替换成ConsistencyHashCluster实现即可

```java
//        Cluster cluster = new NormalHashCluster();
        Cluster cluster=new ConsistencyHashCluster();
```

在初始状态下，数据的分布和缓存命中率如下：

```java
数据分布情况：
IP:192.168.0.1,数据量:15345
IP:192.168.0.2,数据量:14084
IP:192.168.0.3,数据量:10211
IP:192.168.0.4,数据量:10360
缓存命中率：1.0
```

从以上数据可以看出，数据分布相对均匀，没有取模算法的均匀，在不增不减节点的情况下，缓存全部命中

我们增加一个节点

```java
cluster.addNode(new Node("c" + 5 + ".yywang.info", "192.168.0." + 5));
```

这时缓存命中率

```java
增加一个节点的缓存命中率：0.82154
```

可以看出缓存命中率明显高于取模运算的命中率

我们删除一个节点

```java
cluster.removeNode(new Node("c4.yywang.info", "192.168.0.4"));
```

这时缓存命中率

```java
删除一个节点的缓存命中率：0.7928
```

从以上可以看出，我们可以看出使用一致性hash算法，可以极大的提高缓存的命中率，减少在增加节点、删除节点时，数据迁移的成本。