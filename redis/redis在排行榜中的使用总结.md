## redis在排行榜中的使用总结



# 前言

> [redis官网](https://redis.io/)
>
> Redis 是一个开源（BSD许可）的，内存中的数据结构存储系统，它可以用作数据库、缓存和消息中间件。它支持多种类型的数据结构，如 字符串（strings）， 散列（hashes）， 列表（lists）， 集合（sets）， 有序集合（sorted sets） 与范围查询， bitmaps， hyperloglogs 和 地理空间（geospatial） 索引半径查询。 Redis 内置了 复制（replication），LUA脚本（Lua scripting）， LRU驱动事件（LRU eviction），事务（transactions） 和不同级别的 磁盘持久化（persistence）， 并通过 Redis哨兵（Sentinel）和自动 分区（Cluster）提供高可用性（high availability）

正如上面的介绍，redis支持很多种类型的数据结构，适用于多种场景。目前应用最为广泛的场景主要分为以下几类：

- 会话缓存(session cache)
- 队列
- 排行榜/列表
- 计数器
- 发布/订阅

本篇，我主要是从排行榜的业务场景下，进行的一些个人总结。

## 一、排行榜业务的分类

排行榜业务变化多样，从不同的角度思考，是不同的排行榜需求，但总结起来，主要分为以下几类：

#### 实效性

从排行榜的实效性上划分，主要分为：

- 实时榜：基于当前一段时间内数据的实时更新，进行排行。例如：当前一小时内游戏热度实时榜，当前一小时内明星送花实时榜等
- 历史榜：基于历史一段周期内的数据，进行排行。例如：日榜（今天看昨天的），周榜（上一周的），月榜（上个月的），年榜（上一年的)

#### 业务数据类型

从需要排行的数据类型上划分，主要分为：

- 单类型数据排行榜：是指需要排行的主体不需要区分类型，例如，所有用户积分排行，所有公贡献值排行，所有游戏热度排行等
- 多类型（复合类型）数据排行榜：是指需要排行的主体在排行中要求有类型上的区分，例如：竞技类游戏热度排行、体育类游戏热度排行、MOBA类游戏操作性排行、角色/回合/卡牌三类游戏热度排行等

#### 展示唯度

从榜单的最终展示唯度上划分，主要分为：

- 单唯度：是指选择展示的排行榜就是基于一个唯度下的排行，例如前面提到的MOBA类游戏操作性排行榜，就仅展示所有MOBA类游戏按操作性的评分排行
- 多唯度：是指选择展示的排行榜还有多种唯度供用户选择，仍然以前面的MOBA类游戏为例，唯度除了操作性，还有音效评分排行，难易度评分排行，画面评分排行等。

#### 展示数据量

从需要展示的数据量上划分，主要分为：

- topN数据：只要求展示topN条排行纪录，例如：最火MOBA游戏top20
- 全量数据：要求展示所有数据的排行，例如：所有用户的积分排行

在以上几种榜单中，往往还会有需要加入，当前用户自身的一些数据在排行榜中的位置。

## 二、排行榜redis数据结构的选择

选择合适的redis数据结构，可以快速的实现排行榜要求。

首选Sorted Set数据结构:

> Redis Sorted sets —
> Sorted sets are a data type which is similar to a mix between a Set and a Hash. Like sets, sorted sets are composed of unique, non-repeating string elements, so in some sense a sorted set is a set as well.
> However while elements inside sets are not ordered, every element in a sorted set is associated with a floating point value, called the score (this is why the type is also similar to a hash, since every element is mapped to a value).
> Moreover, elements in a sorted sets are taken in order (so they are not ordered on request, order is a peculiarity of the data structure used to represent sorted sets). They are ordered according to the following rule:
> If A and B are two elements with a different score, then A > B if A.score is > B.score.
> If A and B have exactly the same score, then A > B if the A string is lexicographically greater than the B string. A and B strings can’t be equal since sorted sets only have unique elements.

在Sorted-Set中添加、删除或更新一个成员都是非常快速的操作，其时间复杂度为集合中成员数量的对数。由于Sorted-Sets中的成员在集合中的位置是有序的，因此，即便是访问位于集合中部的成员也仍然是非常高效的。

Sorted-Set底层的实现是跳表(skiplist),插入和删除的效率都很高.

