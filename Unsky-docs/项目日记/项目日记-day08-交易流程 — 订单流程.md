
> 日期：2026/05/07----05/08
> 目标：用户能下单、能管理订单状态，为后续评价与信用体系做准备。

---

## 一、订单基础结构搭建

### 1.1 订单表设计

在`DataGrip`里面执行下面的脚本完成建表：

```mysql
-- ============================================  
-- UnSky Market - Day08 订单表建表脚本  
-- 数据库：unsky_market  
-- 对应后端实体：orders  
-- 字符集：utf8mb4（支持 emoji 和特殊字符）  
-- ============================================  
  
CREATE TABLE orders (  
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '订单ID',  
    order_no VARCHAR(64) NOT NULL COMMENT '订单编号',  
    buyer_id BIGINT NOT NULL COMMENT '买家ID',  
    seller_id BIGINT NOT NULL COMMENT '卖家ID',  
    product_id BIGINT NOT NULL COMMENT '商品ID',  
    total_price DECIMAL(10, 2) NOT NULL COMMENT '订单总金额',  
    status INT NOT NULL DEFAULT 0 COMMENT '订单状态：0待付款，1待发货，2待收货，3已完成，-1已取消',  
    logistics_no VARCHAR(100) DEFAULT NULL COMMENT '物流单号',  
    expire_time DATETIME DEFAULT NULL COMMENT '订单过期时间',  
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',  
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',  
    UNIQUE KEY uk_order_no (order_no),  
    KEY idx_buyer_id (buyer_id),  
    KEY idx_seller_id (seller_id),  
    KEY idx_product_id (product_id),  
    KEY idx_status (status)  
) COMMENT = '订单表';
```

### 1.2 订单状态字段设计

> **📌 定义一个订单状态机，🎯 用“可读的名字”替代“难懂的数字”，统一订单状态标准，保证整个订单流程可控、可维护**

- 创建`order`模块，在里面添加`constant`包，并在包下新建订单状态常量类：

```Java
/**
 * 订单状态常量
 */
public class OrderStatus {
    //待付款
    public static final Integer WAIT_PAY = 0;
    //待发货
    public static final Integer WAIT_SHIP = 1;
    //待收货
    public static final Integer WAIT_RECEIVE = 2;
    //已完成
    public static final Integer FINISHED = 3;
    //已取消
    public static final Integer CANCELED = -1;
}

```

> 用常量类来代替订单的状态，简化对订单的维护；目前只涉及上面订单状态常量，其他状态后面在进行优化✅

### 1.3 模块基础结构搭建

1. 创建Order实体类

```Java
@Data  
@TableName("orders")  
public class Order {  
    //订单ID  
    private Long id;  
    //订单编号  
    private String orderNo;  
    //买家ID  
    private Long buyerId;  
    //卖家ID  
    private Long sellerId;  
    //商品ID  
    private Long productId;  
    //订单总金额  
    private BigDecimal totalPrice;  
    //订单状态：0待付款，1待发货，2待收货，3已完成，-1已取消  
    private Integer status;  
    //物流单号  
    private String logisticsNo;  
    //订单过期时间  
    private LocalDateTime expireTime;  
    //创建时间  
    private LocalDateTime createTime;  
    //更新时间  
    private LocalDateTime updateTime;  
}
```

2. 创建`OrderMapper`数据访问层

```Java
@Mapper  
public interface OrderMapper extends BaseMapper<Order> {}
```

3. 创建`OrderService`接口

```Java
public interface OrderService {}
```

4. 创建`OrderServiceImpl`接口实现类

```Java
@Service  
@RequiredArgsConstructor  
public class OrderServiceImpl implements OrderService {}
```

5. 创建`OrderController`接口控制层

```Java
@RestController  
@RequestMapping("/api/order")  
@RequiredArgsConstructor  
public class OrderController {  
    private final OrderService orderService;  
}
```

> 这一步骤先把模块的基础结构搭建好，后面的多个接口直接在这个框架里延伸扩展

### 1.4 OrderVO 返回结构设计

1. 创建`OrderVO`接口返回给前端的结构

```Java
@Data  
public class OrderVO {  
    // 订单ID  
    private Long id;  
    // 订单编号  
    private String orderNo;  
    // 商品ID  
    private Long productId;  
    // 商品标题  
    private String productTitle;  
    // 商品图片  
    private String productImage;  
    // 买家ID  
    private Long buyerId;  
    // 卖家ID  
    private Long sellerId;  
    // 订单总金额  
    private BigDecimal totalPrice;  
    // 订单状态  
    private Integer status;  
    // 订单状态文本  
    private String statusText;  
    // 物流单号  
    private String logisticsNo;  
    // 订单过期时间  
    private LocalDateTime expireTime;  
    // 创建时间  
    private LocalDateTime createTime;  
}
```

> 这个VO要与前面的实体类做区分，实体类是跟数据库对应的，VO是返回给前端的数据
> 关于`statusText`，后面转换 VO 时会把 `status` 转成 `statusText`

---

## 二、立即购买下单流程实现

### 2.1 创建订单 DTO 设计

🎯 **定义前端创建订单时传什么参数**

