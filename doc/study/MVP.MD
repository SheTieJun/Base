简化MVP 其中的步骤

```
所有的Presenter 持有的的都是 IView ，没必要每个界面定义一个特殊的IView 

我们做的是为了解耦view 和 model。但是如果定义很多接口就得不偿失了

所有所有的界面都是IView，都通过updateView()更新
```

Presenter 持有IView 和 对应的model

当需要数据变化引起界面变化时 在Presenter 中 直接调用 view 的updateView() 方法进行更新界面就好

```
@Keep
interface IView {
   ......

    /**
     * (唯一更新view的方法)
     * 在每次接到信息时,把信息显示view上,---更新view
     * @param message 接收到的信息
     */
    fun updateView(message: Message)
}
```
