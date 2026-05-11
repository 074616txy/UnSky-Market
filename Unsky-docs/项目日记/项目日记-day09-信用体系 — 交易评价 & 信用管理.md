

> 日期：2026/05/09
> 目标： 交易完成后能互相评价，信用分动态调整

---

## 一、评价模块基础结构搭建

### 1.1 评价表设计

在`DataGrip`里面执行下面的脚本完成建表：

```mysql
-- ============================================  
-- UnSky Market - Day09 评价表建表脚本  
-- 数据库：unsky_market  
-- 对应后端实体：review  
-- 字符集：utf8mb4（支持 emoji 和特殊字符）  
-- ============================================  
  
CREATE TABLE review (  
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '评价ID',   
    order_id BIGINT NOT NULL COMMENT '订单ID',  
    product_id BIGINT NOT NULL COMMENT '商品ID',  
    from_user_id BIGINT NOT NULL COMMENT '评价人ID',  
    to_user_id BIGINT NOT NULL COMMENT '被评价人ID',  
    score INT NOT NULL COMMENT '评分：1-5分',  
    content VARCHAR(500) DEFAULT NULL COMMENT '评价内容',  
    is_anonymous TINYINT DEFAULT 0 COMMENT '是否匿名：0不匿名，1匿名',  
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',  
    UNIQUE KEY uk_order_from_user (order_id, from_user_id),  
    KEY idx_order_id (order_id),  
    KEY idx_product_id (product_id),  
    KEY idx_from_user_id (from_user_id),  
    KEY idx_to_user_id (to_user_id)  
) COMMENT = '交易评价表';
```

### 1.2 评价状态与评分规则设计

1. 创建评分常量类`ReviewScore`

```Java
/**  
 * 评分常量类  
 */  
public class ReviewScore {    
    // 最低评分  
    public static final Integer MIN_SCORE = 1;  
    // 最高评分  
    public static final Integer MAX_SCORE = 5;  
    // 好评分界线  
    public static final Integer GOOD_SCORE = 5;  
    // 中评分界线  
    public static final Integer NORMAL_SCORE = 3;  
    // 差评分界线  
    public static final Integer BAD_SCORE = 1;  
}
```

> 当前评分用数字，后面用常量表示更清楚

### 1.3 模块基础结构搭建

**🎯 目标：搭好评价体系基础模块，为后面写业务逻辑打下基础**

1. 创建`Review`实体类

```Java
@Data  
@TableName("review")  
public class Review {   
    // 评价ID  
    private Long id;  
    // 订单ID  
    private Long orderId;  
    // 商品ID  
    private Long productId;  
    // 评价人ID  
    private Long fromUserId;  
    // 被评价人ID  
    private Long toUserId; 
    // 评分：1-5分  
    private Integer score;  
    // 评价内容  
    private String content;  
    // 是否匿名：0不匿名，1匿名  
    private Integer isAnonymous;  
    // 创建时间  
    private LocalDateTime createTime;  
}
```

2. 创建`ReviewMapper`数据访问层

```Java
@Mapper  
public interface ReviewMapper extends BaseMapper<Review> {}
```

3. 创建`ReviewService`接口声明

```Java
public interface ReviewService {}
```

4. 创建`ReviewServiceImpl`接口实现类

```Java
@Service  
@RequiredArgsConstructor  
public class ReviewServiceImpl implements ReviewService {}
```

5. 创建`ReviewController`接口控制层

```Java
@RestController  
@RequestMapping("/api/review")  
@RequiredArgsConstructor  
public class ReviewController {    
    private final ReviewService reviewService;  
}
```

> 上述评价体系的结构已经搭建完善，接下来的业务开发逻辑只需要在这里进行补充

### 1.4 ReviewVO 返回结构设计

- 创建返回结构`ReviewVO`