- 创建`CreateOrderDTO`前端返回结构参数

```Java
@Data
public class CreateOrderDTO {
    // 商品ID
    private Long productId;
}
```

> 📌 目前是立即购买的场景，用户在商品详情页点击立即购买，后端直接通过 `productId` 就可以查到：商品价格、卖家ID和商品状态；买家ID依旧从`token`里上传；订单金额依照后端数据库价格为准，所以不用前端上传 (¬‿¬)   

### 2.2 下单前商品状态校验

1. 在 `OrderService` 声明创建订单方法

```Java
/**  
 * 创建订单  
 * @param createOrderDTO  
 * @param buyerId  
 * @return  
 */  
Result<Void> createOrder(CreateOrderDTO createOrderDTO, Long buyerId);
```

2. 在 `OrderServiceImpl` 里面实现接口方法

```Java
/**  
 * 创建订单  
 * @param createOrderDTO  
 * @param buyerId  
 * @return  
 */  
@Override  
public Result<Void> createOrder(CreateOrderDTO createOrderDTO, Long buyerId) {  
    // 1. 判断商品ID是否为空  
    if (createOrderDTO == null || createOrderDTO.getProductId() == null) {  
        return Result.error("商品ID不能为空");  
    }   
    // 2. 查询商品是否存在  
    Product product = productMapper.selectById(createOrderDTO.getProductId());  
    if (product == null) {  
        return Result.error("商品不存在");  
    }  
    // 3. 判断商品是否上架  
    if (product.getStatus() == null || product.getStatus() != 1) {  
        return Result.error("商品当前不可购买");  
    }  
    return Result.success();  
}
```

### 2.3 防止用户购买自己的商品

```Java
// 4. 防止用户购买自己发布的商品
if (product.getSellerId().equals(buyerId)) {
    return Result.error("不能购买自己发布的商品");
}
```

> 在二手交易平台中，卖家不能买自己发布的商品，否则会出现问题，所以必须在创建订单时进行校验，防止后面影响day09的信用体系

### 2.4 立即购买接口实现

1. 在`OrderServiceImpl`里面补充依赖

```Java
private final OrderMapper orderMapper;
private final ProductMapper productMapper;
```

2. 在`OrderServiceImpl`里面创建订单对象并插入订单

```java
    // 5. 创建订单对象
    Order order = new Order();
    order.setOrderNo(UUID.randomUUID().toString().replace("-", ""));
    order.setBuyerId(buyerId);
    order.setSellerId(product.getSellerId());
    order.setProductId(product.getId());
    order.setTotalPrice(product.getPrice());
    order.setStatus(OrderStatus.WAIT_PAY);
    order.setExpireTime(LocalDateTime.now().plusMinutes(30));
    
    // 6. 插入订单
    orderMapper.insert(order);
    
    return Result.success();
}
```

>  ⚠️ 注意：
>  1.`orderNo` 不直接用数据库 id，这是生成一个订单编号，给前端和用户看的
>  2.`totalPrice` 从商品表取，不能由用户传，否则用户可以改价格
>  3. 初始状态是待付款，上面代码直接设置默认初始状态为`WAIT_PAY`未付款
>  4. 设置过期时间，上面代码表示订单 30 分钟内未付款，后面可以自动取消

3. 在 `OrderController` 添加立即购买接口

```Java
/**  
 * 立即购买创建订单  
 * @param createOrderDTO  
 * @param request  
 * @return  
 */  
@PostMapping("/create")  
public Result<Void> createOrder(@RequestBody CreateOrderDTO createOrderDTO,  
                                HttpServletRequest request) {  
    String token = request.getHeader("token");  
    Long buyerId = JwtUtil.getUserIdFromToken(token);  
    return orderService.createOrder(createOrderDTO, buyerId);  
}
```

> `Controller` 只负责：1.接收参数  2.解析当前用户  3.调用 `Service`  4.返回结果

### 2.5 接口测试与验证

- `Apifox`测试截图

![[Pasted image 20260507210226.png]]

> 我使用的是`Id` =  5,"王五爷"同学的账号来进行`productId`= 2 的商品订单创建

- 数据库验证

![[Pasted image 20260507210654.png]]

> 后面还有四个字段`status`、`expire_time`、`create_time`、`update_time`就不展示了

> 同时测试了商品不存在、商品未上架、购买自己商品等异常场景，接口均能返回对应错误信息。

---

## 三、订单查询与状态流转实现

### 3.1 我的订单列表接口实现

1. 在`OrderService`里面声明查询我的订单列表方法

```Java
/**  
 * 查询我的订单列表  
 * @param userId  
 * @return  
 */  
Result<List<OrderVO>> listMyOrders(Long userId);
```

2. 在 `OrderServiceImpl` 接口实现类里实现方法

```Java
/**  
 * 查询我的订单列表  
 * @param userId  
 * @return  
 */  
@Override  
public Result<List<OrderVO>> listMyOrders(Long userId) {  
    // 1. 查询当前用户作为买家的订单  
    LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();  
    wrapper.eq(Order::getBuyerId, userId);  
    wrapper.orderByDesc(Order::getCreateTime);  
    List<Order> orderList = orderMapper.selectList(wrapper);  
    // 2. 转换为 VO
        List<OrderVO> voList = orderList.stream()  
            .map(this::convertToVO)  
            .collect(Collectors.toList());  
            
    return Result.success(voList);  
}
```

