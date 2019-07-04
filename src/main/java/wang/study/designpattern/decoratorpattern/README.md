# 装饰器模式
    允许向一个现有的对象添加新的功能，同时又不改变其结构
    创建一个装饰类，用来包装原有的类，并保持类方法签名完整性的前提下，提供额外的功能
    继承关系的一种替代方案
    
    应用案例 java I/O 标准库设计
    
## 意图
    动态的给一个对象添加一些额外的职责

## 优点
    装饰类和被装饰类可以独立发展，不会相互耦合，装饰模式是继承的一个替代模式，装饰模式可以动态扩展一个实现类的功能。
## 缺点
    多层装饰比较复杂。