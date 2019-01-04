## **概述**

Intellij IDEA真是越用越觉得它强大，它总是在我们写代码的时候，不时给我们来个小惊喜。出于对Intellij IDEA的喜爱，我决定写一个与其相关的专栏或者系列，把一些好用的Intellij IDEA技巧分享给大家。本文是这个系列的第一篇，主要介绍一些你可能不知道的但是又实用的小技巧。

## **我最爱的【演出模式】**

我们可以使用【Presentation Mode】，将IDEA弄到最大，可以让你只关注一个类里面的代码，进行毫无干扰的coding。

可以使用Alt+V快捷键，弹出View视图，然后选择Enter Presentation Mode。效果如下：



![img](https:////upload-images.jianshu.io/upload_images/1977282-dea0d352abf2d712?imageMogr2/auto-orient/strip%7CimageView2/2/w/1000/format/webp)



这个模式的好处就是，可以让你更加专注，因为你只能看到特定某个类的代码。可能读者会问，进入这个模式后，我想看其他类的代码怎么办？这个时候，就要考验你快捷键的熟练程度了。你可以使用CTRL+E弹出最近使用的文件。又或者使用CTRL+N和CTRL+SHIFT+N定位文件。

如何退出这个模式呢？很简单，使用ALT+V弹出view视图，然后选择Exit Presentation Mode 即可。但是我强烈建议你不要这么做，因为你是可以在Enter Presentation Mode模式下在IDEA里面做任何事情的。当然前提是，你对IDEA足够熟练。

## **神奇的Inject language**

如果你使用IDEA在编写JSON字符串的时候，然后要一个一个\去转义双引号的话，就实在太不应该了，又烦又容易出错。在IDEA可以使用Inject language帮我们自动转义双引号。 



![img](https:////upload-images.jianshu.io/upload_images/1977282-42afc1176d6ac611?imageMogr2/auto-orient/strip%7CimageView2/2/w/508/format/webp)



先将焦点定位到双引号里面，使用alt+enter快捷键弹出inject language视图，并选中 Inject language or reference。 



![img](https:////upload-images.jianshu.io/upload_images/1977282-8c87b374034d6b40?imageMogr2/auto-orient/strip%7CimageView2/2/w/639/format/webp)



选择后,切记，要直接按下enter回车键，才能弹出inject language列表。在列表中选择 json组件。



![img](https:////upload-images.jianshu.io/upload_images/1977282-ae366f4f578a9f78?imageMogr2/auto-orient/strip%7CimageView2/2/w/785/format/webp)



选择完后。鼠标焦点自动会定位在双引号里面，这个时候你再次使用alt+enter就可以看到 



![img](https:////upload-images.jianshu.io/upload_images/1977282-9ff2cf6dd541871e.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/652/format/webp)



选中Edit JSON Fragment并回车，就可以看到编辑JSON文件的视图了。 



![img](https:////upload-images.jianshu.io/upload_images/1977282-fa3a300bd49c2906?imageMogr2/auto-orient/strip%7CimageView2/2/w/868/format/webp)



可以看到IDEA确实帮我们自动转义双引号了。如果要退出编辑JSON信息的视图，只需要使用ctrl+F4快捷键即可。

Inject language可以支持的语言和操作多到你难以想象，读者可以自行研究。

## **使用快捷键移动分割线**

假设有下面的场景，某个类的名字在project视图里被挡住了某一部分。 



![img](https:////upload-images.jianshu.io/upload_images/1977282-c02e71067ed738bf?imageMogr2/auto-orient/strip%7CimageView2/2/w/1000/format/webp)



你想完整的看到类的名字，该怎么做。一般都是使用鼠标来移动分割线，但是这样子效率太低了。可以使用alt+1把鼠标焦点定位到project视图里，然后直接使用ctrl+shift+左右箭头来移动分割线。

**ctrl+shift+enter不只是用来行尾加分号的**

ctrl+shift+enter其实是表示为您收尾的意思，不只是用来给代码加分号的。比如说： 



![img](https:////upload-images.jianshu.io/upload_images/1977282-5c912fb5c4d11271?imageMogr2/auto-orient/strip%7CimageView2/2/w/543/format/webp)



这段代码，我们还需要为if语句加上大括号才能编译通过，这个时候你直接输入ctrl+shift+enter，IDEA会自动帮你收尾，加上大括号的。

**不要动不动就使用IDEA的重构功能**

IDEA的重构功能非常强大，但是也有时候，在单个类里面，如果只是想批量修改某个文本，大可不必使用到重构的功能。比如说： 



![img](https:////upload-images.jianshu.io/upload_images/1977282-653758f34d76d564?imageMogr2/auto-orient/strip%7CimageView2/2/w/594/format/webp)



上面的代码中，有5个地方用到了rabbitTemplate文本，如何批量修改呢？ 首先是使用ctrl+w选中rabbitTemplate这个文本,然后依次使用5次alt+j快捷键，逐个选中，这样五个文本就都被选中并且高亮起来了，这个时候就可以直接批量修改了。 



![img](https:////upload-images.jianshu.io/upload_images/1977282-17727a3cec6f4fe5?imageMogr2/auto-orient/strip%7CimageView2/2/w/631/format/webp)



## **去掉导航栏**

去掉导航栏，因为平时用的不多。 



![img](https:////upload-images.jianshu.io/upload_images/1977282-2a6e9809655fb9fc.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1000/format/webp)



可以把红色的导航栏去掉，让IDEA显得更加干净整洁一些。使用alt+v，然后去掉Navigation bar即可。去掉这个导航栏后，如果你偶尔还是要用的，直接用alt+home就可以临时把导航栏显示出来。



![img](https:////upload-images.jianshu.io/upload_images/1977282-6aa63d85606fbdcb.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1000/format/webp)



如果想让这个临时的导航栏消失的话，直接使用esc快捷键即可。

## **把鼠标定位到project视图里**

当工程里的包和类非常多的时候，有时候我们想知道当前类在project视图里是处在哪个位置。



![img](https:////upload-images.jianshu.io/upload_images/1977282-3bf8197e734bf85e?imageMogr2/auto-orient/strip%7CimageView2/2/w/952/format/webp)



上面图中的DemoIDEA里，你如何知道它是在spring-cloud-config工程里的哪个位置呢？ 可以先使用alt+F1，弹出Select in视图，然后选择Project View中的Project，回车，就可以立刻定位到类的位置了。



![img](https:////upload-images.jianshu.io/upload_images/1977282-94620c2d4e5eaefd?imageMogr2/auto-orient/strip%7CimageView2/2/w/346/format/webp)



那如何从project跳回代码里呢？可以直接使用esc退出project视图，或者直接使用F4,跳到代码里。

## **强大的symbol**

如果你依稀记得某个方法名字几个字母，想在IDEA里面找出来，可以怎么做呢？ 直接使用ctrl+shift+alt+n，使用symbol来查找即可。 比如说： 



![img](https:////upload-images.jianshu.io/upload_images/1977282-2f56bcb26a27ba8d?imageMogr2/auto-orient/strip%7CimageView2/2/w/581/format/webp)



你想找到checkUser方法。直接输入user即可。



![img](https:////upload-images.jianshu.io/upload_images/1977282-8071cf9278df3646?imageMogr2/auto-orient/strip%7CimageView2/2/w/735/format/webp)



如果你记得某个业务类里面有某个方法，那也可以使用首字母找到类,然后加个.，再输入方法名字也是可以的。 



![img](https:////upload-images.jianshu.io/upload_images/1977282-a9ea2a11b6e01c0a?imageMogr2/auto-orient/strip%7CimageView2/2/w/1000/format/webp)



## **如何找目录**

使用ctrl+shift+n后，使用/，然后输入目录名字即可. 



![img](https:////upload-images.jianshu.io/upload_images/1977282-f1bc657db7e35a4b?imageMogr2/auto-orient/strip%7CimageView2/2/w/657/format/webp)



## **自动生成not null判断语句**

自动生成not null这种if判断，在IDEA里有很多种办法，其中一种办法你可能没想到。



![img](https:////upload-images.jianshu.io/upload_images/1977282-7f9e248053dc65f9?imageMogr2/auto-orient/strip%7CimageView2/2/w/791/format/webp)



当我们使用rabbitTemplate. 后，直接输入notnull并回车，IDEA就好自动生成if判断了。



![img](https:////upload-images.jianshu.io/upload_images/1977282-f73e09980eb183dd?imageMogr2/auto-orient/strip%7CimageView2/2/w/612/format/webp)



## **按照模板找内容**

这个也是我非常喜欢的一个功能，可以根据模板来找到与模板匹配的代码块。比如说：

想在整个工程里面找到所有的try catch语句,但是catch语句里面没有做异常处理的。

catch语句里没有处理异常，是极其危险的。我们可以IDEA里面方便找到所有这样的代码。



![img](https:////upload-images.jianshu.io/upload_images/1977282-49326672c6dd7d28?imageMogr2/auto-orient/strip%7CimageView2/2/w/654/format/webp)



首先使用ctrl+shift+A快捷键弹出action框，然后输入Search Struct 



![img](https:////upload-images.jianshu.io/upload_images/1977282-98370f4d7d209ba5?imageMogr2/auto-orient/strip%7CimageView2/2/w/612/format/webp)



选择Search Structurally后，回车，跳转到模板视图。



![img](https:////upload-images.jianshu.io/upload_images/1977282-04d8bf4bc158ea3b?imageMogr2/auto-orient/strip%7CimageView2/2/w/788/format/webp)



点击Existing Templates按钮，选择try模板。为了能找出catch里面没有处理异常的代码块，我们需要配置一下CatchStatement的Maximum count的值，将其设置为1。

点击Edit Variables按钮，在界面修改Maximum count的值。 



![img](https:////upload-images.jianshu.io/upload_images/1977282-651d13ee9d8208a9?imageMogr2/auto-orient/strip%7CimageView2/2/w/716/format/webp)



最后点击find按钮，就可以找出catch里面没有处理异常的代码了。 



![img](https:////upload-images.jianshu.io/upload_images/1977282-08f436f5b2ff9d40?imageMogr2/auto-orient/strip%7CimageView2/2/w/1000/format/webp)





作者：Sam哥哥

链接：https://blog.csdn.net/linsongbin1/article/details/80211919