关于跳表(skiplist)结构（图片来自[维基百科](https://en.wikipedia.org/wiki/File:Skip_list.svg))：
[![screenshot.png](http://ata2-img.cn-hangzhou.img-pub.aliyun-inc.com/e489444827d07ce29fe5e3c2d46c7c0a.png)](http://ata2-img.cn-hangzhou.img-pub.aliyun-inc.com/e489444827d07ce29fe5e3c2d46c7c0a.png)

wiki上的[详情介绍](https://en.wikipedia.org/wiki/Skip_list)

在redis的源码中，找到zset的定义如下(server.h)：

```c
/* ZSETs use a specialized version of Skiplists */
typedef struct zskiplistNode {
    sds ele;
    double score;
    struct zskiplistNode *backward;
    struct zskiplistLevel {
        struct zskiplistNode *forward;
        unsigned int span;
    } level[];
} zskiplistNode;

typedef struct zskiplist {
    struct zskiplistNode *header, *tail;
    unsigned long length;
    int level;
} zskiplist;

typedef struct zset {
    dict *dict;
    zskiplist *zsl;
} zset;
```

插入节点对应的gif演示[来自wiki](https://en.wikipedia.org/wiki/Skip_list#/media/File:Skip_list_add_element-en.gif)

看了上面的插入节点动画演示，再来看插入节点对应的方法源码(t_zset.c)，理解起来就容易多了：

```c
/* Insert a new node in the skiplist. Assumes the element does not already
 * exist (up to the caller to enforce that). The skiplist takes ownership
 * of the passed SDS string 'ele'. */
zskiplistNode *zslInsert(zskiplist *zsl, double score, sds ele) {
    zskiplistNode *update[ZSKIPLIST_MAXLEVEL], *x;
    unsigned int rank[ZSKIPLIST_MAXLEVEL];
    int i, level;

    serverAssert(!isnan(score));
    x = zsl->header;
    for (i = zsl->level-1; i >= 0; i--) {
        /* store rank that is crossed to reach the insert position */
        rank[i] = i == (zsl->level-1) ? 0 : rank[i+1];
        while (x->level[i].forward &&
                (x->level[i].forward->score < score ||
                    (x->level[i].forward->score == score &&
                    sdscmp(x->level[i].forward->ele,ele) < 0)))
        {
            rank[i] += x->level[i].span;
            x = x->level[i].forward;
        }
        update[i] = x;
    }
    /* we assume the element is not already inside, since we allow duplicated
     * scores, reinserting the same element should never happen since the
     * caller of zslInsert() should test in the hash table if the element is
     * already inside or not. */
    level = zslRandomLevel();
    if (level > zsl->level) {
        for (i = zsl->level; i < level; i++) {
            rank[i] = 0;
            update[i] = zsl->header;
            update[i]->level[i].span = zsl->length;
        }
        zsl->level = level;
    }
    x = zslCreateNode(level,score,ele);
    for (i = 0; i < level; i++) {
        x->level[i].forward = update[i]->level[i].forward;
        update[i]->level[i].forward = x;

        /* update span covered by update[i] as x is inserted here */
        x->level[i].span = update[i]->level[i].span - (rank[0] - rank[i]);
        update[i]->level[i].span = (rank[0] - rank[i]) + 1;
    }

    /* increment span for untouched levels */
    for (i = level; i < zsl->level; i++) {
        update[i]->level[i].span++;
    }

    x->backward = (update[0] == zsl->header) ? NULL : update[0];
    if (x->level[0].forward)
        x->level[0].forward->backward = x;
    else
        zsl->tail = x;
    zsl->length++;
    return x;
}
```

（p.s:skip list 看似一个简单的链表分层设计，却可以取得很好的效果，大道至简！）

## 三、缓存的设计

了解redis中的sorted set为何可以做到快速的排序之后，接下来就是我们在实际的业务中，如何来利用它的这个特性了。

在我们的业务排行榜中，在决定使用redis来做排序之前，我们往往还有其他方案可供选择，比如：采用数据库存储数据，构建好相应的索引，通过sql查询来直接排行，也是可以做到的。个人认为，如果数据量不大，而且排行榜类型也不多，完全可以用sql来解决，毕竟引入redis，也还需要考虑很多运维场景。

如果我们选择使用redis，那如何定义好业务的key，member,和scores，就很重要了。
根据前面所总结的排行榜业务的分类不同，设计时思考的角度也都不同，但基本上我会按照以下步骤：

- 规范key的命名：一般我们采用这种格式，服务代号:业务代号:排行主体:榜单分类:%s_版本号。在这里的变量%s，也会根据不同的排行榜进行定义，往往是排序对象的父级，例如：所有游戏用户日积分排行榜，排行展示的对象是用户列表，在该榜单中用户归属至某一天，那对应的%s就可以定义为yyyyMMdd格式的具体日期，这样就可以很方便我们进行key的组装和数据查询；类似，某款游戏下的用户累积充值排行榜，排行展示的对象是用户列表，在该榜单中的用户归属于游戏，那对应的%s可以是游戏id，再比如，竞技类游戏热度排行榜，排行展示的是游戏列表，游戏都归属在这个竞技类下面，那这里的%s可以是该游戏类别。
- 确定memeber：将需要排序的对象，也就是最终要展示在榜单列表中的对象定义为member。就像上面提到所有游戏用户日积分排行榜中对应的member是用户(uid)、竞技类游戏热度排行榜中对应的member是游戏(gameId)
- 确定scores:大部份的情况下,scores的值就是最终用来比较的值，例如上面提到的积分值、充值值、热度值，还有的可能是时间值，也可能是一个经过复合计算的权重值（小技巧)，目的就是为了提供给sorted set进行比较排序
- 有效期：根据上面第一节中榜单的时效性分类，对应不同的缓存时间

#### 案例分析

明星收花自然周实时榜(游戏用户给喜欢的游戏代言明星送花，用户即可查看当前所有明星的本周排行榜，一周有效，获得花越多明星的排名越前，分页展示)，也可以查看具体一个明星下，送花的用户的排行榜，当天有效。根据前面的分类，明星本周排行，需要最终展示的列表是明星，那member必定是明星(starId)，而归属的实效性上是自然周，所以我们key值用自然周来进行组合，于是我们做下面这种设计：

```c
key---star:ss:flower:week:real:%s_1.0   (%s 可以是周的第一天日期yyyyMMdd)
member---starId
score---flowerNumber
每当一个用户有给明星送花时，通过zadd、incrBy(key,flowerNumber,starId)插入数据
需要排行时，通过zrangeByScore可能方便的分页获取排行榜,倒序可以zrevrangebyscore
需要获取排行榜中的总数量，通过zcount可以获得
需要获取当前用户送过花的某个明星的所处的排行，通过zrank,即可得到相应的序号
需要获取当前用户送过花的某个明星的具体收到的花的数量，通过zscore，即可得到相应的数量
```

具体明星下的送花用户排行榜（用户送的越多，排名越前,分页展示），这种场景，这时要求展示的列表是普通用户，那member我们选择普通用户(uid),而其归属是在一个具体的明星下，所以我们key值用starId来进行组合，于是我们也可以做这种设计：

```c
key---star:ss:flower:week:real:%s_1.0 (%s 就可以用starId)
member---uid
score---flowerNumber
```

用户送花自然周实时榜、用户送花自然月实时榜也类似。

是不是觉其实挺简单，只要我们能清楚的将需要展示的排行榜，确定member，界定key，score值的量化，那这种排行榜实现起来是不是比数据库来做就容易多了。

上面举的列子中，score值都是属于比较确定的，在业务动作发生时，通过不断的累积，能有一个具体的值。还有一些业务场景，这种score值并不是由简单的一个确定了的数字，这个时候往往需要我们自己定义一种计算公式，通过一些简单的运算得到一个“权值”；其次,上面的member也都是可以直接确定的对象id,有的些业务排行榜中，我们可能member也有可能遇到组合的方式。

不管member是唯一的id，还是组合而成，我们都需要注意以下两点：

- member在sorted set数据结构中，必须是唯一的
- 相同score的情况下，是以member的字典顺序排序的

member组合的场景，例如：如果我们需要一种实时排行榜，总是排行当前3小时内的某个明星下的所有送花用户排行列表（如，从14:01--17:01,从14:02--17:02...........),那我们可以这样设计：

```c
key---star:ss:flower:hour:real:%s_2.0 (%s 用starId)
member---uid_now()_flowerNumber  (谁，什么时间，本次送了几朵)  
score---now()
说明：
明星每收到一朵花，我们同样可以通过zadd的方式加入至缓存中，用当前时间和本次送花数量，组合member值（唯一），以当前时间为score值（方便比较）
在我们需要查询该排行榜时，我们首先会zremrangebyscore 删除掉当前时间3个小时以前的数据，然后取出member、拆分、zadd（zincrby)到另一个结果缓存中(1分钟有效），1分钟内的请求从结果缓存中直接获取，这样也就可以得到相应的3小时滚动时间内的排行榜了。
当然也许你还有更好的方案。
```

还有一些排行榜中，我们有时候甚至需要将多个key进行重新组合再排序，这种排行有一个显著的特殊，那就是排行榜中展示的主体对象是有较多类型的，也是说对应我们上面排行榜分类中所提到的，从业务数据类型上划分中的多类型那一类。举个例子：游戏分为竞技类游戏、动作类游戏、赛车类游戏，如果要求每一种类型的游戏都有热度排行榜，我们可以很轻松的设计出key:

```c
key:ng:game:hot:rank:%s_1.0  (%s 用游戏分类id)
member:gameId
score:hotVal
```

如果后面又想将这三种类型的游戏组合到一起进行排序，那又该怎么办？

这就需要通过zunionstore来实现了，通过zunionstore来得到一个并集，并最终排序。但这里需要注意的是需要组成并集的各个集合的key必须是对应到redis集群中的同一个slot上，否则将会出现一个异常：CROSSSLOT Keys in request don't hash to the same slot。所以redis提供了一种特定的标签{},这个{}内的字符串才参与计算hash slot.列如：{user}:aaa与{user}:bbb 这两个集合可以确保在同一个slot上,可以使用zunionstore求它们的并集。所以，在处理这种需要合并redis历史数据时，如果是在redis集群环境下，需要特别注意。

类似的，我们有时候可以通过日榜组合三日榜，七日榜，组合月榜，组合年榜等等

是不是看起来排行榜也挺容易实现的，赶紧试试吧

#### 其他考虑

在基于redis的整个排行榜的设计过程中，我们还需要考虑的

- 排行榜key的数量：确保key的增长数量是可控的，可设置过期时间的，就设置明显的过期时间
- 占用空间评估：redis中排行榜数据内存占用情况进行评估

#### 参考资料

- [https://en.wikipedia.org/wiki/Skip_list](https://en.wikipedia.org/wiki/Skip_list)
- [https://redis.io/](https://redis.io/)
- [http://redis.cn/](http://redis.cn/)