```JAVA 
@Data  
public class ReviewVO {  
    // 评价ID  
    private Long id;  
    // 订单ID  
    private Long orderId;  
    // 商品ID  
    private Long productId;  
    // 商品标题  
    private String productTitle;  
    // 评价人ID  
    private Long fromUserId;  
    // 评价人昵称  
    private String fromUsername;  
    // 被评价人ID  
    private Long toUserId;  
    // 被评价人昵称  
    private String toUsername;  
    // 评分  
    private Integer score;  
    // 评分文本  
    private String scoreText;  
    // 评价内容  
    private String content;  
    // 是否匿名：0不匿名，1匿名  
    private Integer isAnonymous;  
    // 创建时间  
    private LocalDateTime createTime;  
}
```

---

## 二、发布评价接口实现

### 2.1 发布评价 DTO 设计

-  创建 `AddReviewDTO`

```java 
@Data  
public class AddReviewDTO {  
    // 订单ID  
    private Long orderId;  
    // 评分：1-5分  
    private Integer score;  
    // 评价内容  
    private String content;  
    // 是否匿名：0不匿名，1匿名  
    private Integer isAnonymous;  
}
```

> 🚀 前端只把评价内容、评分、是否匿名和订单ID封装到 DTO 提交给后端；当前评价人是谁、评价谁、评价哪个商品，都由后端根据 token 和订单数据判断，最后查询展示时再封装成 VO 返回前端。

### 2.2 订单完成状态校验

1. 在 `ReviewService` 添加发布评价的方法

```Java
/**  
 * 发布评价  
 * @param addReviewDTO  
 * @param currentUserId  
 * @return  
 */  
Result<Void> addReview(AddReviewDTO addReviewDTO, Long currentUserId);
```

2. 在`ReviewServiceImpl`里实现发布评价的基础校验

```Java
/**  
 * 发布评价  
 * @param addReviewDTO  
 * @param currentUserId  
 * @return  
 */  
@Override  
public Result<Void> addReview(AddReviewDTO addReviewDTO, Long currentUserId) {  
    // 1. 判断订单ID是否为空  
    if (addReviewDTO == null || addReviewDTO.getOrderId() == null) {  
        return Result.error("订单ID不能为空");  
    }  
    // 2. 查询订单是否存在  
    Order order = orderMapper.selectById(addReviewDTO.getOrderId());  
    if (order == null) {  
        return Result.error("订单不存在");  
    }  
    // 3. 判断订单是否已完成  
    if (!order.getStatus().equals(OrderStatus.FINISHED)) {  
        return Result.error("只有已完成订单才能评价");  
    }  
    return Result.success();  
}
```

### 2.3 评价双方身份校验

⭐**当前登录用户必须是这笔订单的买家或卖家**，并且自动确定 `toUserId` 是谁

1. 在 `addReview` 里继续添加身份校准

```Java
// 4. 判断当前用户是否是订单参与者，并确定被评价人
Long toUserId;

if (order.getBuyerId().equals(currentUserId)) {
    // 当前用户是买家，被评价人是卖家
    toUserId = order.getSellerId();
} else if (order.getSellerId().equals(currentUserId)) {
    // 当前用户是卖家，被评价人是买家
    toUserId = order.getBuyerId();
} else {
    return Result.error("只能评价自己参与的订单");
}
```

> ⚠️ 这里应该让后端自己判断"**谁评价谁**"，防止用户伪造数据

- 后端应该根据订单自动判断：

```text
当前用户是买家 → 评价卖家
当前用户是卖家 → 评价买家
当前用户不是订单参与者 → 不允许评价
```

### 2.4 防重复评价设计

🎯 目的：**同一个用户对同一个订单只能评价一次**

1. 在 `addReview` 里继续添加防重复设计

```Java
// 5. 判断是否已经评价过
LambdaQueryWrapper<Review> wrapper = new LambdaQueryWrapper<>();
wrapper.eq(Review::getOrderId, order.getId())
        .eq(Review::getFromUserId, currentUserId);
Long count = reviewMapper.selectCount(wrapper);
if (count > 0) {
    return Result.error("请勿重复评价");
}
```