3. 在`OrderServiceImpl` 接口实现类里添加`convertToVO` 方法

```Java
// 转换订单VO
private OrderVO convertToVO(Order order) {
    OrderVO vo = new OrderVO();
    
    vo.setId(order.getId());
    vo.setOrderNo(order.getOrderNo());
    vo.setProductId(order.getProductId());
    vo.setBuyerId(order.getBuyerId());
    vo.setSellerId(order.getSellerId());
    vo.setTotalPrice(order.getTotalPrice());
    vo.setStatus(order.getStatus());
    vo.setStatusText(getStatusText(order.getStatus()));
    vo.setLogisticsNo(order.getLogisticsNo());
    vo.setExpireTime(order.getExpireTime());
    vo.setCreateTime(order.getCreateTime());
    
    // 查询商品信息
    Product product = productMapper.selectById(order.getProductId());
    if (product != null) {
        vo.setProductTitle(product.getTitle());
        vo.setProductImage(product.getImages());
    }
    return vo;
}
```

4.  添加订单状态文本转换方法`getStatusText`

```Java
// 转换订单状态文本
private String getStatusText(Integer status) {
    if (status == null) {
        return "未知状态";
    }
    
    switch (status) {
        case 0:
            return "待付款";
        case 1:
            return "待发货";
        case 2:
            return "待收货";
        case 3:
            return "已完成";
        case -1:
            return "已取消";
        default:
            return "未知状态";
    }
}
```

5. 在 `OrderController` 添加接口

```Java
/**  
 * 查询我的订单列表  
 * @param request  
 * @return  
 */  
@GetMapping("/list")  
public Result<List<OrderVO>> listMyOrders(HttpServletRequest request) {  
    String token = request.getHeader("token");  
    Long userId = JwtUtil.getUserIdFromToken(token);  
    return orderService.listMyOrders(userId);  
}
```

> ⭐ 我梳理一下思路：首先是查找这个订单，有两个方面，一个是买家查找这个订单，这里要做一个vo转换，因为如果直接使用数据库中的实体类就不会返回图片之类的数据，所以要重新封装一个vo来返回数据，这里就是做一个转换，然后要注意vo里面的状态使用数字表示的，前端返回的话用户会看不懂这个状态，所以需要把它转换成文本，所以有需要一个方法，将订单状态转换成汉字并返回，最后再添加接口就可以了，接口需要token验证来查看登录；另外还有一个卖家查找订单，这里先不涉及。

6. 在Apifox里面测试并验证接口

![[Pasted image 20260508133238.png]]

> 这里使用的是`Id` =  5,"王五爷"同学的账号来进行查看我的订单列表的操作，成功返回该同学的收藏订单

### 3.2 按订单状态筛选查询

1. 修改`OrderService`中的声明方法

```Java
/**  
 * 查询我的订单列表  
 * @param userId  
 * @param status 可选状态  
 * @return  
 */  
Result<List<OrderVO>> listMyOrders(Long userId , Integer status);
```

2. 修改`OrderServiceImpl`接口实现类中的方法

```Java
/**  
 * 查询我的订单列表  
 * @param userId  
 * @param status  
 * @return  
 */  
@Override  
public Result<List<OrderVO>> listMyOrders(Long userId, Integer status) {  
    // 1. 查询当前用户作为买家的订单  
    LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();  
    wrapper.eq(Order::getBuyerId, userId);  
    
    // 2. 如果传了状态，就按状态筛选  
    if (status != null) {  
        wrapper.eq(Order::getStatus, status);  
    }  
    
    wrapper.orderByDesc(Order::getCreateTime);  
    
    List<Order> orderList = orderMapper.selectList(wrapper);  
    
    // 3. 转换为 VO
        List<OrderVO> voList = orderList.stream()  
            .map(this::convertToVO)  
            .collect(Collectors.toList());  
            
    return Result.success(voList);  
}
```

4. 修改`OrderController`中的接口

```Java
/**  
 * 查询我的订单列表  
 * @param status  
 * @param request  
 * @return  
 */  
@GetMapping("/list")  
public Result<List<OrderVO>> listMyOrders(@RequestParam(required = false) Integer status,  
                                          HttpServletRequest request) {  
    String token = request.getHeader("token");  
    Long userId = JwtUtil.getUserIdFromToken(token);  
    return orderService.listMyOrders(userId, status);  
}
```

>  📌 `status` 是可选参数，不传的话就表示查找全部订单；传的话就表示只查找相应订单，这样同一个接口就能支持多个筛选场景，不需要写很多接口

5. 在Apifox里测试并验证

![[Pasted image 20260508144651.png]]

> 这里设置了可选参数`status`,可以按照用户意愿进行特定查找；目前还没有做取消订单的接口，所以无法继续验证取消订单接口(・_・;)

### 3.3 取消订单接口实现

