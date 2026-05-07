
>日期：2026/05/04----05/06
> 目标：用户能收藏商品、能加入购物车，为后续订单流程做准备。

---

## 一、商品收藏功能实现

### 1.1 收藏表设计

> 📌收藏表是一个关系表，用来记录“哪个用户收藏了哪个商品”

**在数据库中创建`favorite` 表(使用 DataGrip 执行 SQL 脚本)**：

```mysql
-- ============================================  
-- UnSky Market - Day07 收藏表建表脚本  
-- 数据库：unsky_market  
-- 对应后端实体：favorite  
-- 字符集：utf8mb4（支持 emoji 和特殊字符）  
-- ============================================  
  
CREATE TABLE favorite (  
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '收藏ID',  
    user_id BIGINT NOT NULL COMMENT '用户ID',  
    product_id BIGINT NOT NULL COMMENT '商品ID',  
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '收藏时间',  
  
    UNIQUE KEY uk_user_product (user_id, product_id),  
    KEY idx_user_id (user_id),  
    KEY idx_product_id (product_id)  
) COMMENT = '商品收藏表';
```

> 📌`favorite` 表是 `user` 和 `product` 的中间关系表，`user_id` + `product_id` 唯一约束用于防止重复收藏。

### 1.2 模块基础结构搭建

1. 创建`Favorite`实体类

```Java
@Data  
@TableName("favorite")  
public class Favorite {  
    //收藏id  
    private Long id;  
    //用户id  
    private Long userId;  
    //商品id  
    private Long productId;  
    //收藏时间  
    private LocalDateTime createTime;  
}
```

2. 创建 `FavoriteMapper`数据库访问层

```Java
@Mapper  
public interface FavoriteMapper extends BaseMapper<Favorite> {}
```

3. 创建 `FavoriteService`接口

```Java  
/**  
 * 收藏商品  
 * @param productId  
 * @param userId  
 * @return  
 */  
Result<Void> addFavorite(Long productId, Long userId);
```

4. 创建 `FavoriteServiceImpl`接口实现类

```Java
/**  
 * 添加收藏  
 * @param productId  
 * @param userId  
 * @return  
 */  
@Override  
public Result<Void> addFavorite(Long productId, Long userId) {  
    return null; //此处只先搭建方法结构，具体逻辑在 1.3 中实现。
}
```

> 🚀 上述基础结构已经搭建完毕，后面开始实现各个接口功能

### 1.3 收藏商品接口实现

1. 在`FavoriteServiceImpl`里实现具体方法

```Java
@Override
public Result<Void> addFavorite(Long productId, Long userId) {
    // 1. 判断商品ID是否为空
    if (productId == null) {
        return Result.error("商品ID不能为空");
    }
    // 2. 判断是否已经收藏
    LambdaQueryWrapper<Favorite> wrapper = new LambdaQueryWrapper<>();
    wrapper.eq(Favorite::getUserId, userId)
            .eq(Favorite::getProductId, productId);
    Long count = favoriteMapper.selectCount(wrapper);
    if (count > 0) {
        return Result.error("请勿重复收藏");
    }
    // 3. 新增收藏记录
    Favorite favorite = new Favorite();
    favorite.setUserId(userId);
    favorite.setProductId(productId);
    
    favoriteMapper.insert(favorite);
    return Result.success();
}
```

2. 接着创建 `FavoriteController`接口

```Java
@RestController  
@RequestMapping("/api/favorite")  
@RequiredArgsConstructor  
public class FavoriteController {    
    private final FavoriteService favoriteService;  
    /**  
     * 收藏商品  
     * @param productId 收藏商品id  
     * @param request  从请求头获取token
     * @return  
     */  
    @PostMapping("/add/{productId}")  
    public Result<Void> addFavorite(@PathVariable Long productId,  
                                    HttpServletRequest request) {  
        String token = request.getHeader("token");  
        Long userId = JwtUtil.getUserIdFromToken(token);   
        return favoriteService.addFavorite(productId, userId);  
    }  
}
```

### 1.4 接口测试与验证