> ⭐ 核心规则是：`orderId` + `fromUserId` 唯一，就是同一个用户，对同一笔订单，只能评价一次，但是允许同一笔订单有两条评价买家评价卖家、卖家评价买家，所以不能限制`orderId` 唯一，否则买家评价完，卖家就不能评价了

### 2.5 发布评价接口实现

1. 在 `addReview` 中继续添加评分校验 

```Java
// 6. 判断评分是否合法
if (addReviewDTO.getScore() == null
        || addReviewDTO.getScore() < ReviewScore.MIN_SCORE
        || addReviewDTO.getScore() > ReviewScore.MAX_SCORE) {
    return Result.error("评分必须在1到5之间");
}
```

2. 在 `addReview` 中创建并保存评价

```Java
// 7. 创建评价对象
Review review = new Review();
review.setOrderId(order.getId());
review.setProductId(order.getProductId());
review.setFromUserId(currentUserId);
review.setToUserId(toUserId);
review.setScore(addReviewDTO.getScore());
review.setContent(addReviewDTO.getContent());
review.setIsAnonymous(addReviewDTO.getIsAnonymous() == null ? 0 : addReviewDTO.getIsAnonymous());

reviewMapper.insert(review);

return Result.success();
```

3. 在 `ReviewController` 添加接口

```java 
/**  
 * 发布评价  
 * @param addReviewDTO  
 * @param request  
 * @return  
 */  
@PostMapping("/add")  
public Result<Void> addReview(@RequestBody AddReviewDTO addReviewDTO,  
                              HttpServletRequest request) {  
    String token = request.getHeader("token");  
    Long currentUserId = JwtUtil.getUserIdFromToken(token);  
    return reviewService.addReview(addReviewDTO, currentUserId);  
}
```

### 2.6 接口测试与验证

- 在Apifox里面进行测试

![[Pasted image 20260509154900.png]]

> 在这里我使用的是`id`=1，"天下云"同学的账号将订单 `id = 2` (也就是商品`id`=5的商品)的商品状态手动修改为 `3 已完成`，然后对它进行发表评价，返回成功

- 数据库验证

> 数据库成功展示了评价信息和相应字段，证明链路完善，验证成功 (*^▽^*)

---

## 三、评价列表查询实现

### 3.1 查询用户收到的评价

1. 在 `ReviewService` 添加查询评价的方法

```Java
/**  
 * 查询用户收到的评价  
 * @param userId  
 * @return  
 */  
Result<List<ReviewVO>> listReceivedReviews(Long userId);
```

2. 在`ReviewServiceImpl`实现查询收到的评价

```Java
/**  
 * 查询用户收到的评价  
 * @param userId  
 * @return  
 */  
@Override  
public Result<List<ReviewVO>> listReceivedReviews(Long userId) {  
    // 1. 判断用户ID是否为空  
    if (userId == null) {  
        return Result.error("用户ID不能为空");  
    }  
    // 2. 查询该用户收到的评价  
    LambdaQueryWrapper<Review> wrapper = new LambdaQueryWrapper<>();  
    wrapper.eq(Review::getToUserId, userId);  
    wrapper.orderByDesc(Review::getCreateTime);  
    
    List<Review> reviewList = reviewMapper.selectList(wrapper);  
    // 3. 转换为VO  
    List<ReviewVO> voList = reviewList.stream()  
            .map(this::convertToVO)  
            .collect(Collectors.toList());  
              
    return Result.success(voList);  
}
```

3. 添加 `convertToVO` 方法