1. 在 `OrderService` 添加取消订单方法声明

```Java
/**  
 * 取消订单  
 * @param orderId  
 * @param userId  
 * @return  
 */  
Result<Void> cancelOrder(Long orderId, Long userId);
```

2. 在 `OrderServiceImpl` 接口实现类里添加取消订单方法

```Java
/**  
 * 取消订单  
 * @param orderId  
 * @param userId  
 * @return  
 */  
@Override  
public Result<Void> cancelOrder(Long orderId, Long userId) {  
    // 1. 判断订单ID是否为空  
    if (orderId == null) {  
        return Result.error("订单ID不能为空");  
    }   
    // 2. 查询订单是否存在  
    Order order = orderMapper.selectById(orderId);  
    if (order == null) {  
        return Result.error("订单不存在");  
    }  
    // 3. 判断是否是当前用户自己的订单  
    if (!order.getBuyerId().equals(userId)) {  
        return Result.error("只能取消自己的订单");  
    }  
    // 4. 只有待付款订单可以取消  
    if (!order.getStatus().equals(OrderStatus.WAIT_PAY)) {  
        return Result.error("当前订单状态不可取消");  
    }  
    // 5. 修改订单状态为已取消  
    order.setStatus(OrderStatus.CANCELED);  
    orderMapper.updateById(order);  
    
    return Result.success();  
}
```

>  ⚠️ 注意：
> 1. 不能只根据订单id取消，防止别人知道订单id就可以取消该订单；
> 2. 不是所有的订单都可以取消，当前只允许取消待付款的订单 

3. 在 `OrderController` 添加取消订单的接口

```Java
/**  
 * 取消订单  
 * @param orderId  
 * @param request  
 * @return  
 */  
@PutMapping("/cancel/{orderId}")  
public Result<Void> cancelOrder(@PathVariable Long orderId,  
                                HttpServletRequest request) {  
    String token = request.getHeader("token");  
    Long userId = JwtUtil.getUserIdFromToken(token);  
    return orderService.cancelOrder(orderId, userId);  
}
```

4. 在`Apifox`里测试并验证取消订单接口

![[Pasted image 20260508150913.png]]

- 数据库验证

> 数据库关于`orderId` = 4的订单成功被删除，验证成功；其他的返回结果就不在这里展示了，比如说："当前订单状态不可取消"、"只能取消自己的订单"等  (ಡωಡ)

### 3.4 卖家发货接口实现

🎯 **卖家填写物流单号，把订单从待发货改为待收货**

1. 创建发货返回结果`ShipOrderDTO`

```JAVA 
@Data  
public class ShipOrderDTO {  
    // 订单ID  
    private Long orderId;  
    // 物流单号  
    private String logisticsNo;  
}
```

2. 在 `OrderService` 添加卖家发货接口的方法

```Java
/**  
 * 卖家发货  
 * @param shipOrderDTO  
 * @param sellerId  
 * @return  
 */  
Result<Void> shipOrder(ShipOrderDTO shipOrderDTO, Long sellerId);
```

3. 在 `OrderServiceImpl`接口实现类里实现卖家发货的方法

```Java
/**  
 * 卖家发货  
 * @param shipOrderDTO  
 * @param sellerId  
 * @return  
 */  
@Override  
public Result<Void> shipOrder(ShipOrderDTO shipOrderDTO, Long sellerId) {  
    // 1. 判断参数是否为空  
    if (shipOrderDTO == null || shipOrderDTO.getOrderId() == null) {  
        return Result.error("订单ID不能为空");  
    }   
    // 2. 判断物流单号是否为空  
    if (shipOrderDTO.getLogisticsNo() == null || shipOrderDTO.getLogisticsNo().trim().isEmpty()) {  
        return Result.error("物流单号不能为空");  
    }  
    // 3. 查询订单是否存在  
    Order order = orderMapper.selectById(shipOrderDTO.getOrderId());  
    if (order == null) {  
        return Result.error("订单不存在");  
    }  
    // 4. 判断是否是当前卖家的订单  
    if (!order.getSellerId().equals(sellerId)) {  
        return Result.error("只能发货自己的订单");  
    }  
    // 5. 只有待发货订单可以发货  
    if (!order.getStatus().equals(OrderStatus.WAIT_SHIP)) {  
        return Result.error("当前订单状态不可发货");  
    }  
    // 6. 填写物流单号并修改状态为待收货  
    order.setLogisticsNo(shipOrderDTO.getLogisticsNo());  
    order.setStatus(OrderStatus.WAIT_RECEIVE);  
    orderMapper.updateById(order);  
    
    return Result.success();  
}
```

> 不把卖家 id 放进 `ShipOrderDTO`，是因为：
> `sellerId` 属于当前登录用户身份，必须由后端从 token 中解析，不能由前端传入；DTO 只负责封装前端本次操作真正需要提交的数据，比如订单 id 和物流单号。

4. 在 `OrderController` 添加卖家发货的接口