- `Apifox`测试截图

![[Pasted image 20260504144305.png]]

- 数据库验证

![[Pasted image 20260504144414.png]]

> 我使用的是用户id=4，"李四光"同学的`token`收藏商品id=5，"小燕考研英语资料全套"的商品

- 重复收藏测试

我继续用这个用户的账号重复收藏，显示返回结果： (ง •̀_•́)ง 📌

```json
{
    "code": 500,
    "msg": "请勿重复收藏",
    "data": null
}
```

### 1.5 取消收藏接口实现

1. 在`FavoriteService`里面新增取消收藏的接口

```Java
/**  
 * 取消收藏  
 * @param productId  
 * @param userId  
 * @return  
 */  
Result<Void> cancelFavorite(Long productId, Long userId);
```

2. 在`FavoriteServiceImpl`里面补充实现方法

```Java
/**  
 * 取消收藏  
 * @param productId  
 * @param userId  
 * @return  
 */  
@Override  
public Result<Void> cancelFavorite(Long productId, Long userId) {  
    // 1. 判断商品ID是否为空  
    if (productId == null) {  
        return Result.error("商品ID不能为空");  
    }    
    // 2. 根据 userId + productId 删除收藏记录  
    LambdaQueryWrapper<Favorite> wrapper = new LambdaQueryWrapper<>();  
    wrapper.eq(Favorite::getUserId, userId)  
            .eq(Favorite::getProductId, productId);   
    int rows = favoriteMapper.delete(wrapper);    
    // 3. 如果没有删除到数据，说明原本没有收藏  
    if (rows == 0) {  
        return Result.error("当前商品未收藏");  
    }  
    return Result.success();  
}
```

3. 在 `FavoriteController`里面补充接口

```Java
/**  
 * 取消收藏  
 * @param productId  
 * @param request  
 * @return  
 */  
@DeleteMapping("/cancel/{productId}")  
public Result<Void> cancelFavorite(@PathVariable Long productId,  
                                   HttpServletRequest request) {  
    String token = request.getHeader("token");  
    Long userId = JwtUtil.getUserIdFromToken(token);  
     
    return favoriteService.cancelFavorite(productId, userId);  
}
```

> 📌取消收藏的本质是删除 `userId + productId` 对应的收藏关系，是在`favorite`数据库表中执行`delete`语句，它是按照当前用户 `userId`和目标商品 `productId`进行删除，因为一个用户可以有多条收藏，所以**需要两个Id来确认唯一要删除收藏的商品**，这就相当于在平面坐标系中需要x和y确认一个点一样

### 1.6 接口测试与验证

- `Apifox`测试截图

![[Pasted image 20260504150624.png]]

- 数据库成功删除对应收藏商品，验证成功

> ⚠️ 只有收藏中的商品才能有被取消收藏的资格，用户只能删除自己的收藏关系

### 1.7 收藏列表接口实现

 **🎯 目标 ：查询当前登录用户收藏过的商品列表**

1. 创建收藏列表`FavoriteVO`

🚀 收藏列表`FavoriteVO`还需要返回`favorite` 表里面没有的内容，包含商品、标题、价格、图片、浏览量、收藏时间等这些前端需要的商品信息

```Java
@Data  
public class FavoriteVO {  
    //收藏记录ID  
    private Long favoriteId;  
    //商品ID  
    private Long productId;  
    //商品标题  
    private String title;  
    //商品价格  
    private BigDecimal price;  
    //商品图片  
    private String images;  
    //商品浏览量  
    private Integer viewCount;  
    //收藏时间  
    private LocalDateTime createTime;  
}
```

2. 在`FavoriteService`里面新增我的收藏列表的接口

```Java
/**  
 * 我的收藏列表  
 * @param userId  
 * @return  
 */  
Result<List<FavoriteVO>> listMyFavorites(Long userId);
```

3. 在`FavoriteServiceImpl`里补充我的收藏列表接口实现类