```Java
// 转换评价VO
private ReviewVO convertToVO(Review review) {
    ReviewVO vo = new ReviewVO();
    
    vo.setId(review.getId());
    vo.setOrderId(review.getOrderId());
    vo.setProductId(review.getProductId());
    vo.setFromUserId(review.getFromUserId());
    vo.setToUserId(review.getToUserId());
    vo.setScore(review.getScore());
    vo.setScoreText(getScoreText(review.getScore()));
    vo.setContent(review.getContent());
    vo.setIsAnonymous(review.getIsAnonymous());
    vo.setCreateTime(review.getCreateTime());
    // 查询商品标题
    Product product = productMapper.selectById(review.getProductId());
    if (product != null) {
        vo.setProductTitle(product.getTitle());
    }
    // 查询评价人昵称
    User fromUser = userMapper.selectById(review.getFromUserId());
    if (fromUser != null) {
        if (review.getIsAnonymous() != null && review.getIsAnonymous() == 1) {
            vo.setFromUsername("匿名用户");
        } else {
            vo.setFromUsername(fromUser.getUsername());
        }
    }
    // 查询被评价人昵称
    User toUser = userMapper.selectById(review.getToUserId());
    if (toUser != null) {
        vo.setToUsername(toUser.getUsername());
    }
    return vo;
}
```

4. 添加评分文本转换方法`getScoreText`

```Java
// 转换评分文本
private String getScoreText(Integer score) {
    if (score == null) {
        return "未知评价";
    }
    if (score >= 5) {
        return "好评";
    } else if (score >= 3) {
        return "中评";
    } else {
        return "差评";
    }
}
```

5. 在 `ReviewController` 添加接口

```Java
/**  
 * 查询用户收到的评价  
 * @param userId  
 * @return  
 */  
@GetMapping("/received/{userId}")  
public Result<List<ReviewVO>> listReceivedReviews(@PathVariable Long userId) {  
    return reviewService.listReceivedReviews(userId);  
}
```

6. 在`Apifox`里面进行测试并验证

![[Pasted image 20260509165104.png]]

> 这里依旧是按照上面发布评价的链路进行，显示的是"天下云"同学对订单`id`=2的商品评价

### 3.2 查询我发出的评价

🎯 **当前登录用户查看自己发出去的评价**

1. 在 `ReviewService` 添加查询我发出的评价的方法

```Java
/**  
 * 查询我发出的评价  
 * @param currentUserId  
 * @return  
 */  
Result<List<ReviewVO>> listMySentReviews(Long currentUserId);
```

2. `ReviewServiceImpl` 实现查询我发出的评价

```Java
/**  
 * 查询我发出的评价  
 * @param currentUserId  
 * @return  
 */  
@Override  
public Result<List<ReviewVO>> listMySentReviews(Long currentUserId) {  
    // 1. 查询当前用户发出的评价  
    LambdaQueryWrapper<Review> wrapper = new LambdaQueryWrapper<>();  
    wrapper.eq(Review::getFromUserId, currentUserId);  
    wrapper.orderByDesc(Review::getCreateTime);  
    
    List<Review> reviewList = reviewMapper.selectList(wrapper);  
    // 2. 转换为VO  
    List<ReviewVO> voList = reviewList.stream()  
            .map(this::convertToVO)  
            .collect(Collectors.toList());  
            
    return Result.success(voList);  
}
```

3. 在 `ReviewController` 添加查询我发出的评价的接口

```Java
/**  
 * 查询我发出的评价  
 * @param request  
 * @return  
 */  
@GetMapping("/sent")  
public Result<List<ReviewVO>> listMySentReviews(HttpServletRequest request) {  
    String token = request.getHeader("token");  
    Long currentUserId = JwtUtil.getUserIdFromToken(token);  
    return reviewService.listMySentReviews(currentUserId);  
}
```

4. 在`Apifox`里面进行测试与验证

![[Pasted image 20260509170319.png]]

> 查询“我”发出的评价，“我是谁”不能让前端传 userId，必须从 token 里解析，这里仍然用的是"天下云"同学的`token`进行登录并查询 ( •̀ ω •́ )✌

### 3.3 评价返回结构组装

⚠️ 评价列表查询时，不能直接返回 `Review` 实体类。

📚 `Review` 实体类只对应 `review` 表，里面保存的是：