```Java
/**  
 * 卖家发货  
 * @param shipOrderDTO  
 * @param request  
 * @return  
 */  
@PutMapping("/ship")  
public Result<Void> shipOrder(@RequestBody ShipOrderDTO shipOrderDTO,  HttpServletRequest request) {  
    String token = request.getHeader("token");  
    Long sellerId = JwtUtil.getUserIdFromToken(token);  
    return orderService.shipOrder(shipOrderDTO, sellerId);  
}
```

> 由于目前没有做支付接口，刚创建的订单是待付款，发货状态要求的是待发货，所以测试前需要在数据库中把状态手动改为1

5. 在Apifox里面测试并验证卖家发货接口

![[Pasted image 20260508153620.png]]

- 数据库验证

> 数据库中的`orderId`= 1，`logistics_no`已经成功变为`SF123456789`，并且status 已经从 1 变为 2，说明订单已经从“待发货”流转为“待收货”，完整流程验证成功 (≧▽≦) 

### 3.5 买家确认收货接口实现

🎯 **买家确认收到商品，把订单从待收货改为已完成**

1. 在 `OrderService` 里添加买家确认收获的方法声明

```java 
/**  
 * 买家确认收货  
 * @param orderId  
 * @param buyerId  
 * @return  
 */  
Result<Void> confirmOrder(Long orderId, Long buyerId);
```

2. 在 `OrderServiceImpl` 实现买家确认收货的实现方法

```Java
/**  
 * 买家确认收货  
 * @param orderId  
 * @param buyerId  
 * @return  
 */  
@Override  
public Result<Void> confirmOrder(Long orderId, Long buyerId) {  
    // 1. 判断订单ID是否为空  
    if (orderId == null) {  
        return Result.error("订单ID不能为空");  
    }  
    // 2. 查询订单是否存在  
    Order order = orderMapper.selectById(orderId);  
    if (order == null) {  
        return Result.error("订单不存在");  
    }  
    // 3. 判断是否是当前买家的订单  
    if (!order.getBuyerId().equals(buyerId)) {  
        return Result.error("只能确认自己的订单");  
    }  
    // 4. 只有待收货订单可以确认收货  
    if (!order.getStatus().equals(OrderStatus.WAIT_RECEIVE)) {  
        return Result.error("当前订单状态不可确认收货");  
    }  
    // 5. 修改订单状态为已完成  
    order.setStatus(OrderStatus.FINISHED);  
    orderMapper.updateById(order);  
    
    return Result.success();  
}
```

3. 在 `OrderController` 添加买家确认收货的接口

```Java
/**  
 * 买家确认收货  
 * @param orderId  
 * @param request  
 * @return  
 */  
@PutMapping("/confirm/{orderId}")  
public Result<Void> confirmOrder(@PathVariable Long orderId,  
                                 HttpServletRequest request) {  
    String token = request.getHeader("token");  
    Long buyerId = JwtUtil.getUserIdFromToken(token);  
    return orderService.confirmOrder(orderId, buyerId);  
}
```

4. 在`Apifox`里面进行买家确认收货的接口测试

![[Pasted image 20260508163539.png]]

- 数据库验证

> 数据库中的状态字段`status`成功变为3，状态变为"已完成"

> 我使用的是`Id` =  5,"王五爷"同学的账号来进行买家确认收货，该同学现在成功收到商品并确认收货，验证成功  (＾▽＾)

>  ⚠️ 注意：测试前订单的状态必须是2 待收货，这样才可以进行确认收货

### 3.6 王五爷的订单测试线

 🎯 为了验证 Day08 订单流程是否完整，我使用 `id = 5` 的用户“王五爷”作为买家账号，围绕同一个商品 `id = 2`，也就是“小明同学”的小米笔记本 Pro，完成了一次完整的订单状态流转测试。

- 测试主线如下：

```text
王五爷登录
  ↓
立即购买商品id=2 ---> "小明同学"的小米笔记本pro
  ↓
生成待付款订单 status = 0
  ↓
手动模拟支付完成，将订单状态改为待发货 status = 1
  ↓
卖家填写物流单号并发货
  ↓
订单状态变为待收货 status = 2
  ↓
王五爷确认收货
  ↓
订单状态变为已完成 status = 3
```

> ✅ 本次测试中，王五爷作为买家完成了“创建订单 → 等待卖家发货 → 确认收货”的完整买家侧流程；卖家侧则完成了“待发货 → 填写物流单号 → 发货”的状态流转。
> 
> 📌 由于当前项目还没有实现支付模块，所以 `status = 0` 到 `status = 1` 的过程暂时通过数据库手动修改模拟，后续可以在支付模块或订单支付接口中补全。
> 
> 🚀 至此，一条订单从创建到完成的核心状态流转已经跑通。( •̀ ω •́ )✌！！！

---

## 四、购物车结算与订单流程梳理（⭐）

### 4.1 购物车结算接口实现

🚀  核心：**一个购物车商品生成一条订单**

```text
用户在购物车中选择商品
  ↓
后端根据 cartId 查购物车记录
  ↓
再根据 productId 查商品
  ↓
批量生成订单
```

1. 创建购物车结算`CheckoutOrderDTO`

```Java
@Data  
public class CheckoutOrderDTO {    
    // 要结算的购物车ID列表  
    private List<Long> cartIds;  
}
```