```Java
/**  
 * 我的收藏列表  
 * @param userId  
 * @return  
 */
@Override
public Result<List<FavoriteVO>> listMyFavorites(Long userId) {
    // 1. 查询当前用户的收藏记录
    LambdaQueryWrapper<Favorite> wrapper = new LambdaQueryWrapper<>();
    wrapper.eq(Favorite::getUserId, userId)
            .orderByDesc(Favorite::getCreateTime);
            
    List<Favorite> favoriteList = favoriteMapper.selectList(wrapper);
    
    // 2. 收藏记录为空，直接返回空列表
    if (favoriteList == null || favoriteList.isEmpty()) {
        return Result.success(new ArrayList<>());
    }
    
    // 3. 遍历收藏记录，查询对应商品信息
    List<FavoriteVO> voList = favoriteList.stream().map(favorite -> {
        Product product = productMapper.selectById(favorite.getProductId());
        
        FavoriteVO vo = new FavoriteVO();
        vo.setFavoriteId(favorite.getId());
        vo.setProductId(favorite.getProductId());
        vo.setCreateTime(favorite.getCreateTime());
        
        if (product != null) {
            vo.setTitle(product.getTitle());
            vo.setPrice(product.getPrice());
            vo.setImages(product.getImages());
            vo.setViewCount(product.getViewCount());
        }
        
        return vo;
    }).collect(Collectors.toList());
    
    return Result.success(voList);
}

```

4. 在`FavoriteController`里新增我的收藏列表接口

```Java
/**  
 * 我的收藏列表  
 * @param request  
 * @return  
 */  
@GetMapping("/list")  
public Result<List<FavoriteVO>> listMyFavorites(HttpServletRequest request) {  
    String token = request.getHeader("token");  
    Long userId = JwtUtil.getUserIdFromToken(token);  
    
    return favoriteService.listMyFavorites(userId);  
}
```

> 当前采用简单遍历查询商品信息，后续可优化为批量查询。

### 1.8 接口测试与验证

- `Apifox`截图

![[Pasted image 20260506083808.png]]

> 我在图示登录`token`账户里新增了一个`productId`=2的商品收藏，在我的收藏列表查看，最终测试成功，我的收藏列表成功返回

---

## 二、购物车功能实现

### 2.1 购物车表设计

> 📌购物车表也是一个关系表，用来记录“哪个用户把哪个商品加入了购物车”

**在数据库中创建`cart` 表(使用 DataGrip 执行 SQL 脚本)**：

```mysql
-- ============================================  
-- UnSky Market - Day07 购物车建表脚本  
-- 数据库：unsky_market  
-- 对应后端实体：cart  
-- 字符集：utf8mb4（支持 emoji 和特殊字符）  
-- ============================================  
  
CREATE TABLE cart (  
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '购物车ID',  
    user_id BIGINT NOT NULL COMMENT '用户ID',  
    product_id BIGINT NOT NULL COMMENT '商品ID',  
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '加入购物车时间',  
  
    UNIQUE KEY uk_user_product (user_id, product_id),  
    KEY idx_user_id (user_id),  
    KEY idx_product_id (product_id)  
) COMMENT = '购物车表';
```

>📌 和收藏表类似：`UNIQUE KEY`是防止同一个用户重复把同一个商品加入购物车

> 📌当前暂时不扩展商品数量，勾选状态和规格，因为当前是二手交易平台，一件商品通常只能被购买一次。

### 2.2 模块基础结构搭建

1. 创建`cart`实体类

```Java
@Data  
@TableName("cart")  
public class Cart {  
    //购物车id  
    private Long id;  
    //用户id  
    private Long userId;  
    //商品id  
    private Long productId;  
    //加入购物车时间  
    private LocalDateTime createTime;  
}
```

2. 创建 `CartMapper`数据库访问层

```Java
@Mapper  
public interface CartMapper extends BaseMapper<Cart> {}
```

3. 创建 `CartService`接口

```Java
/**  
 * 加入购物车  
 * @param productId  
 * @param userId  
 * @return  
 */  
Result<Void> addCart(Long productId, Long userId);
```

4. 创建 `CartServiceImpl`接口实现类