- 订单ID
- 商品ID
- 评价人ID
- 被评价人ID
- 评分
- 评价内容
- 是否匿名

⚠ 但是前端展示评价列表时，还需要商品标题、评价人昵称、被评价人昵称、评分文本等信息。

🛠 所以这里通过 `convertToVO` 方法，将 `Review` 转换成 `ReviewVO` 返回给前端。

- 转换过程中主要做了几件事：

1. 将评价表中的基础字段复制到 `ReviewVO`；
2. 根据 `productId` 查询商品信息，补充 `productTitle`；
3. 根据 `fromUserId` 查询评价人信息，补充 `fromUsername`；
4. 根据 `toUserId` 查询被评价人信息，补充 `toUsername`；
5. 根据 `score` 转换出 `scoreText`；
6. 如果评价是匿名评价，则前端展示为“匿名用户”。

> 👉 这样前端拿到的就不只是数据库原始记录，而是可以直接用于页面展示的评价数据。

---

## 四、信用分更新机制设计（⭐）

> 由于 `user` 表中已经存在 `credit_score` 字段，且 `User` 实体类中已经有 `creditScore` 属性，因此本阶段不需要再修改用户表结构，直接基于现有字段实现信用分更新逻辑。

### 4.1 评价分数与信用分关系

💡 当前阶段先采用简单的信用分调整规则：

- `5` 分：信用分 `+2`
- `4` 分：信用分 `+1`
- `3` 分：信用分不变
- `2` 分：信用分 `-1`
- `1` 分：信用分 `-2`

> 👉 也就是说，评价分数越高，被评价用户的信用分越高；评价分数越低，被评价用户的信用分会被扣减。

> 当前规则不做复杂权重计算，只根据单次评价分数对被评价人的信用分进行简单加减，先保证信用体系能和评价模块联动起来。

### 4.2 发布评价后更新信用分

🎯 在已经写好的 `addReview` 方法里，**插入评价成功后更新被评价人的信用分**

1. 在 `ReviewServiceImpl` 里添加信用分变化方法`calculateCreditChange`

```Java
// 根据评分计算信用分变化值
private Integer calculateCreditChange(Integer score) {
    if (score == null) {
        return 0;
    }
    switch (score) {
        case 5:
            return 2;
        case 4:
            return 1;
        case 3:
            return 0;
        case 2:
            return -1;
        case 1:
            return -2;
        default:
            return 0;
    }
}
```

2. 在 `addReview` 里插入更新逻辑

```Java
// 8. 更新被评价人的信用分  
User toUser = userMapper.selectById(toUserId);  
if (toUser != null) {  
    Integer currentScore = toUser.getCreditScore() == null ? 100 : toUser.getCreditScore();  
    Integer changeScore = calculateCreditChange(addReviewDTO.getScore());  
    
    toUser.setCreditScore(currentScore + changeScore);  
    userMapper.updateById(toUser);  
}
```

3. 在`Apifox`里进行测试并验证

> ⚠️ 这里要找一个**还没评价过**的**已完成**订单，发布评价，再查看信誉分的变化

![[Pasted image 20260509173527.png]]

> 📌 这里是买家"天下云"同学向卖家"张三丰"同学的商品进行评价，并且在数据验证中，"张三丰"同学的信誉分成功变为102，说明测试与验证都成功了 (≧▽≦)

### 4.3 信用体系当前阶段边界

🎯 当前 Day09 的信用体系先做基础版本，重点是让评价模块和用户信用分产生联动。

⭐ 当前已经完成的内容包括：

- 用户可以基于已完成订单发布评价；
- 只有订单参与者才能评价；
- 同一个用户对同一笔订单只能评价一次；
- 评价会保存到 `review` 表；
- 查询评价时会通过 `ReviewVO` 返回展示数据；
- 发布评价后，会根据评分修改被评价人的 `credit_score`。

🚀 当前信用分调整规则比较简单：

- `5` 分：信用分 `+2`
- `4` 分：信用分 `+1`
- `3` 分：信用分不变
- `2` 分：信用分 `-1`
- `1` 分：信用分 `-2`