2. 在 `OrderService` 添加购物车结算的方法

```Java
/**  
 * 购物车结算  
 * @param checkoutOrderDTO  
 * @param buyerId  
 * @return  
 */  
Result<Void> checkoutOrder(CheckoutOrderDTO checkoutOrderDTO, Long buyerId);
```

3. 在 `OrderServiceImpl` 里实现购物车结算方法

```Java
/**  
 * 购物车结算  
 * @param checkoutOrderDTO  
 * @param buyerId  
 * @return  
 */  
@Override  
@Transactional  
public Result<Void> checkoutOrder(CheckoutOrderDTO checkoutOrderDTO, Long buyerId) {  
    // 1. 判断购物车ID列表是否为空  
    if (checkoutOrderDTO == null || checkoutOrderDTO.getCartIds() == null || checkoutOrderDTO.getCartIds().isEmpty()) {  
        return Result.error("请选择要结算的商品");  
    }   
    // 2. 遍历购物车ID，逐个生成订单  
    for (Long cartId : checkoutOrderDTO.getCartIds()) {  
        Cart cart = cartMapper.selectById(cartId);  
        // 3. 判断购物车记录是否存在  
        if (cart == null) {  
            return Result.error("购物车记录不存在");  
        } 
        // 4. 判断是否是当前用户自己的购物车记录  
        if (!cart.getUserId().equals(buyerId)) {  
            return Result.error("只能结算自己的购物车商品");  
        }  
        // 5. 查询商品  
        Product product = productMapper.selectById(cart.getProductId());  
        if (product == null) {  
            return Result.error("商品不存在");  
        }  
        // 6. 判断商品是否上架  
        if (product.getStatus() == null || product.getStatus() != 1) {  
            return Result.error("商品当前不可购买");  
        }  
        // 7. 防止购买自己发布的商品  
        if (product.getSellerId().equals(buyerId)) {  
            return Result.error("不能购买自己发布的商品");  
        }  
        // 8. 创建订单  
        Order order = new Order();  
        order.setOrderNo(UUID.randomUUID().toString().replace("-", ""));  
        order.setBuyerId(buyerId);  
        order.setSellerId(product.getSellerId());  
        order.setProductId(product.getId());  
        order.setTotalPrice(product.getPrice());  
        order.setStatus(OrderStatus.WAIT_PAY);  
        order.setExpireTime(LocalDateTime.now().plusMinutes(30));  
        orderMapper.insert(order);  
        // 9. 结算成功后删除购物车记录  
        cartMapper.deleteById(cartId);  
    }  
    return Result.success();  
}
```

> 加上`@Transactional`事务管理是保证生成订单和删除购物车记录是一起成功的，再批量操作中避免出现订单生成一半，购物车删除了一半这种情况

4. 在 `OrderController` 添加购物车结算的接口

```Java
/**  
 * 购物车结算  
 * @param checkoutOrderDTO  
 * @param request  
 * @return  
 */  
@PostMapping("/checkout")  
public Result<Void> checkoutOrder(@RequestBody CheckoutOrderDTO checkoutOrderDTO,  
                                  HttpServletRequest request) {  
    String token = request.getHeader("token");  
    Long buyerId = JwtUtil.getUserIdFromToken(token);  
    return orderService.checkoutOrder(checkoutOrderDTO, buyerId);
```

5. 在`Apifox`里面进行测试与验证

![[Pasted image 20260508195133.png]]

> 购物车结算之后，我的购物车列表中的相应记录也被删除了，再次结算显示的是"购物车记录不存在"，购物车结算验证成功 (*^▽^*)
 
### 4.2 购物车数据与订单数据的关系

- 购物车表 `cart` 和订单表 `orders` 不是同一种数据。

`cart` 表记录的是用户的购买意向：

```text
user_id
product_id
create_time
```

> 它表示：某个用户把某个商品加入了购物车，但还没有真正发起交易。

`orders` 表记录的是正式交易数据：

```text
order_no
buyer_id
seller_id
product_id
total_price
status
expire_time
```

> 它表示：用户已经基于某个商品创建了一条订单，后续会进入待付款、待发货、待收货、已完成等状态流转。

> 🚀 当前项目中，一个购物车记录对应一个商品；由于二手交易平台以“一物一件”为主，所以一个购物车记录结算后生成一条订单。结算成功后删除购物车记录，是因为这条数据已经从“购买意向”转化成了“正式订单”，不应该继续停留在购物车中。

### 4.3 订单状态机流转规则

Day08 的订单流程本质上是围绕订单状态字段 `status` 展开的。

当前项目中，订单状态分为：
- `0`：待付款
- `1`：待发货
- `2`：待收货
- `3`：已完成
- `-1`：已取消

一条正常订单的主流程是：

`0 待付款` → `1 待发货` → `2 待收货` → `3 已完成`

其中：

- 创建订单后，订单初始状态为 `0 待付款`；
- 支付完成后，订单进入 `1 待发货`；
- 卖家发货后，订单进入 `2 待收货`；
- 买家确认收货后，订单进入 `3 已完成`。

