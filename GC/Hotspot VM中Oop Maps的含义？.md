# Hotspot VM中Oop Maps的含义？

Oop Maps是记录Java栈中引用对象的数据结构。它的主要作用是发现Java栈中的GC Roots对象，当对象被移动到Java堆中更新该对象的应用。

有三种类型的Oop Maps：

- 解释执行型Oop Maps。采用延迟计算算法，当GC发生时，通过字节码流分析。
- JIT编译型Oop Maps。JIT编译期间被触发，并内置于编译后的代码当中，当对象被引用时VM通过Java栈中的指令快速找出，并进行注册。
- 触发共享运行时副本型Oop Maps。开发者手动创建这些maps-运行时副本的作者。