⚠️ 当前暂时不处理的内容包括：

- 复杂信用等级体系；
- 评价权重算法；
- 恶意评价识别；
- 评价申诉；
- 评价审核；
- 信用分上下限限制；
- 多次差评后的风控处理。

> 💡 这些内容都属于信用体系的后续扩展。当前阶段先保证“完成订单 → 发布评价 → 更新信用分”这条主链路跑通。

---

## 五、今日成果总结

- [x] 完成评价表 `review` 设计
- [x] 完成评价模块基础结构搭建
  - `Review` 实体类
  - `ReviewMapper` 数据访问层
  - `ReviewService` 业务接口
  - `ReviewServiceImpl` 业务实现类
  - `ReviewController` 接口控制层
- [x] 完成 `ReviewVO` 返回结构设计
- [x] 完成 `AddReviewDTO` 发布评价参数设计
- [x] 实现发布评价接口 `/api/review/add`
- [x] 完成发布评价前的核心校验
  - 订单必须存在
  - 订单必须已完成
  - 当前用户必须是订单参与者
  - 同一用户不能重复评价同一订单
  - 评分必须在 `1-5` 分之间
- [x] 实现查询用户收到的评价接口 `/api/review/received/{userId}`
- [x] 实现查询我发出的评价接口 `/api/review/sent`
- [x] 完成 `Review` 到 `ReviewVO` 的返回结构组装
  - 商品标题
  - 评价人昵称
  - 被评价人昵称
  - 评分文本
  - 匿名用户展示
- [x] 完成评价分数与信用分关系设计
- [x] 实现发布评价后更新被评价人信用分
- [x] 完成信用分更新测试
- [x] 梳理信用体系当前阶段边界

---

## 六、下一步任务(day10)

- [x] 完成管理员表 `admin` 设计
  - 设计管理员账号、密码、角色、创建时间等基础字段
  - 管理员账号体系与普通用户账号体系分离
- [x] 完成管理员模块基础结构搭建
  - 创建 `Admin` 实体类
  - 创建 `AdminMapper`
  - 创建 `AdminService`
  - 创建 `AdminServiceImpl`
  - 创建 `AdminController`
- [x] 完成管理员登录参数与返回结构设计
  - 创建 `AdminLoginDTO`
  - 创建 `AdminVO`
  - 登录成功后只返回管理员基础信息和 Token，不返回密码
- [x] 初始化管理员账号
  - 使用 BCrypt 生成加密密码
  - 在 `admin` 表中插入管理员账号
- [x] 实现管理员登录接口
  - 接口路径：`/api/admin/login`
  - 完成账号参数校验
  - 完成 BCrypt 密码校验
  - 生成管理员 Token
- [x] 实现管理员用户管理功能
  - 查询普通用户列表
  - 补充用户状态字段 `status`
  - 封禁用户
  - 解封用户
- [x] 实现管理员商品管理功能
  - 查询所有商品列表
  - 下架违规商品
  - 管理员可以查看普通用户端不可见的商品状态
- [x] 实现学生认证管理功能
  - 查询所有学生认证申请
  - 审核学生认证申请
  - 补全 Day04 学生认证流程中的管理员审核环节
- [x] 梳理后台管理模块当前阶段边界
  - 当前先完成管理员登录、用户管理、商品管理、认证审核
  - 多级管理员权限、操作日志、数据统计、复杂 RBAC 后续再扩展

---

## 七、踩坑记录

