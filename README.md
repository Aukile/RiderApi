## Rider Api
### Geo物品简易化模板/骑士盔甲模板  
ps:master分支为neoforge1.21.1版本
* Geo物品简易化模板  
Geo物品模板节省了制作Model类和Renderer类  
可使用[IGeoItem](https://github.com/Aukile/RiderApi/blob/master/src/main/java/net/ankrya/rider_api/interfaces/geo/IGeoItem.java)接口使用，模组中有[BaseGeoItem](https://github.com/Aukile/RiderApi/blob/master/src/main/java/net/ankrya/rider_api/item/base/BaseGeoItem.java)作为使用例  

| 方法                                  | 用途         |
|-------------------------------------|------------|
| playAnimation、playAnimationAndReset | 简易的播放动画方法  |
| visibilityBones                     | 隐藏骨骼组的方法   |
| autoGlow                            | 添加自动发光层的方法 |
| getRenderType                       | 修改渲染类型方法   |
| getRenderLayers                     | 添加渲染层方法    |
* 盔甲简易化模板  
盔甲可使用[IGeoArmor](https://github.com/Aukile/RiderApi/blob/master/src/main/java/net/ankrya/rider_api/interfaces/geo/IGeoArmor.java)其为[IGeoItem](https://github.com/Aukile/RiderApi/blob/master/src/main/java/net/ankrya/rider_api/interfaces/geo/IGeoItem.java)的子类  
增添了更多的常用功能

| 方法              | 用途              |
|-----------------|-----------------|
| visibilityBones | 作为方法的隐藏块（用得到吗？） |
| transformations | 旋转时会执行的方法       |
| lightBones      | 骨骼块发光           |
### 骑士盔甲模板
* [BaseRiderArmorBase](https://github.com/Aukile/RiderApi/blob/master/src/main/java/net/ankrya/rider_api/item/base/armor/BaseRiderArmorBase.java)为模板的底版  
主要用途为检测是否穿戴整套装备（可覆写getArmorClass方法设定）  
以腰带[BaseDriver](https://github.com/Aukile/RiderApi/blob/master/src/main/java/net/ankrya/rider_api/item/base/armor/BaseDriver.java)和盔甲[BaseRiderArmor](https://github.com/Aukile/RiderApi/blob/master/src/main/java/net/ankrya/rider_api/item/base/armor/BaseRiderArmor.java)进行为了两个分支  
* [BaseDriver](https://github.com/Aukile/RiderApi/blob/master/src/main/java/net/ankrya/rider_api/item/base/armor/BaseDriver.java)  
暂时无功能
* [BaseRiderArmor](https://github.com/Aukile/RiderApi/blob/master/src/main/java/net/ankrya/rider_api/item/base/armor/BaseRiderArmor.java)  
主要提供收纳变身时所穿戴物品的功能、自动清除功能、添加药水集和进行穿脱的方法（提供了[RiderArmorEquipEvent](https://github.com/Aukile/RiderApi/blob/master/src/main/java/net/ankrya/rider_api/api/event/RiderArmorEquipEvent.java)和[RiderArmorRemoveEvent](https://github.com/Aukile/RiderApi/blob/master/src/main/java/net/ankrya/rider_api/api/event/RiderArmorRemoveEvent.java)的监听事件，  
ps:因为是静态方法呢，还有更简易的办法嘛？）

| 方法            | 用途              |
|---------------|-----------------|
| equip和unequip | 自动判定是否有收纳的盔甲并退还 |
| getEffects    | 设定药水集（夜视自动12秒）  |
| getSlot       | 记录了注册的槽位，看着用？   |

### 同步数据
提供了简单手写同步数据的方法  
使用[DataInitEvent](https://github.com/Aukile/RiderApi/blob/master/src/main/java/net/ankrya/rider_api/api/event/DataInitEvent.java)中的register即可注册数据  
[Variables](https://github.com/Aukile/RiderApi/blob/master/src/main/java/net/ankrya/rider_api/data/Variables.java)中的getVariable获取数据，setVariable设置数据  
自己记好数据类型捏~dwd
### 玩家动画
[PlayerAnimator](https://github.com/Aukile/RiderApi/blob/master/src/main/java/net/ankrya/rider_api/compat/animation/PlayerAnimator.java)中预备了使用玩家动画的方法playerAnimation（应该能用吧？）
### Geo胸甲手臂渲染
自动识别穿戴的如果是Geckolib中的胸甲，会自动渲染手臂
### 服务器刻中运行
* [WaitToRun](https://github.com/Aukile/RiderApi/blob/master/src/main/java/net/ankrya/rider_api/help/runnable/WaitToRun.java)等待刻运行
* [TickToRun](https://github.com/Aukile/RiderApi/blob/master/src/main/java/net/ankrya/rider_api/help/runnable/TickToRun.java)间隔刻运行
* [ConditionsToRun](https://github.com/Aukile/RiderApi/blob/master/src/main/java/net/ankrya/rider_api/help/runnable/ConditionsToRun.java)条件刻运行（灵活）
* [ConditionsToCall](https://github.com/Aukile/RiderApi/blob/master/src/main/java/net/ankrya/rider_api/help/runnable/ConditionsToCall.java)条件刻运算？我暂时没用过
### 工具箱~
全在[GJ](https://github.com/Aukile/RiderApi/blob/master/src/main/java/net/ankrya/rider_api/help/GJ.java)里面，应该不会是石山代码吧QAQ  
继承[GJ](https://github.com/Aukile/RiderApi/blob/master/src/main/java/net/ankrya/rider_api/help/GJ.java)的子类可使用[INMessage](https://github.com/Aukile/RiderApi/blob/master/src/main/java/net/ankrya/rider_api/message/NMessageCreater.java)网络包中的[OnetimeMessage](https://github.com/Aukile/RiderApi/blob/master/src/main/java/net/ankrya/rider_api/message/ex_message/OnetimeMessage.java)网络包进行同步运行  
主要用途应该是做技能？吧？  
其中还包括：
* 时停时缓控制器
* 高级粒子使用例/创建方法

### 懒人网络包
* [MessageCreater](https://github.com/Aukile/RiderApi/blob/master/src/main/java/net/ankrya/rider_api/message/MessageCreater.java)创建它使用[IFMessage](https://github.com/Aukile/RiderApi/blob/master/src/main/java/net/ankrya/rider_api/interfaces/message/IFMessage.java)实例作为参数，仅支持无参的网络包，自动编/解码
* [EXMessageCreater](https://github.com/Aukile/RiderApi/blob/master/src/main/java/net/ankrya/rider_api/message/EXMessageCreater.java)创建它使用[IEXMessage](https://github.com/Aukile/RiderApi/blob/master/src/main/java/net/ankrya/rider_api/interfaces/message/IEXMessage.java)实例作为参数，自动解码，编码时必须使用[IEXMessage](https://github.com/Aukile/RiderApi/blob/master/src/main/java/net/ankrya/rider_api/interfaces/message/IEXMessage.java)中提供的方法  
且有[AllPacktForIEX](https://github.com/Aukile/RiderApi/blob/master/src/main/java/net/ankrya/rider_api/message/ex_message/AllPacktForIEX.java)网络包自动从发送服务器再发送至客户端，缺点是会进行多次读写？
* [NMessageCreater](https://github.com/Aukile/RiderApi/blob/master/src/main/java/net/ankrya/rider_api/message/NMessageCreater.java)创建它使用[INMessage](https://github.com/Aukile/RiderApi/blob/master/src/main/java/net/ankrya/rider_api/message/NMessageCreater.java)实例作为参数，自动解码，[IEXMessage](https://github.com/Aukile/RiderApi/blob/master/src/main/java/net/ankrya/rider_api/interfaces/message/IEXMessage.java)的衍生版，编码时使用一个autoWriteAll方法把buff和参数全丢进去就可以自动解析了  
且有[AllPackt](https://github.com/Aukile/RiderApi/blob/master/src/main/java/net/ankrya/rider_api/message/ex_message/AllPackt.java)网络包自动从发送服务器再发送至客户端，套在[INMessage](https://github.com/Aukile/RiderApi/blob/master/src/main/java/net/ankrya/rider_api/message/NMessageCreater.java)外即可使用，相比于[IEXMessage](https://github.com/Aukile/RiderApi/blob/master/src/main/java/net/ankrya/rider_api/interfaces/message/IEXMessage.java)网络包没有大量读写次数的问题，缺点是网络包的参数最多不能超过9个  
使用例：[PlayerAnimationMessage](https://github.com/Aukile/RiderApi/blob/master/src/main/java/net/ankrya/rider_api/message/ex_message/PlayerAnimationMessage.java)