```Java
/**  
 * 加入购物车  
 * @param productId  
 * @param userId  
 * @return  
 */  
@Override  
public Result<Void> addCart(Long productId, Long userId) {  
    return null; //此处只先搭建方法结构，具体逻辑在 1.3 中实现。
}
```

> 🚀 上述基础结构已经搭建完毕，后面开始实现各个接口功能

### 2.3 加入购物车接口实现

1. 在`CartServiceImpl`里实现具体方法

```Java
/**  
 * 加入购物车  
 * @param productId  
 * @param userId  
 * @return  
 */  
@Override  
public Result<Void> addCart(Long productId, Long userId) {  
    // 1. 判断商品ID是否为空  
    if (productId == null) {  
        return Result.error("商品ID不能为空");  
    }  
    
    // 2. 判断是否已经加入购物车  
    LambdaQueryWrapper<Cart> wrapper = new LambdaQueryWrapper<>();  
    wrapper.eq(Cart::getUserId, userId)  
            .eq(Cart::getProductId, productId);  
            
    Long count = cartMapper.selectCount(wrapper);  
    if (count > 0) {  
        return Result.error("请勿重复加入购物车");  
    }  
    
    // 3. 新增购物车记录  
    Cart cart = new Cart();  
    cart.setUserId(userId);  
    cart.setProductId(productId);  
    
    cartMapper.insert(cart);   
    
    return Result.success();  
}
```

4. 在`CartController`里新增加入购物车接口

```Java
/**  
 * 加入购物车  
 * @param productId  
 * @param request  
 * @return  
 */  
@PostMapping("/add/{productId}")  
public Result<Void> addCart(@PathVariable Long productId,  
                            HttpServletRequest request) {  
    String token = request.getHeader("token");  
    Long userId = JwtUtil.getUserIdFromToken(token);  
    
    return cartService.addCart(productId, userId);  
}
```

### 2.4 接口测试与验证

- `Apifox`截图

![[Pasted image 20260506091804.png]]

- 数据库验证

![[Pasted image 20260506091914.png]]

>  我使用的是用户id=2，"小明同学"的`token`收藏商品id=5，"小燕考研英语资料全套"的商品

- 重复收藏测试

我继续用这个用户的账号重复收藏，显示返回结果： (ง •̀_•́)ง 📌

```json
{
    "code": 500,
    "msg": "请勿重复加入购物车",
    "data": null
}
```

### 2.5 删除购物车商品接口实现

1. 在`CartService`里面新增删除购物车商品的接口

```Java
/**  
 * 删除购物车商品  
 * @param productId  
 * @param userId  
 * @return  
 */  
Result<Void> removeCart(Long productId, Long userId);
```

2. 在`CartServiceImpl`里面补充实现方法

```Java
/**  
 * 取消购物车商品  
 * @param productId  
 * @param userId  
 * @return  
 */  
@Override  
public Result<Void> removeCart(Long productId, Long userId) {  
    // 1. 判断商品ID是否为空  
    if (productId == null) {  
        return Result.error("商品ID不能为空");  
    }  
      
    // 2. 根据 userId + productId 删除购物车记录  
    LambdaQueryWrapper<Cart> wrapper = new LambdaQueryWrapper<>();  
    wrapper.eq(Cart::getUserId, userId)  
            .eq(Cart::getProductId, productId);  
            
    int rows = cartMapper.delete(wrapper);  
    
    // 3. 如果没有删除到数据，说明该商品不在购物车中  
    if (rows == 0) {  
        return Result.error("该商品不在购物车中");  
    }  
    
    return Result.success();  
}
```

3. 在 `CartController`里面补充接口

```Java
/**  
 * 删除购物车商品  
 * @param productId  
 * @param request  
 * @return  
 */  
@DeleteMapping("/remove/{productId}")  
public Result<Void> removeCart(@PathVariable Long productId,  
                               HttpServletRequest request) {  
    String token = request.getHeader("token");  
    Long userId = JwtUtil.getUserIdFromToken(token);  
    
    return cartService.removeCart(productId, userId);  
}
```