| 问题                   | 原因                                          | 解决                                               |
| -------------------- | ------------------------------------------- | ------------------------------------------------ |
| 发布评价时不能让前端传评价人和被评价人  | 用户身份可以被伪造，不能相信前端传来的 `fromUserId`、`toUserId` | **评价人从 token 解析**，被评价人根据订单买卖双方自动判断               |
| 不是订单参与者也可能尝试评价       | 只靠 `orderId` 不足以判断评价权限                      | 判断当前用户是否等于订单的 `buyerId` 或 `sellerId`             |
| 未完成订单也能被评价的风险        | 如果不校验订单状态，未交易完成也可以评价                        | 只允许 `status = 3 已完成` 的订单发布评价                     |
| 重复评价同一笔订单            | 用户可能重复提交评价请求                                | 使用 `orderId + fromUserId` 做唯一约束，并在 Service 层提前查询 |
| 不能只用 `orderId` 做唯一约束 | 一笔订单可能允许买家和卖家各评价一次                          | 使用 `orderId + fromUserId`，而不是单独限制 `orderId`      |
| `Review` 直接返回前端信息不够  | 表中只有用户ID、商品ID、评分等基础数据                       | 使用 `ReviewVO` 补充商品标题、用户昵称、评分文本等展示信息              |
| 查询用户昵称时报错            | `User` 实体中的字段名和代码里的 `getUsername()` 不一致     | 按照 `User` 实体真实字段修改 getter 方法                     |
| 匿名评价仍可能暴露评价人         | 如果直接返回评价人昵称，匿名字段就失效                         | `isAnonymous = 1` 时统一展示为“匿名用户”                   |
| 评价成功但信用分没变化          | 可能查的是评价人，而不是被评价人                            | 信用分更新对象应该是 `toUserId` 对应的用户                      |
| 评分为 `3` 时信用分不变       | 当前规则中 `3` 分对应变化值为 `0`                       | 使用 `5` 或 `1` 分测试信用分变化更明显                         |

---

## 八、我依旧想说：୧(๑•̀⌄•́๑)૭（2026/05/09）

【作者说：这篇文章我花了一整天的时间给它完成了，今天还是学了挺久的 ᕦ(ò_óˇ)ᕤ 今天是周末，但是我是早上八点多起床的，起了个大早去练车(ಥ_ಥ) ，回来睡了个午睡然后从两点多搞到现在，感觉今天过得很快----今天比较充实(＾▽＾) ✨
    关于这个day09的内容还有一些更深层次的东西我没有写，当然也不打算继续去开展一个绿叶模块去拓展了，因为这个day09所有的主要内容以及重要链路都在包含在笔记里面了，像有一些评价申诉等等之类的小业务模块我也不想花费太多时间，有这些时间我还不如去学习一些新东西！(￣ー￣)  ，所以那些内容我就放弃扩展了，加快一些整体的进度尽快完工这个项目，然后根据这个项目来进行简历的整理以及一些专业问题的问答！我今晚会花点时间开启day10的学习内容，然后大概在明天完成这整个项目的开发以及落地୧(๑•̀⌄•́๑)૭
安排是这样的安排，明天依旧需要早起练车，很痛苦啊(￣o￣) . z Z 今天确实也没啥内容可以写，因为经历的东西不是很新奇，主要就是日常安排罢了，昨晚上包括今天我深度使用了codex，后面的学习也离不开它，所以我有买了点`token`(ಥ_ಥ)，只要能帮助到我，花多少钱都可以ʕ•̀ω•́ʔ✧
  这个脚感觉也好了不少，估计过几天就可以打球了✔🏀，也正是因为这个脚伤，我也才能心无旁骛地在这里疯狂学习和码字，所以说有些是事情还得从两面来看！明天完成这整个项目之后，我来开始真正的用ai来完成一套前端页面的开发了，这大概是我第一次真正使用ai来进行工作，我当然是很期待的！ (｡･ω･｡) 到时候进行一些简单的测试和验证，我就可以开始下一步学习计划了，当然这些辛辛苦苦学的笔记也会是我珍贵的财富，我会好好使用它们的(☆▽☆)✨
   今晚我要开启day10的学习了，关于day09在这里就至此收工，期待下一步的学习！💡
     收工收工，快速完工！☁️ 🌊 ⛵未完待续，敬请期待day10的内容吧(๑˃̵ᴗ˂̵)و！！！ 】