>  ⚠️ 当前项目暂时没有实现支付模块，所以 `0 待付款` → `1 待发货` 的过程在测试时通过数据库手动修改状态模拟。

取消订单属于异常分支，目前只允许：

`0 待付款` → `-1 已取消`

也就是说，只有还没有付款的订单可以取消；如果订单已经进入待发货、待收货或已完成状态，就不能再随意取消。

状态机的作用是限制订单只能按照规定路径流转，避免出现“待付款订单直接确认收货”“已完成订单再次发货”这类错误状态。
### 4.4 超时取消任务设计

> ⚠️ 订单创建后，初始状态是 `0 待付款`。如果用户长时间没有支付，这条订单不应该一直停留在待付款状态，因此需要设计超时取消机制。

当前订单表中已经预留了 `expire_time` 字段，用来记录订单的过期时间。在创建订单时，后端会设置：

`expire_time = 当前时间 + 30 分钟`

后续可以通过定时任务扫描订单表，将已经超时但仍处于待付款状态的订单自动取消。

>  超时取消的判断条件是：订单状态为 `0 待付款`，并且 `expire_time` 小于当前时间。满足条件后，将订单状态修改为 -1 已取消，当前阶段暂时不实现定时任务代码，只完成字段预留和设计说明。⚠️原因是项目还没有实现支付模块，如果现在直接启用定时取消，测试时容易和手动模拟支付流程冲突。后续如果补充支付模块，可以再通过 `@Scheduled` 定时任务实现自动取消，例如每隔一段时间扫描一次待付款订单。
### 4.5 当前阶段边界

⭐ 当前 Day08 主要完成的是订单主流程，也就是从创建订单到订单状态流转的核心链路。

当前已经完成的内容包括：

- 立即购买创建订单；
- 查询我的订单列表；
- 按订单状态筛选订单；
- 买家取消待付款订单；
- 卖家填写物流单号并发货；
- 买家确认收货；
- 购物车结算生成订单；
- 订单状态从 `0 待付款` 到 `3 已完成` 的核心流转。

当前暂时不处理的内容包括：

- 支付接口；
- 真实支付回调；
- 退款 / 退货；
- 售后仲裁；
- 订单评价；
- 自动确认收货；
- 超时自动取消的具体定时任务实现；
- 商品库存锁定；
- 并发下单控制。

这些内容并不是不重要，而是它们已经超出了当前 Day08 的核心目标。Day08 先保证订单主流程跑通，后续 Day09 再基于已完成订单继续扩展评价与信用体系。

---

## 五、今日成果总结

- [x] ~~完成订单表 `orders` 的设计与创建~~
- [x] ~~完成订单状态常量类 `OrderStatus` 的设计~~
- [x] ~~完成订单模块基础结构搭建~~
  - `Order` 实体类
  - `OrderMapper` 数据访问层
  - `OrderService` 业务接口
  - `OrderServiceImpl` 业务实现类
  - `OrderController` 接口控制层
- [x] ~~完成 `OrderVO` 返回结构设计~~
- [x] ~~完成 `CreateOrderDTO` 创建订单参数设计~~
- [x] ~~实现立即购买创建订单接口 `/api/order/create`~~
- [x] ~~完成下单前商品校验~~
  - 商品是否存在
  - 商品是否上架
  - 是否购买自己发布的商品
- [x] ~~实现我的订单列表接口 `/api/order/list`~~
- [x] ~~实现按订单状态筛选查询~~
- [x] ~~实现买家取消订单接口 `/api/order/cancel/{orderId}`~~
- [x] ~~实现卖家发货接口 `/api/order/ship`~~
- [x] ~~实现买家确认收货接口 `/api/order/confirm/{orderId}`~~
- [x] ~~完成购物车结算接口 `/api/order/checkout`~~
- [x] ~~完成订单状态流转测试~~
  - `0 待付款`
  - `1 待发货`
  - `2 待收货`
  - `3 已完成`
  - `-1 已取消`
- [x] ~~梳理购物车数据与订单数据的关系~~
- [x] ~~梳理订单状态机流转规则~~
- [x] ~~明确当前阶段边界~~
  - 当前先完成订单主流程
  - 支付、退款、自动取消、并发控制等内容后续再扩展

---

## 六、下一步任务(day09)

- [x] 完成评价表 `review` 设计
  - 记录订单ID、商品ID、评价人ID、被评价人ID、评分、评价内容等信息
  - 使用 `orderId + fromUserId` 防止同一用户重复评价同一订单
- [x] 搭建评价模块基础结构
  - 创建 `Review` 实体类
  - 创建 `ReviewMapper`
  - 创建 `ReviewService`
  - 创建 `ReviewServiceImpl`
  - 创建 `ReviewController`
- [x] 设计评价返回结构
  - 创建 `ReviewVO`
  - 返回商品标题、评价人昵称、被评价人昵称、评分文本等展示字段
- [x] 设计发布评价参数
  - 创建 `AddReviewDTO`
  - 前端只传订单ID、评分、评价内容、是否匿名
  - 评价人和被评价人由后端根据 token 和订单数据判断