>  📌 与上面收藏一致，删除购物车的本质是删除`userId + productId`对应的购物车关系不是按照 `productId` 单独删，因为不同用户可能都把同一个商品加入购物车，当前用户只能删除自己的购物车记录

### 2.6 接口测试与验证

- `Apifox`测试截图

![[Pasted image 20260506141431.png]]

- 数据库成功删除对应购物车商品，验证成功 (＾▽＾) 

### 2.7 购物车列表接口实现

**🎯目标：查询当前登录用户购物车中的商品列表**

1. 创建购物车列表`CartVO`

```Java
@Data  
public class CartVO {  
    //购物车记录ID  
    private Long cartId;  
    //商品ID  
    private Long productId;  
    //商品标题  
    private String title;  
    //商品价格  
    private BigDecimal price;  
    //商品图片  
    private String images;  
    //商品浏览量  
    private Integer viewCount;  
    //加入购物车时间  
    private LocalDateTime createTime;  
}
```

2. 在`CartService`里面新增我的购物车列表的接口

```Java
/**  
 * 我的购物车列表  
 * @param userId  
 * @return  
 */  
Result<List<CartVO>> listMyCart(Long userId);
```

3. 在`CartServiceImpl`里补充我的购物车列表接口实现类

```Java
/**  
 * 我的购物车列表  
 * @param userId  
 * @return  
 */  
@Override  
public Result<List<CartVO>> listMyCart(Long userId) {  
    // 1. 查询当前用户购物车记录  
    LambdaQueryWrapper<Cart> wrapper = new LambdaQueryWrapper<>();  
    wrapper.eq(Cart::getUserId, userId)  
            .orderByDesc(Cart::getCreateTime);
               
    List<Cart> cartList = cartMapper.selectList(wrapper);  
    
    // 2. 购物车为空，直接返回空列表  
    if (cartList == null || cartList.isEmpty()) {  
        return Result.success(new ArrayList<>());  
    }  
    
    // 3. 遍历购物车记录，查询对应商品信息  
    List<CartVO> voList = cartList.stream().map(cart -> {  
        Product product = productMapper.selectById(cart.getProductId());  
        
        CartVO vo = new CartVO();  
        vo.setCartId(cart.getId());  
        vo.setProductId(cart.getProductId());  
        vo.setCreateTime(cart.getCreateTime());  
        
        if (product != null) {  
            vo.setTitle(product.getTitle());  
            vo.setPrice(product.getPrice());  
            vo.setImages(product.getImages());  
            vo.setViewCount(product.getViewCount());  
        }  
        
        return vo;  
    }).collect(Collectors.toList());  
    
    return Result.success(voList);  
}
```

 4. 在`CartController`里新增我的收藏列表接口

```Java
/**  
 * 我的购物车列表  
 * @param request  
 * @return  
 */  
@GetMapping("/list")  
public Result<List<CartVO>> listMyCart(HttpServletRequest request) {  
    String token = request.getHeader("token");  
    Long userId = JwtUtil.getUserIdFromToken(token);  
    
    return cartService.listMyCart(userId);  
}
```

> 📌 当前是简单版，后续可以优化成批量查询

### 2.8 接口测试与验证

- Apifox测试截图

![[Pasted image 20260506144244.png]]

> 测试前我用用户id=4，"李四光"同学的`token`将商品id=4，"卖葱机械键盘（青轴）"的商品加入到购物车，然后再调用购物车列表接口，成功显示出对应购物车商品，测试成功 (＾▽＾) 

---

## 三、关键机制设计：幂等性处理

> 🚀 这一章主要总结前面收藏和购物车的共同机制

### 3.1 收藏防重复设计

- 收藏接口做了两层防护
1. Service 层先查是否已经收藏
2. 数据库层通过 user_id + product_id 唯一约束兜底

- 对应代码：

```Java
LambdaQueryWrapper<Favorite> wrapper = new LambdaQueryWrapper<>();
wrapper.eq(Favorite::getUserId, userId)
        .eq(Favorite::getProductId, productId);
        
Long count = favoriteMapper.selectCount(wrapper);
if (count > 0) {
    return Result.error("请勿重复收藏");
}
```

