# jQuery转Vue项目实践总结

> 工作需要，将公司项目从jQuery转成Vue来写。这里分享下转变项目的过程并写了一个小demo，希望能对遇到同样问题的朋友一些帮助。
> PS： 本人Android开发，兼职前端，前端知识浅薄，有什么不对的地方还请指出，大家共同进步。谢谢~
> 由于代码较多，我把源码放在了博客最后了。

# jQuery和Vue的区别

jQuery是使用选择器（$）选取DOM对象，对其进行赋值、取值、事件绑定等操作，其实和原生的HTML的区别只在于可以更方便的选取和操作DOM对象，而数据和界面是在一起的。比如需要获取label标签的内容：`$("lable").val();`,它还是依赖DOM元素的值。
Vue则是通过Vue对象将数据和View完全分离开来了。对数据进行操作不再需要引用相应的DOM对象，可以说数据和View是分离的，他们通过Vue对象这个vm实现相互的绑定。这就是传说中的MVVM。

# jQuery to Vue

## 1. 导入Vue.js，去除jQuery。

下载Vue.js，导入工程,我将其放在头文件中。

```javascript
<script src="vue.js"></script>
```

[Vue.js下载地址](http://cn.vuejs.org/guide/installation.html)

## 2. 根视图id绑定

为最外层的div标签定义id，然后使用Vue的el属性进行绑定

```javascript
<div id="app">
        <h2>学生信息登记</h2>
        ...
        <label>{{ result }}</label>    
</div>
...
<script>
    new Vue({
        el: '#app',
        ...
    });
</script>
...
```

## 3. 为input添加v-model

使用v-model属性将input标签的value值绑定到data的相应数据中。

```javascript
<div>
    <label>姓名：</label>
    <input type="text" placeholder="请输入姓名" v-model="name">
</div>
<div>
    <label>性别：</label>
    <input id="sex01" type="radio" value="1" v-model="sex">
    <label for="sex01">男</label>
    <input id="sex02" type="radio" value="2" v-model="sex">
    <label for="sex02">女</label>
</div>
<div>
    <label>年龄：</label>
    <select v-model="age">
        <option selected>18</option>
        <option>19</option>
        <option>20</option>
        <option>21</option>
    </select></div><div>
    <label>党员：</label>
    <input type="checkbox" v-model="member">
</div>
```

注意上方的v-model，我们在Vue对象的data属性中绑定数据：

```javascript
new Vue({
    el: '#app',
    data: {
        name: '',
        sex: '',
        age: '',
        member: '',
        result: ''
    },
    ...
})
```

到这里就实现了将表单input框和数据的绑定。更多Vue表单绑定可以查看[表单控件绑定](http://cn.vuejs.org/guide/forms.html)

## 4. 删除id、name这些用于jQuery的属性

在使用jQuery时，在HTML中需要定义大量的id、name之类的属性用于jQuery选择器获取元素。

```javascript
<input type="text" placeholder="请输入姓名" id="name">
...
var name = $('#name').val();
```

我们这里就不需要了。去除HTML中的这些属性。
（其实这里v-model和id的作用有些类似，都是一个桥梁作用。我在修改的时候偷懒直接将id改成v-model，后面的name不改~）

## 5. 将点击事件onclick改为v-on:click

Vue提供了v-on来监听DOM事件，如demo中的点击事件监听属性v-on:click。

```javascript
<button id="btnCommit" v-on:click="commit">提交</button>
<button id="btnReset" v-on:click="reset">重置</button>
```

然后在Vue对象的methods属性中创建这两个事件方法。

```javascript
new Vue ({
    ...
    methods: {
        commit: function () {...},
        reset: function () {...},
    ...
...
```

另外，Vue还提供了其他v-on:属性，如v-on:change、v-on:keyup等。具体请看：[方法与事件处理](http://cn.vuejs.org/guide/events.html)

## 6. 引用数据

作为MVVM，当然是双向绑定的。Vue用v-model属性使input可以修改数据内容，实现界面修改数据；使用双大括号来引用数据内容，实现数据修改界面。
具体写法如下：

```javascript
<label>{{ result }}</label>
...
data: {
    result: 'hello world',
    ...
}
```

如上引用后，数据`hello world`将会实时同步显示在labal标签上，每当result数据有所改变，label的内容立即会改变。

## 7. 替换ready方法

jQuery中为我们提供了一个document的ready方法，Vue中有ready属性，它们的触发时间差不多，具体要参考各自的生命周期。
**jQuery写法：**

```javascript
$(document).ready(function () {
    alert("加载完成");
});
```

**Vue写法：**

```javascript
new Vue ({
    ...
    ready: function () {
        alert("加载完成");
    },
    ...
})
```

## 8.修改获取和修改元素属性的方式

这是jQuery和Vue的最大不同点了。先看代码:
**jQuery:**

```javascript
var name = $('#name').val();
var sex = '';
if (getChackedValue('input[name=sex]') == 1){
    sex = '男'
}else if (getChackedValue('input[name=sex]') == 2){
    sex = '女'
}
var age = $('#age').val();
var member = '';
if($('#member').is(':checked')) {
    member = '党员';
}else {
    member = "非党员";
}
var result = name + ' ' + sex + ' ' + age + ' ' + member;
$('#result').text(result);
```

这是显示表单结果的函数。jQuery是通过美元符号$来获取指定元素，然后通过val()、text()等方法获取指定元素的内容或者为指定元素赋值。
**Vue：**

```javascript
var name = this.name;
var sex = '';
if (this.sex == 1){
    sex = '男';
}else if (this.sex == 2){
    sex = '女';
}
var age = this.age;
var member = '';
if (this.member){
    member = '党员';
}else {
    member = '非党员'
}
var result = name + ' ' + sex + ' ' + age + ' ' + member;
this.result = result;
```

同样是显示表单结果的函数。Vue不需要获取DOM元素，只需要获得和修改data对象中的数据就可以了。
这里需要注意的是：要用jQuery获得或者修改一组radio很麻烦，需要操作checked属性；而Vue处理radio只需通过数据，数据内容就是radio的value值，修改value值radio就会自动选择目标项。checkbox也是如此，jQuery要使用checked，而Vue只需要知道checkbox绑定的data为true或者false就可以知道checkbox是否被选中。

## 9. Vue使用watch方法测试

Vue的watch方法真的挺好用，当程序出现问题时可以将出问题的data使用watch打log，每当该数据发生变化时，watch方法都会被触发。很好用~

```javascript
watch: {
    'sex': function (val, oldVal) {
        console.log('oldVal = ' + oldVal + ' val = ' + val);
    }
}
```

# 总结心得

jQuery完全是通过美元符号来对各种元素进行操作！根据HTML元素的id、name等属性来获取到元素并对其进行取值、赋值、修改属性能行为。
Vue的使用过程 是：先绘制HTML界面，然后在需要绑定数据的地方写下v-model、v-on等这些绑定属性和方法，在显示数据内容的地方使用双大括号显示内容。然后在Vue中，el属性绑定根视图的id，data属性定义并初始化v-model、双大括号用到的数据和一些其他数据。methods属性定义在v-on中用到的和一些其他方法。更新界面修改数据实现。而修改数据通过操作界面实现。
写完了这个demo后，感觉到Vue的确有它的魅力所在。它的MVVM让业务逻辑变得更加清晰和简单。

# 所有代码

## jQueryPage

```html
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>index</title>
    <script src="jquery-2.2.3.js"></script>
</head>
<body>
    <div>
        <h2>学生信息登记</h2>
        <br>
        <div>
            <label>姓名：</label>
            <input type="text" placeholder="请输入姓名" id="name">
        </div>
        <div>
            <label>性别：</label>
            <input id="sex01" type="radio" value="1" name="sex">
            <label for="sex01">男</label>
            <input id="sex02" type="radio" value="2" name="sex">
            <label for="sex02">女</label>
        </div>
        <div>
            <label>年龄：</label>
            <select id="age">
                <option selected>18</option>
                <option>19</option>
                <option>20</option>
                <option>21</option>
            </select>
        </div>
        <div>
            <label>党员：</label>
            <input type="checkbox" id="member">
        </div>
        <br>
        <button id="btnCommit" onclick="commit()">提交</button>
        <button id="btnReset" onclick="reset()">重置</button>
        <br>
        <br>
        <label id="result"></label>
    </div>
    <script type="text/javascript">
        $(document).ready(function () {
            alert("加载完成");
        });
        function commit() {
            var name = $('#name').val(); 
            var sex = '';
            if (getChackedValue('input[name=sex]') == 1){
                sex = '男'
            }else if (getChackedValue('input[name=sex]') == 2){
                sex = '女'
            }
            var age = $('#age').val();
            var member = '';
            if($('#member').is(':checked')) {
                member = '党员';
            }else {
                member = "非党员";
            }
            var result = name + ' ' + sex + ' ' + age + ' ' + member;
            $('#result').text(result);
        }
        function reset() {
            $('#result').text('');
        }
        function getChackedValue (CheckboxId) {
            var value = 0;
            var i = 0;
            $(CheckboxId).each(function () {
                if($(CheckboxId).eq(i).is(':checked'))                {
                    value = $(CheckboxId).eq(i).val(); 
                   return;
                }
                i++;
            });
            return value;
        }
    </script>
</body>
</html>
```

## VuePage

```html
<!DOCTYPE html>
<html lang="en" xmlns:v-on="http://www.w3.org/1999/xhtml"><head>
    <meta charset="UTF-8">
    <title>index</title>
    <script src="vue.js"></script>
</head>
<body>
    <div id="app">
        <h2>学生信息登记</h2>
        <br>
        <div>
            <label>姓名：</label>
            <input type="text" placeholder="请输入姓名" v-model="name">
        </div>
        <div>
            <label>性别：</label>
            <input id="sex01" type="radio" value="1" v-model="sex">
            <label for="sex01">男</label>
            <input id="sex02" type="radio" value="2" v-model="sex">
            <label for="sex02">女</label>
        </div>
        <div>
            <label>年龄：</label>
            <select v-model="age">
                <option selected>18</option>
                <option>19</option>
                <option>20</option>
                <option>21</option>
            </select>
        </div>
        <div>
            <label>党员：</label>
            <input type="checkbox" v-model="member">
        </div>
        <br>
        <button id="btnCommit" v-on:click="commit">提交</button>
        <button id="btnReset" v-on:click="reset">重置</button>
        <br>
        <br>
        <label>{{ result }}</label>
    </div>
    <script>
        new Vue({
            el: '#app',
            data: {
                name: '',
                sex: '',
                age: '',
                member: '',
                result: ''
            }, 
            ready: function () {
                alert("加载完成");
            },
            methods: {
                commit: function () {
                    var name = this.name;
                    var sex = '';
                    if (this.sex == 1){
                        sex = '男';
                    }else if (this.sex == 2){
                        sex = '女';
                    }
                    var age = this.age;
                    var member = '';
                    if (this.member){
                        member = '党员';
                    } else {
                        member = '非党员';
                    }
                    var result = name + ' ' + sex + ' ' + age + ' ' + member;
                    this.result = result;
                },
                reset: function () {
                    this.result = '';
                }
            },
            watch: {
                'sex': function (val, oldVal) {
                    console.log('oldVal = ' + oldVal + ' val = ' + val);
                }
            }
        });
    </script>
</body>
</html>
```