- [x] 实现发布评价接口
  - 接口路径：`/api/review/add`
  - 订单必须是已完成状态
  - 当前用户必须是订单参与者
  - 同一用户不能重复评价同一订单
  - 评分必须在 `1-5` 分之间
- [x] 实现评价查询接口
  - 查询用户收到的评价：`/api/review/received/{userId}`
  - 查询我发出的评价：`/api/review/sent`
  - 查询结果统一转换为 `ReviewVO`
- [x] 实现信用分更新机制
  - `5` 分：信用分 `+2`
  - `4` 分：信用分 `+1`
  - `3` 分：信用分不变
  - `2` 分：信用分 `-1`
  - `1` 分：信用分 `-2`
  - 发布评价成功后更新被评价人的 `credit_score`
- [x] 梳理信用体系当前阶段边界
  - 当前先完成“订单完成 → 发布评价 → 更新信用分”主链路
  - 复杂信用等级、评价审核、恶意评价识别、申诉与风控后续再扩展

---

## 七、踩坑记录

| 问题                        | 原因                                                        | 解决                                                            |
| ------------------------- | --------------------------------------------------------- | ------------------------------------------------------------- |
| 商品状态为 `1`，但下单提示“商品当前不可购买” | `Product.status` 是 `Byte`，但代码用 `equals(1)` 和 `Integer` 比较 | 改为 `product.getStatus() == \|\| nullproduct.getStatus() != 1` |
| Day06 查询状态正常，Day08 判断状态失败 | Day06 是 SQL 数值比较，Day08 是 Java 对象比较                        | Java 业务判断要注意字段类型，避免 `Byte.equals(Integer)`                    |
| 创建订单时 `getUserId()` 报错    | `Product` 实体类没有 `userId` 字段                               | 改用商品发布者字段 `sellerId`                                          |
| 发货接口提示缺少请求体               | `@RequestBody` 需要 JSON Body，但测试时没传或传错位置                   | Apifox 使用 Body → JSON 传 `orderId` 和 `logisticsNo`             |
| 购物车结算提示不能购买自己的商品          | 购物车里存在自己发布的商品                                             | 结算时保留校验，并排除自己的商品                                              |
| 自己的商品可以加入购物车              | Day07 加入购物车时缺少卖家校验                                        | 在 `addCart` 中判断 `product.getSellerId().equals(userId)`        |
| 批量结算时一条失败，全部回滚            | 方法使用了 `@Transactional`                                    | 保留事务，避免订单和购物车数据不一致                                            |
| 不理解 `cartIds: [5, 6, 7]`  | `[]` 表示 JSON 数组，对应 `List<Long>`                           | 多个写 `[5,6,7]`，单个也要写 `[5]`                                     |
| 混淆 `cartId` 和 `productId` | 购物车结算传的是购物车记录ID，不是商品ID                                    | 先查购物车列表，使用返回的 `cartId` 结算                                     |

---

## 八、我继续说：✧(｡•̀ᴗ-)✧(2026/05/08)

【作者说：时隔两天也算是把day08给完工了(*^▽^*)！但是大部分内容是今天完成的，昨天比较惨(╥﹏╥) ，穿着我的新鞋打球脚居然崴了，(ಥ_ಥ) 非要这么搞吗！day08主要就是对整个订单系统进行完善与验证，我也用其中的几个用户的token跑了一下整个链路，也算是基本完善(ง •̀_•́)ง！！！
    但是这篇笔记后面还有部分内容因为支付系统的不完善还没有开工，估计要等day09之后在开始补充了，当然补充的话也不会在这篇笔记上补充，我做笔记的顺序就是一个开发项目的完整顺序，所以这个顺序是不会乱的(｀・ω・´)！
今天是星期五，我刚好带伤在今天的体育课中体测，还好不是一千米✅ (T_T) ，脚背肿了一块，不是脚踝，所以不是很严重!感觉很快就会好起来！！！明天又会是很累的一天(╯°□°）╯，明天周六跟后天周天我又要去练车，早上又要起个大早骑车到驾校，想想就好痛苦啊 (╥﹏╥)！！！但是早起骑车沐浴清晨的阳光也是真的很爽！(＾▽＾)👍 
   我最近这个项目也快完结了，我也在筹备我的第二个项目，这个项目我肯定会很快完工的，不能再像现在这个项目一样用很长的周期准备了，我做这个项目是用来了解后端体系的，系统性的进行学习，而我还会准备一个关于`AI Agent`的项目，然后后面看看还有没有时间去学习一下`Python`，最后肯定是要留点时间准备简历和八股文的，还得要继续努力啊！`Skyron`(๑• . •๑)！
   现在我仍然是越来越期待用AI生成的前端页面了，这样的话这个项目就算是完成了，然后我也算是有产出项目了！！现在写作动力和学习动力也就这点盼头了(T_T) ，今天怎么说也算是圆满舒服的，我本以为明天是调休补课，内心慌的一批，结果明天正常假期，真实nice啊(๑˃̵ᴗ˂̵)و！
至此，day08到这里就结束了!收工收工，舒服完工！☁️ 🌊 ⛵未完待续，敬请期待day09的内容吧(๑˃̵ᴗ˂̵)و！！！ 】
  