- 数据库约束：

```mysql
UNIQUE KEY uk_user_product (user_id, product_id)
```

**🚀核心：同一个用户只能收藏同一个商品一次。**

### 3.2 购物车防重复设计

- 购物车机制与收藏机制差不多：

```Java
LambdaQueryWrapper<Cart> wrapper = new LambdaQueryWrapper<>();
wrapper.eq(Cart::getUserId, userId)
        .eq(Cart::getProductId, productId);
        
Long count = cartMapper.selectCount(wrapper);
if (count > 0) {
    return Result.error("请勿重复加入购物车");
}
```

- 数据库约束：

```mysql
UNIQUE KEY uk_user_product (user_id, product_id)
```

**🚀核心：同一个用户不能重复把同一个商品加入购物车。**

### 3.3 userId + productId 唯一约束

1. 为什么不是只用 `productId`？
因为：同一个商品可以被多个用户收藏/加入购物车。

2. 为什么不是只用 `userId`
因为：同一个用户可以收藏/加入购物车多个商品。

> 所以必须用两个字段确定一个唯一的关系

> Day07 的幂等性主要解决：用户重复点击按钮导致重复数据插入，这里使用Service 查询判断 + 数据库唯一约束兜底来解决这个问题

---

## 四、交易准备流程梳理（⭐）

### 4.1收藏在交易流程中的作用

- 收藏表示：用户暂时感兴趣，但不一定马上购买

- 它的作用是：方便用户之后再次找到商品

- 流程：浏览商品 → 收藏商品 → 后续从收藏列表再次进入商品详情

---

### 4.2 购物车在交易流程中的作用

- 购物车比收藏更接近交易。

- 表示：用户已经有较强购买意向

- 流程：浏览商品 → 加入购物车 → 查看购物车 → 选择商品 → 进入订单创建

---

### 4.3 和 Day08 订单模块的关系

Day08 要做订单模块。

- 订单来源可能有两个：

```text
1. 商品详情页立即购买
2. 购物车中选择商品后结算
```

> 所以 Day07 的购物车列表会成为 Day08 的前置数据来源。

- 也就是说：

```text
Day07 只是记录用户想买什么
Day08 才真正生成订单
```

---

### 4.4 简单流程图

- 流程图展示：

```text
商品详情
  ↓
收藏商品（弱交易意向）
  ↓
稍后查看

商品详情
  ↓
加入购物车（强交易意向）
  ↓
购物车列表
  ↓
订单创建（Day08）
```

---

### 4.5 当前阶段边界

当前 Day07 不处理：

```text
订单生成
库存锁定
商品状态变更
支付
超时取消
```

这些都放到 Day08 订单模块。

---

## 五、今日成果总结

- [x] 完成收藏表 `favorite` 设计
- [x] 完成收藏模块基础结构搭建
- [x] 实现收藏商品接口
- [x] 实现取消收藏接口
- [x] 实现我的收藏列表接口
- [x] 完成购物车表 `cart` 设计
- [x] 完成购物车模块基础结构搭建
- [x] 实现加入购物车接口
- [x] 实现删除购物车商品接口
- [x] 实现我的购物车列表接口
- [x] 完成 `userId + productId` 防重复设计
- [x] 梳理收藏 / 购物车与订单模块的关系

---

## 六、下一步任务（Day08）

---
## 七、踩坑记录

| 问题/现象 | 原因分析 | 解决方式 |
| --- | --- | --- |
| 收藏和购物车一开始容易想放进 `ProductService` | 收藏、购物车虽然都围绕商品展开，但本质是“用户行为”，不是商品本身的基础能力 | 单独拆出 `FavoriteService`、`CartService`，让商品模块和用户行为模块职责分离 |
| 重复收藏 / 重复加入购物车 | 用户可能连续点击按钮，或者重复请求同一个接口，导致插入相同的 `userId + productId` 记录 | Service 层先查询是否存在，同时数据库使用 `UNIQUE KEY (user_id, product_id)` 兜底 |
| 取消收藏 / 删除购物车时不能只按 `productId` 删除 | 同一个商品可能被多个用户收藏或加入购物车，如果只按 `productId` 删除，会误删其他用户的数据 | 删除时必须同时使用 `userId + productId` 作为条件，只删除当前用户自己的关系记录 |
| 收藏列表 / 购物车列表不能只返回关系表数据 | `favorite` 和 `cart` 表只保存 `userId`、`productId`、`createTime`，前端无法直接展示商品标题、价格、图片等信息 | 新增 `FavoriteVO`、`CartVO`，查询关系表后再根据 `productId` 查询商品信息并组装返回 |
| 收藏列表和购物车列表当前存在 N+1 查询隐患 | 当前实现是遍历每条收藏/购物车记录，再逐个 `selectById` 查询商品，数据量大时查询次数会变多 | 当前阶段先保证功能跑通，后续可优化为收集 `productId` 后使用 `selectBatchIds` 批量查询 |
| 二手交易平台购物车是否需要 `quantity` 字段容易纠结 | 普通电商购物车通常有购买数量，但二手商品一般是一物一件，不适合重复购买多个数量 | 当前阶段不设计 `quantity`，购物车只表示“用户有购买意向”，后续如有批量商品再扩展 |
| `userId + productId` 唯一约束容易只理解成防重复 | 它不仅用于防重复，也表达了收藏/购物车表的核心业务关系：一个用户与一个商品之间只能有一条关系记录 | 在收藏表和购物车表中都统一使用 `UNIQUE KEY uk_user_product (user_id, product_id)` |

> Day07 的主要踩坑点集中在“关系表设计”和“用户行为归属”上：收藏和购物车都不是商品本身，而是当前登录用户与商品之间的关系，因此所有新增、删除、查询都必须围绕 `userId + productId` 展开。
---

## 八、我继续说：(๑•́ ₃ •̀๑)（2026/05/06）

【作者说：day07的内容总体上讲比较少，只涉及到购物车和收藏模块，但是这一篇还是比较重要的(≧▽≦)！它属于是用户行为体系的内容，只要是针对用户的操作来进行的。准确来说这一篇日记我只用了两天时间就完成了，四号上午学习了一半内容，后面跟兄弟们出去玩了很久，五号回才到寝室(￣ー￣)累爆了还打了一会球，所以昨天算是没有什么产出，今天上午数据结构的实验课我也学习了大半部分，今天总算完工！
这个笔记分布有点不均匀，前面主要是具体开发操作，所以用的篇幅就长很多，后面的内容就偏技术总结了，篇幅比较少(｀・ω・´)这一篇涉及的两个模块属于是day08订单体系的前置依赖了，后面的订单体系就主要是业务开发的重点了，估计篇幅也不会太短  (ಥ_ಥ)  ，对于之前的笔记我总感觉有很多地方没啃干净，哎(๑• . •๑)? 慢慢来吧！
    最近也是暂时弃用`Claude code`选择使用`codex`，感觉还不错，后续会继续使用性价比比较高，值得大学生使用(＾▽＾)👍
总感觉没有之前那么有动力去学习这个项目了，大概率是产出跟成就获得感不成正比，少了很多新鲜感和急切心，做了很多看不到暂时的效果确实很苦恼，我可以在五一假期敲代码敲到两点多，也可以在昨天以及今天下午没有什么作为 (╥﹏╥)这样肯定是不行的，必须早点结束这个项目，然后再去学习`Python`准备一个`AI Agent`项目这样简历上的两个项目就准备好了，就可以开始投递了！(｡･ω･｡) 非常非常期待！！！
    所有的规划只为了更好的前进，请继续燃起那颗热烈跳动的心吧！(๑•̀ㅂ•́)و✧
   确实没有很多的想写的内容，今天就到这吧！收工收工，假期完工！☁️ 🌊 ⛵未完待续，敬请期待day08的内容吧(๑˃̵ᴗ˂̵)و！！！ 】
