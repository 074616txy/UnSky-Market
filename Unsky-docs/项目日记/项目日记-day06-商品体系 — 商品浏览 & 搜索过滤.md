
> 日期：2026/04/29----05/01
> 目标：用户能浏览商品列表、查看详情、按条件搜索与筛选过滤

---

## 项目结构

**（🌿更新----2026/05/03）**
```
D:\Develop\UnSky-Market-Project
├── unsky-backend
│   ├── src/main/java/com/Market
│   │   ├── cert/*                 （认证模块：简写）
│   │   ├── user/*                 （用户模块：简写）
│   │   │
│   │   ├── product              （🔥商品模块：本日重点）
│   │   │   ├── controller       （控制层）
│   │   │   │   ├── ProductController
│   │   │   │   └── ProductCategoryController
│   │   │   │
│   │   │   ├── service          （业务层）
│   │   │   │   ├── ProductService
│   │   │   │   ├── ProductCategoryService
│   │   │   │   │
│   │   │   │   └── impl         （实现类）
│   │   │   │       ├── ProductServiceImpl
│   │   │   │       └── ProductCategoryServiceImpl
│   │   │   │
│   │   │   ├── mapper           （数据访问层）
│   │   │   │   ├── ProductMapper
│   │   │   │   └── ProductCategoryMapper
│   │   │   │
│   │   │   ├── dto              （请求参数封装）
│   │   │   │   ├── ProductPublishDTO   （发布商品）
│   │   │   │   └── ProductUpdateDTO    （编辑商品）
│   │   │   │  
│   │   │   ├── vo               （返回数据封装）
│   │   │   │   ├── ProductListVO     （列表展示）
│   │   │   │   └── ProductDetailVO   （详情展示）
│   │   │   └── query
│   │   │         └── ProductQuery        （查询参数）
│   │   │
│   │   └── UnSkyApplication     （启动类）
│   │
│   ├── src/main/resources
│   │   ├── application.yml
│   │   └── application-dev.yml
│   │
│   └── pom.xml
│
├── unsky-common/*（公共模块：工具类 / Result / 实体等）
│           └── entity
│               ├── Product
│               └── ProductCategory
└── ...
```
---
## 一、商品模块前置依赖搭建

### 1.1 在 `MySQL` 中创建 `category` 表并初始化分类数据

- 在商品模块正式开发之前，首先需要补充商品分类表 `category`。  
- 这是因为后续商品发布、商品列表筛选和商品详情展示，都会依赖分类数据作为基础支撑，因此分类体系必须优先于商品体系落地。

```sql
-- =============================================  
-- UnSky Market - Day06 商品分类建表脚本  
-- 数据库：unsky_market  
-- 对应后端实体：ProductCategory
-- 字符集：utf8mb4（支持 emoji 和特殊字符）=============================================  
CREATE TABLE product_category (  
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '分类ID',  
  
    name VARCHAR(50) NOT NULL COMMENT '分类名称',  
  
    sort INT DEFAULT 0 COMMENT '排序字段（值越小越靠前）',  
  
    status TINYINT DEFAULT 1 COMMENT '状态（1=启用，0=禁用）',  
  
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间', 
    
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'  
) COMMENT='商品分类表';
```


```sql
-- 初始化分类数据
INSERT INTO product_category (name, sort, status)
VALUES
('数码', 1, 1),
('教材', 2, 1),
('生活', 3, 1),
('衣物', 4, 1),
('虚拟物品', 5, 1),
('其他', 6, 1);
```

### 1.2 创建 `ProductCategory.java` 实体类

- 下面需要在后端补充与该表对应的实体类 `ProductCategory.java`，将数据库中的分类数据映射为 Java 中可操作的对象，为后续分类列表接口、商品发布和商品分类筛选提供统一的数据载体。

```Java
/**  
 * 商品分类实体类：对应数据库product_category  
 */@Data  
@TableName("product_category")  
public class ProductCategory {  
    //主键 自增 分类ID  
    @TableId(type = IdType.AUTO)  
    private Long id;  
    //分类名称  
    private String name;  
    //排序字段（值越小越靠前）  
    private Integer sort;  
    //状态（1=启用，0=禁用）  
    private Byte status;  
}
```

### 1.3 创建 `ProductCategoryMapper`数据访问层接口

- 在完成 `category` 表和对应实体类 `ProductCategory` 之后，接下来需要补充 `ProductCategoryMapper`，用于后续对商品分类数据进行数据库访问操作。后面的分类列表接口，就是通过这一层去查询已启用的分类数据。

```Java
@Mapper  
public interface ProductCategoryMapper extends BaseMapper<ProductCategory> {  
}
```

### 1.4 创建 `ProductCategoryService`业务接口

- 在完成 `ProductCategoryMapper` 之后，接下来继续补充商品分类模块的业务层接口 `ProductCategoryService`。这一步的作用，是先把商品分类业务的 Service 骨架搭起来，为后续分类列表接口预留业务入口。

```Java
public interface ProductCategoryService {}
```

>  - 当前阶段先搭骨架，后续再补充“查询分类列表”等具体方法， 保持 `Controller → Service → ServiceImpl → Mapper` 的分层结构，避免在 Controller 中直接操作数据库

### 1.5 创建 `ProductCategoryServiceImpl`业务接口实现类

- 在完成 `ProductCategoryService` 接口之后，接下来继续补充 `ProductCategoryServiceImpl` 作为商品分类模块的业务实现层。后续关于分类列表查询等逻辑，都将在这一层中正式实现。

```Java
@Service  
@RequiredArgsConstructor//自动生成“必需参数”的构造方法  
/** 主要用途：  
 * 配合 private final  
 * 省掉手写构造方法  
 * 实现构造器注入  
 */  
public class ProductCategoryServiceImpl implements ProductCategoryService {  
    //“必需参数”----> final 修饰的字段  
    private final ProductCategoryMapper productCategoryMapper;  
}
```

> 这里使用`Lombok`官方的*自动生成“依赖注入用的构造器”的注解@RequiredArgsConstructor*

---
## 二、商品浏览实现

### 2.1 实现分类列表接口 `/api/category/list`

1. 在 `ProductCategoryService` 中声明查询分类列表方法

- 依据分层结构，首先需要在 `ProductCategoryService` 中声明分类列表查询方法，为后续具体实现预留业务入口

```Java
/**  
 * 查询分类列表  
 * 因为返回的是这一分类的所有数据，所以用List<ProductCategory>  
 * 当前阶段分类列表只负责返回所有启用分类，因此方法不需要额外参数
 * @return  
 */  
Result<List<ProductCategory>> listCategory();
```

2. 在 `ProductCategoryServiceImpl` 中实现查询分类列表逻辑并按`sort`排序返回

- 当前阶段需要查询所有启用状态的商品分类，并按照排序字段 `sort` 从小到大返回，为后续前端分类展示提供稳定的数据来源。

```Java
/**  
 * 查询分类列表  
 */  
@Override  
public Result<List<ProductCategory>> listCategory() {  
    LambdaQueryWrapper<ProductCategory> wrapper = new LambdaQueryWrapper<>();  
            wrapper.eq(ProductCategory::getStatus, 1)
            //只返回启用的分类列表  
            .orderByAsc(ProductCategory::getSort);
            //按照sort来进行固定排序  
  
    List<ProductCategory> categoryList = productCategoryMapper.selectList(wrapper);  
    return Result.success(categoryList);  
}
```


3. 在 `Controller` 中声明 `/api/category/list`

- 接下来需要在 `ProductCategoryController` 中继续暴露对应接口，使前端或 Apifox 可以直接获取商品分类列表数据。

```Java
@RestController  
@RequestMapping("/api/category")  
@RequiredArgsConstructor  
public class ProductCategoryController {  
  
    private final ProductCategoryService productCategoryService;  
  
    @GetMapping("/list")  //本质上是查询操作
    public Result<List<ProductCategory>> listCategory() {  
        return productCategoryService.listCategory();  
    }  
}
```


4. Apifox 测试分类列表接口

- 接下来使用 Apifox 对 `/api/category/list` 进行联调测试，验证商品分类数据是否能够正常返回，并确认分类列表是否只包含启用状态的分类且按 `sort` 升序排列。

- 测试结果json展示：
```json
{ "code": 0, "msg": "操作成功", "data": [ { "id": 1, "name": "数码", "sort": 1, "status": 1 }, { "id": 2, "name": "教材", "sort": 2, "status": 1 }, { "id": 3, "name": "生活", "sort": 3, "status": 1 }, { "id": 4, "name": "衣物", "sort": 4, "status": 1 }, { "id": 5, "name": "虚拟物品", "sort": 5, "status": 1 }, { "id": 6, "name": "其他", "sort": 6, "status": 1 } ] }
```

- 测试结果图片展示：（不完整）(●°u°●) 」

![[Pasted image 20260429132132.png]]

> - 成功返回商品分类列表
> - 返回结果仅包含 `status = 1` 的启用分类
> - 分类顺序按 `sort` 字段从小到大排列

---

### 2.2 实现商品列表接口`/api/product/list`

1. 在`MySQL`中创建`product`表并初始化商品数据

```mysql
-- ============================================ 
-- UnSky Market - Day06 商品信息建表脚本  
-- 数据库：unsky_market  
-- 对应后端实体：product  
-- 字符集：utf8mb4（支持 emoji 和特殊字符）  
-- ============================================   
DROP TABLE IF EXISTS product;  
CREATE TABLE product (  
  id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',  

  seller_id BIGINT NOT NULL COMMENT '卖家ID（关联sys_user.id）',  
  
  title VARCHAR(128) NOT NULL COMMENT '商品标题',  
  description TEXT COMMENT '商品描述',  
  
  price DECIMAL(10,2) NOT NULL COMMENT '售价',  
  original_price DECIMAL(10,2) DEFAULT NULL COMMENT '原价',  
  
  category_id BIGINT NOT NULL COMMENT '分类ID（关联product_category.id）',  
  
  condition_level TINYINT DEFAULT 5 COMMENT '新旧程度（1~5）',  
  
  images TEXT COMMENT '商品图片（JSON数组）',  
  
  view_count INT DEFAULT 0 COMMENT '浏览量',  
  favorite_count INT DEFAULT 0 COMMENT '收藏量',  
  
  status TINYINT DEFAULT 1 COMMENT '状态（1-上架 2-下架 3-已售）',  
  
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '发布时间',  
  
  update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  
  PRIMARY KEY (id),  
  
  INDEX idx_category_id (category_id),  
  INDEX idx_seller_id (seller_id),  
  INDEX idx_status (status)  
  
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品表';
```

```mysql
-- ============================================ 
-- UnSky Market - Day06 商品信息初始化数据  
-- 表：product  
-- 说明：用于测试商品信息的模拟数据  
-- ============================================   
INSERT INTO product  
    (seller_id, title, description, price, original_price, category_id, condition_level, images, view_count, favorite_count, status)  
VALUES  
  
(1, '二手 iPhone 18 128G', '成色良好，无拆修，正常使用痕迹', 3200.00, 5999.00, 1, 4, '["img/iphone1.png","img/iphone2.png"]', 120, 15, 1),  
  
(2, '小米笔记本 Pro', '轻薄办公本，性能稳定，适合学生使用', 2800.00, 4999.00, 2, 4, '["img/laptop1.png"]', 85, 10, 1),  
  
(3, '二手山地自行车', '骑行顺畅，适合校园代步', 300.00, 800.00, 3, 3, '["img/bike1.png"]', 60, 5, 1),  
  
(1, '卖葱机械键盘（青轴）', '手感清脆，灯光正常，无损坏', 150.00, 399.00, 4, 4, '["img/keyboard1.png"]', 45, 8, 1),  
  
(4, '小燕考研英语资料全套', '包含历年真题+解析，几乎全新', 80.00, 200.00, 5, 5, '["img/book1.png"]', 30, 6, 1),  
  
(2, '二手kfc显示器 24寸', '1080P高清，办公/游戏均可', 400.00, 899.00, 2, 4, '["img/monitor1.png"]', 70, 9, 1),  
  
(3, '闲置滑板', '适合新手练习，带护具', 120.00, 300.00, 3, 3, '["img/skate1.png"]', 25, 3, 2),  
  
(5, '游戏手柄YBOX', '支持PC/手机，蓝牙连接', 90.00, 199.00, 4, 4, '["img/controller1.png"]', 40, 7, 1);
```

2. 创建`Product.java`实体类

```java 
/**  
 * 商品实体类：对应数据库product  
 */@Data  
@TableName("product")  
public class Product {  
        //商品ID  
        @TableId(type = IdType.AUTO)  
        private Long id;  
        //卖家ID，对应 sys_user.id 
        private Long sellerId;  
        //分类ID，对应 product_category.id 
        private Long categoryId;  
        //商品标题  
        private String title;  
        //商品描述  
        private String description;  
        //商品售价 金额需要精确计算--BigDecimal
        private BigDecimal price;  
        //商品原价  
        private BigDecimal originalPrice;  
        //新旧程度（1=较旧，5=几乎全新）  
        private Byte conditionLevel;  
        //商品图片JSON数组  
        private String images;  
        //浏览量  
        private Integer viewCount;  
        //收藏数量  
        private Integer favoriteCount;  
        //商品状态（0=下架，1=上架，2=已售）  
        private Byte status;  
        //创建时间  
        private LocalDateTime createTime;  
        //更新时间  
        private LocalDateTime updateTime;  
    }
```

3. 在 `ProductMapper` 中创建数据访问层接口

```Java
@Mapper  
public interface ProductMapper extends BaseMapper<Product> {  
}
```

4. 在 `ProductService` 中声明商品列表查询方法

```Java
/**  
 * 商品业务接口  
 */  
public interface ProductService {  
    /**  
     * 查询商品列表  
     * 当前阶段先查询所有上架商品，  
     * 后续再逐步扩展分类筛选、关键词搜索、价格区间、分页等功能。  
     * @return 商品列表  
     */  
     Result<List<Product>> listProduct();  
}
```

5. 在 `ProductServiceImpl` 中实现商品列表查询逻辑

```Java
/**  
 * 商品业务实现类  
 */  
@Service  
@RequiredArgsConstructor  
public class ProductServiceImpl implements ProductService {  
    private final ProductMapper productMapper;  
    /**  
     * 查询商品列表  
     * 当前阶段只查询上架商品，并按照创建时间倒序返回。  
     * @return 商品列表  
     */  
    @Override  
    public Result<List<Product>> listProduct() {  
        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<>();  
  
        wrapper.eq(Product::getStatus, 1)  
                .orderByDesc(Product::getCreateTime);  
  
        List<Product> productList = productMapper.selectList(wrapper);  
  
        return Result.success(productList);  
    }  
}
```

6. 在 `ProductController` 中暴露 `/api/product/list`

```Java
@RestController  
@RequestMapping("/api/product")  
@RequiredArgsConstructor  
public class ProductController {  
  
    private final ProductService productService;  
    //查询商品列表
    @GetMapping("/list")  
    public Result<List<Product>> listProduct()  
    {  
        return productService.listProduct();  
    }  
}
```

7. 使用 Apifox 测试商品列表接口

测试截图：(●°u°●) (不完整)」
![[Pasted image 20260429183335.png]]

> - 返回结果仅包含 `status = 1` 的上架商品
> - 商品按 `create_time` 倒序排列
> - 当前阶段商品列表接口先完成基础链路，不继续展开分页、搜索、筛选等增强能力。  
> - 分页查询、关键词搜索、分类筛选、价格区间筛选等内容后续放到“绿叶篇”中单独补充。

> 后续分页查询、动态排序、Query 参数封装和 VO 返回结构，详见：
> [[项目日记-day06🌿-商品体系 — 商品模块扩展补强#二、商品列表能力扩展]]


### 2.3 实现商品详情接口`/api/product/detail/{id}`

> 说明：商品列表接口用于展示多个商品，而商品详情接口用于根据商品 ID 查看某一个商品的完整信息，后续前端点击商品卡片时，就可以通过商品 ID 请求详情接口，进入商品详情页

1. 在 `ProductService` 中声明根据商品 ID 查询详情的方法

```Java
/**  
 * 根据商品id查询详情信息  
 * @param id 提供具体商品id  
 * @return 商品详情信息  
 */  
Result<Product> getProductDetail(Long id);
```

2. 在 `ProductServiceImpl` 中实现商品详情查询逻辑

```Java
/**  
 * 根据商品id查询具体商品详情信息  
 * @param id 提供具体商品id  
 * @return  
 */  
@Override  
public Result<Product> getProductDetail(Long id) {  
    // 1. 先查询商品（只查上架的）  
    LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<>();  
    wrapper.eq(Product::getId, id)  
            .eq(Product::getStatus, 1);  // 只查上架的商品  
  
    Product product = productMapper.selectOne(wrapper);  
  
    // 2. 判断商品是否存在  
    if (product == null) {  
        return Result.error("商品不存在或已下架");  
    }    
    // 说明：如果返回"已下架"而不是"不存在"，用户会知道这个商品曾经存在但已下架
    // 龙骨篇不做区分，统一返回"商品不存在或已下架"
    return Result.success(product); 
    // ✅ 主流程走完，直接返回 
}
```

3. 在 `ProductController` 中暴露 `/api/product/detail/{id}`

```Java
/**  
 * 根据商品id查询具体商品详情信息   
 * @param id  
 * @return  
 */  
@GetMapping("/detail/{id}")  
public Result<Product> getProductDetail(@PathVariable Long id) {  
    return productService.getProductDetail(id);  
}
```

> `@PathVariable` 的作用是把 URL 里的 `{id}` 提取出来，注入到方法参数里。没有它，Spring 不知道你要取哪个值。

4. 使用 Apifox 测试商品详情接口

![[Pasted image 20260430142123.png]]

> - 已经成功返回商品列表中id=1的商品详细信息，接口测试成功
> - 关于商品不存在或已下架的测试在这里就不展示了

> 商品详情返回结构和浏览量增强，详见：
> [[项目日记-day06🌿-商品体系 — 商品模块扩展补强#3.2 列表VO与详情VO拆分]]
> [[项目日记-day06🌿-商品体系 — 商品模块扩展补强#4.1 浏览量统计实现]]


## 三、商品搜索与筛选

### 3.1 实现按分类筛选

> 在已有商品列表接口基础上，增加**按分类筛选**能力，属于是接口的增强，将原有的`/list`接口替换并升级，实现根据分类id查询某一类商品

1. 在 `ProductService` 中声明按分类筛选的方法

```Java
/**  
     * 按分类筛选商品列表  
     * @param categoryId 分类ID（可选，传null表示查全部）  
     * @return 该分类下的上架商品列表  
     */  
    Result<List<Product>> listProductByCategory(Long categoryId);  
```

2. 在 `ProductServiceImpl` 中实现业务接口逻辑

```java
/**
 * 按分类筛选商品列表
 * @param categoryId 分类ID（可选，传null表示查全部）
 * @return 该分类下的上架商品列表
 */
@Override
public Result<List<Product>> listProductByCategory(Long categoryId) {
    LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<>();
    
    // 1. 只查上架商品
    wrapper.eq(Product::getStatus, 1);
    
    // 2. 如果传了categoryId，就加分类筛选条件
    if (categoryId != null) {
        wrapper.eq(Product::getCategoryId, categoryId);
    }
    
    // 3. 按创建时间倒序，如果创建时间相同，那就按照id倒序
    wrapper.orderByDesc(Product::getCreateTime);
    wrapper.orderByDesc(Product::getId);//修正内容
    
    List<Product> productList = productMapper.selectList(wrapper);
    return Result.success(productList);
}
```

>⚠️ 注意：当排序字段存在重复值时，需要补充唯一字段（如ID）进行二次排序，否则分页结果可能出现顺序不稳定的问题（详见绿叶篇分页查询----已修正）。

3. 在 `ProductController` 中暴露筛选列表接口

```Java
/**  
 * 按分类筛选商品列表  
 * @param categoryId 分类ID（可选，不传则返回全部）  
 * @return 商品列表  
 */  
@GetMapping("/list")  
public Result<List<Product>> listProduct(  
        @RequestParam(required = false) Long categoryId) {  
    return productService.listProductByCategory(categoryId);  
}
```

>`@RequestParam`用于**接收前端请求中的参数（通常是 URL 参数或表单参数）**,这里是指上传分类列表的id，并且`required`默认是`true`
   注意：这里把原来旧的 `/list` 接口**替换**掉了，因为新逻辑完全兼容旧逻辑，不需要两个接口。
 
4. 使用 Apifox 测试按分类筛选商品接口

![[Pasted image 20260430153527.png]]

>注意：其他测试案例在这里就不补充了，这里只展示成功的测试案例截图
> 按分类筛选功能正常，同一接口兼容"查全部"和"按分类查"两种行为

> ⚠️ 说明：此接口在 3.2 中与关键词搜索合并，原有功能由 `searchProducts(categoryId, keyword)` 统一实现，此处保留仅作理解过渡。

### 3.2 实现按关键词搜索

> 在商品列表接口上增加**关键词搜索**能力，没有关键词返回所有上架商品，有关键词就在标题或描述中模糊匹配关键词进行搜索

1.  在 `ProductService` 中声明按关键词搜索的方法

```Java
     /**  
     * 按分类筛选商品列表 and 按关键词搜索商品列表
     * @param categoryId 分类ID（可选，传null表示不限制）  
     * @param keyword 搜索关键词（可选，传null表示查全部）  
     * @return 匹配的上架商品列表  
     */  
    Result<List<Product>> searchProducts(Long categoryId,String keyword);  
```

2. 在 `ProductServiceImpl` 中实现关键词搜索逻辑

```Java
/**  
 * 按分类筛选商品列表 and 按关键词搜索商品列表
 * @param categoryId 分类ID（可选，传null表示不限制）  
 * @param keyword 搜索关键词（可选，传null表示查全部）  
 * @return 匹配的上架商品列表  
 */  
@Override  
public Result<List<Product>> searchProducts(Long categoryId,String keyword) {  
    LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<>();  
  
    // 1. 只查上架商品  
    wrapper.eq(Product::getStatus, 1);  
  
    // 2. 如果传了categoryId，就加分类筛选条件  
    if (categoryId != null) {  
        wrapper.eq(Product::getCategoryId, categoryId);  
    }  
  
    // 3. 如果传了keyword，就做模糊匹配  
    if (keyword != null && !keyword.trim().isEmpty()) {  
        wrapper.and(w -> w  
                .like(Product::getTitle, keyword)  
                .or()  
                .like(Product::getDescription, keyword)  
        );
        }  
    // 4. 按创建时间倒序  
    wrapper.orderByDesc(Product::getCreateTime);  
  
    List<Product> productList = productMapper.selectList(wrapper);  
    return Result.success(productList);  
}
```

> keyword.trim().isEmpty() 多加一个判断：用户可能传了空格，空格不应该被当作有效关键词 
> `wrapper.and(w -> w.like(...).or().like(...))` 的作用是：在**上架商品**这个大前提下，匹配标题 OR 匹配描述 
   使用 like 而不是 eq：like 是模糊匹配，eq 是精确匹配，搜索场景必须用 like 

3. 在 `ProductController` 中暴露关键词搜索功能接口

```Java
/**  
 * 商品列表（支持分类筛选 + 关键词搜索）  
 * @param categoryId 分类ID（可选）  
 * @param keyword 搜索关键词（可选）  
 * @return 商品列表  
 */  
@GetMapping("/list")  
public Result<List<Product>> listProduct(  
        @RequestParam(required = false) Long categoryId,  
        @RequestParam(required = false) String keyword) {  
    return productService.searchProducts(categoryId, keyword);  
}
```

4. 使用 Apifox 测试关键词搜索商品接口

- 用 Apifox 测试合并后的接口`/api/product/list?keyword=xxx`，验证搜索逻辑是否生效

![[Pasted image 20260430190657.png]]

> 测试结果说明：  
> 本次使用 `GET /api/product/list` 接口，并在 Query 参数中传入 `keyword=手机`。接口返回状态码为 `200`，响应结果中 `code = 0`，说明请求链路正常执行。  
> 返回数据中匹配到商品 `游戏手柄YBOX`，其 `description` 字段包含“支持PC/手机，蓝牙连接”，说明关键词搜索不仅可以匹配商品标题，也可以正常匹配商品描述内容。  
> 因此，当前关键词搜索逻辑已经生效，`title LIKE keyword OR description LIKE keyword` 的模糊查询条件验证通过。

> 其他测试情况说明：  
> 除了 `keyword=手机` 的成功案例之外，还可以继续测试“无关键词返回全部上架商品”“关键词不存在返回空列表”“分类 + 关键词组合查询”等场景。这里不再逐一放截图，只保留当前成功案例作为关键词搜索功能已跑通的验证记录。

> keyword 搜索的补充说明，详见：
> [[项目日记-day06🌿-商品体系 — 商品模块扩展补强#5.1 搜索匹配逻辑说明]]

### 3.3 实现按价格区间筛选

> 在分类筛选和关键词搜索都已经合并到 `/api/product/list` 接口之后，继续给商品列表增加**价格区间筛选**能力。  
> 价格筛选的核心逻辑是：前端可以选择性传入最低价 `minPrice` 和最高价 `maxPrice`，后端根据参数动态拼接查询条件。

1. 调整 `ProductService` 中的商品搜索方法参数

- 原来的 `searchProducts(categoryId, keyword)` 已经可以同时处理分类和关键词。现在继续在这个方法上扩展价格区间参数，不需要重新新增接口。
- 因为价格字段在数据库中是 `DECIMAL(10,2)`，Java 实体类中对应的是 `BigDecimal`，所以这里的 `minPrice` 和 `maxPrice` 也使用 `BigDecimal` 接收。

```Java
/**
 * 商品列表查询（支持分类筛选 + 关键词搜索 + 价格区间筛选）
 * @param categoryId 分类ID（可选，传null表示不限制）
 * @param keyword 搜索关键词（可选，传null表示查全部）
 * @param minPrice 最低价格（可选，传null表示不限制最低价）
 * @param maxPrice 最高价格（可选，传null表示不限制最高价）
 * @return 匹配的上架商品列表
 */
Result<List<Product>> searchProducts(Long categoryId, String keyword, BigDecimal minPrice, BigDecimal maxPrice);
```

2. 在 `ProductServiceImpl` 中补充价格区间筛选逻辑

- `minPrice != null` 时，表示需要筛选出价格大于等于最低价的商品，对应 `ge(Product::getPrice, minPrice)`。
- `maxPrice != null` 时，表示需要筛选出价格小于等于最高价的商品，对应 `le(Product::getPrice, maxPrice)`。
- 如果两个参数都不传，就不增加价格条件，仍然保持原来的商品列表查询效果。

```Java
/**
 * 商品列表查询（支持分类筛选 + 关键词搜索 + 价格区间筛选）
 * @param categoryId 分类ID（可选，传null表示不限制）
 * @param keyword 搜索关键词（可选，传null表示查全部）
 * @param minPrice 最低价格（可选，传null表示不限制最低价）
 * @param maxPrice 最高价格（可选，传null表示不限制最高价）
 * @return 匹配的上架商品列表
 */
@Override
public Result<List<Product>> searchProducts(Long categoryId, String keyword, BigDecimal minPrice, BigDecimal maxPrice) {
    LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<>();

    // 1. 只查上架商品
    wrapper.eq(Product::getStatus, 1);

    // 2. 如果传了categoryId，就加分类筛选条件
    if (categoryId != null) {
        wrapper.eq(Product::getCategoryId, categoryId);
    }

    // 3. 如果传了keyword，就做标题/描述模糊匹配
    if (keyword != null && !keyword.trim().isEmpty()) {
        wrapper.and(w -> w
                .like(Product::getTitle, keyword)
                .or()
                .like(Product::getDescription, keyword)
        );
    }

    // 4. 如果传了最低价格，就筛选 price >= minPrice
    if (minPrice != null) {
        wrapper.ge(Product::getPrice, minPrice);
    }

    // 5. 如果传了最高价格，就筛选 price <= maxPrice
    if (maxPrice != null) {
        wrapper.le(Product::getPrice, maxPrice);
    }

    // 6. 按创建时间倒序
    wrapper.orderByDesc(Product::getCreateTime);

    List<Product> productList = productMapper.selectList(wrapper);
    return Result.success(productList);
}
```

> `ge` 是 greater than or equal 的缩写，表示大于等于。  
> `le` 是 less than or equal 的缩写，表示小于等于。  
> 所以 `wrapper.ge(Product::getPrice, minPrice)` 对应 SQL 中的 `price >= minPrice`，`wrapper.le(Product::getPrice, maxPrice)` 对应 SQL 中的 `price <= maxPrice`。

3. 在 `ProductController` 中接收价格筛选参数

- Controller 层继续复用原来的 `/api/product/list` 接口，只是在参数列表中增加 `minPrice` 和 `maxPrice`。
- 这样前端可以通过同一个接口组合出多种查询方式，而不是为每一种筛选条件都新增一个接口。

```Java
/**
 * 商品列表（支持分类筛选 + 关键词搜索 + 价格区间筛选）
 * @param categoryId 分类ID（可选）
 * @param keyword 搜索关键词（可选）
 * @param minPrice 最低价格（可选）
 * @param maxPrice 最高价格（可选）
 * @return 商品列表
 */
@GetMapping("/list")
public Result<List<Product>> listProduct(
        @RequestParam(required = false) Long categoryId,
        @RequestParam(required = false) String keyword,
        @RequestParam(required = false) BigDecimal minPrice,
        @RequestParam(required = false) BigDecimal maxPrice) {
    return productService.searchProducts(categoryId, keyword, minPrice, maxPrice);
}
```

4. 使用 Apifox 测试价格区间筛选商品接口

- 用 Apifox 测试合并后的接口，例如：

![[Pasted image 20260430193514.png]]

> 阶段小结：  
> 到这里，`/api/product/list` 已经从最初的“查询全部上架商品”，逐步扩展成了一个支持**分类筛选 + 关键词搜索 + 价格区间筛选**的综合商品列表接口。  
> 这也是商品浏览模块中最核心的查询入口，后续如果要继续扩展“按新旧程度筛选”“按浏览量排序”“按价格升降序排序”，也可以继续沿用这一套动态条件拼接思路。

---
## 四、商品管理实现

### 4.1 实现发布商品接口

- 卖家发布自己的商品(包含商品标题，价格和描述)

1. 在 `ProductService` 中声明发布商品的方法

```java
/**
 * 发布商品
 * @param product 商品信息（包含标题、价格、分类等）
 * @param userId 卖家ID（从Token中解析出来）
 * @return 发布结果
 */
Result<Void> publishProduct(Product product, Long userId);
```

2.  在 `ProductServiceImpl` 中实现发布商品的逻辑

```java
/**
 * 发布商品
 * @param product 商品信息
 * @param userId 卖家ID
 * @return 发布结果
 */
@Override
public Result<Void> publishProduct(Product product, Long userId) {
    // 1. 基础校验
    if (product.getTitle() == null || product.getTitle().trim().isEmpty()) {
        return Result.error("商品标题不能为空");
    }
    if (product.getPrice() == null) {
        return Result.error("商品价格不能为空");
    }
    if (product.getCategoryId() == null) {
        return Result.error("请选择商品分类");
    }
    
    // 2. 设置默认值
    product.setSellerId(userId);           // 卖家ID = 当前登录用户
    product.setStatus((byte) 1);           // 上架状态（1=上架）
    product.setViewCount(0);               // 浏览量初始为0
    product.setFavoriteCount(0);            // 收藏量初始为0
    
    // 3. 写入数据库
    productMapper.insert(product);
    
    return Result.success();
}
```

3. 在 `ProductController` 中暴露发布商品的接口

```java
/**
 * 发布商品
 * @param product 商品信息（JSON格式）---->所以要用@RequestBody而不是@RequestParam
 * @param request 项目用的是 Header Token 认证方式，所以用于从请求头获取Token
 * @return 发布结果
 */
@PostMapping("/publish")
public Result<Void> publishProduct(@RequestBody Product product, HttpServletRequest request) {
    // 从请求头中获取Token并解析出userId
    String token = request.getHeader("token");
    Long userId = JwtUtil.getUserIdFromToken(token);
    
    return productService.publishProduct(product, userId);
}
```

4. 在`Apifox`中测试并验证发布商品接口

![[Pasted image 20260430224343.png]]

> 从登录界面拿到token，在header参数里面填写，就可以完成数据的测试与验证
> 测试后，检查数据库发现数据库成功插入测试数据，说明验证成功

> 发布商品 DTO、参数校验和防重复提交，详见：
> [[项目日记-day06🌿-商品体系 — 商品模块扩展补强#6.1 发布商品DTO与参数校验]]
> [[项目日记-day06🌿-商品体系 — 商品模块扩展补强#6.2 防重复提交]]

### 4.2 实现编辑商品接口

- 卖家能修改自己发布的商品信息（标题、价格、描述等）。
- **关键约束**：只能编辑**自己发布**的商品，不能编辑别人的。

1. 在 ProductService 中声明编辑商品的方法

```java
/**
 * 编辑商品
 * @param product 商品信息（包含要修改的字段）
 * @param userId 当前登录用户ID（用于校验是否是卖家）
 * @return 编辑结果
 */
Result<Void> updateProduct(Product product, Long userId);
```

> 前端在编辑页面回显商品详情时，已经拿到了商品ID。提交时把ID放在商品对象里一起传过来，`Service` 层根据ID找到商品并更新。
> 在`userId`里进行校验，只有 `sellerId == userId` 的卖家才能编辑商品，防止用户修改别人的商品。

2. 在 `ProductServiceImpl` 中实现编辑商品信息的逻辑

```java
/**
 * 编辑商品
 * @param product 商品信息（包含商品ID和要修改的字段）
 * @param userId 当前登录用户ID
 * @return 编辑结果
 */
@Override
public Result<Void> updateProduct(Product product, Long userId) {
    // 1. 根据ID查询商品
    Product existProduct = productMapper.selectById(product.getId());
    
    // 2. 判断商品是否存在
    if (existProduct == null) {
        return Result.error("商品不存在");
    }
    
    // 3. 校验权限：只有卖家本人才能编辑
    if (!existProduct.getSellerId().equals(userId)) {
        return Result.error("无权操作他人的商品");
    }
    
    // 4. 基础校验
    if (product.getTitle() != null && product.getTitle().trim().isEmpty()) {
        return Result.error("商品标题不能为空");
    }
    if (product.getPrice() != null && product.getPrice().compareTo(java.math.BigDecimal.ZERO) < 0) {
        return Result.error("商品价格不能为负数");
    }
    
    // 5. 更新字段（只更新传了值的字段）
    if (product.getTitle() != null) {
        existProduct.setTitle(product.getTitle());
    }
    if (product.getDescription() != null) {
        existProduct.setDescription(product.getDescription());
    }
    if (product.getPrice() != null) {
        existProduct.setPrice(product.getPrice());
    }
    if (product.getCategoryId() != null) {
        existProduct.setCategoryId(product.getCategoryId());
    }
    if (product.getConditionLevel() != null) {
        existProduct.setConditionLevel(product.getConditionLevel());
    }
    
    // 6. 写入数据库
    productMapper.updateById(existProduct);
    
    return Result.success();
}
```

>先查询再更新是为了保证商品存在，以及获取 `sellerId` 做权限校验
>先查出原商品，再只更新有值的字段，避免null覆盖原数据

3. 在 `ProductController` 中暴露编辑商品的接口

```Java
/**  
 * 编辑商品  
 * @param product 商品信息（JSON格式，包含商品ID和要修改的字段）  
 * @param request 用于从请求头获取Token  
 * @return 编辑结果  
 */  
@PutMapping("/update")//----> PUT = 修改  
public Result<Void> updateProduct(@RequestBody Product product, HttpServletRequest request) {  
    String token = request.getHeader("token");  
    Long userId = JwtUtil.getUserIdFromToken(token);  
  
    return productService.updateProduct(product, userId);  
}
```

> 后续绿叶篇章会将发布和编辑接口的token解析逻辑优化为一个公用方法

4.  在`Apifox`中测试并验证编辑商品接口

![[Pasted image 20260501005429.png]]

> ⚠️ **PUT 请求需要指定要修改的商品 ID**，必须告诉后端改哪一条数据，所以 里面必须包含 `id` 
> ⚠️ **修改时不传的字段会被保留**，`ServiceImpl` 里用了 set 方法只更新有值的字段。
> 在编辑商品的功能下，token获取的用户id必须与商品发布的用户id(卖家id)有一致才能进行修改操作，不然会显示报错信息----"无权操作他人的商品"
> 操作成功后，数据库的相应字段就修改成功，在这里就不放截图了┗( ▔, ▔ )┛

> 编辑商品 DTO 和选择性字段更新，详见：
> [[项目日记-day06🌿-商品体系 — 商品模块扩展补强#6.3 商品编辑能力封装（DTO）]]

### 4.3 实现删除商品接口

- 卖家能删除自己发布的商品。
- **关键约束**：只能删除**自己发布**的商品，不能删除别人的。

1. 在 `ProductService` 中声明删除商品接口的方法

```java
/**
 * 删除商品
 * @param id 要删除商品的ID--删什么
 * @param userId 当前登录用户ID（用于校验是否是卖家）--谁在删
 * @return 删除结果
 */
Result<Void> deleteProduct(Long id, Long userId);
```

2. 在 `ProductServiceImpl` 中实现删除商品的逻辑

```java
/**
 * 删除商品
 * @param id 商品ID
 * @param userId 当前登录用户ID
 * @return 删除结果
 */
@Override
public Result<Void> deleteProduct(Long id, Long userId) {
    // 1. 根据ID查询商品
    Product existProduct = productMapper.selectById(id);
    
    // 2. 判断商品是否存在
    if (existProduct == null) {
        return Result.error("商品不存在");
    }
    
    // 3. 校验权限：只有卖家本人才能删除 sellerId是Long，userId必须是Long
    if (!existProduct.getSellerId().equals(userId)) {
        return Result.error("无权操作他人的商品");
    }
    
    // 4. 执行删除
    productMapper.deleteById(id);
    
    return Result.success();
}
```

3. 在 `ProductController` 中暴露删除商品的接口

```Java
/**  
 * 删除商品  
 * @param id 要删除的商品ID  
 * @param request 用于从请求头获取Token  
 * @return 删除结果  
 */  
@DeleteMapping("/delete/{id}")  
public Result<Void> deleteProduct(@PathVariable Long id, HttpServletRequest request) {  
    String token = request.getHeader("token");  
    Long userId = JwtUtil.getUserIdFromToken(token);  
  
    return productService.deleteProduct(id, userId);  
}
```

> 删除是针对特定资源的操作，商品ID是 URL 路径的一部分（`/delete/{id}`），用 `@PathVariable` 更符合 RESTful 风格。

4. 在`Apifox`中测试并验证删除商品的接口

![[Pasted image 20260501005353.png]]

> ⚠️ **DELETE 请求参数位置**，商品ID放在 URL 路径里（`/delete/{id}`），不是放在 Body 里
> ⚠️ **测试删除前建议先查一下**，这个操作是永久的，测试前可以用商品详情接口确认一下商品确实存在且属于自己，再发 DELETE 请求
> 执行这个操作同样要保证约束，卖家商品只能由卖家来删除

### 4.4 实现我的商品接口

- 卖家查看自己发布的所有商品列表（包含上架、下架、已售各种状态的商品）。

1. 在 `ProductService` 中声明我的商品的方法

```java
/**
 * 查询我发布的商品列表
 * @param userId 当前登录用户ID
 * @return 我发布的商品列表
 */
Result<List<Product>> getMyProducts(Long userId);
```

2. 在 `ProductServiceImpl` 中实现我的商品的逻辑

```java
/**
 * 查询我发布的商品列表
 * @param userId 当前登录用户ID
 * @return 我发布的商品列表
 */
@Override
public Result<List<Product>> getMyProducts(Long userId) {
    LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<>();
    
    // 根据sellerId查询当前用户发布的商品
    wrapper.eq(Product::getSellerId, userId)
           .orderByDesc(Product::getCreateTime);  // 按发布时间倒序
    
    List<Product> productList = productMapper.selectList(wrapper);
    
    return Result.success(productList);
}
```

3. 在 `ProductController` 中暴露我的商品的接口

```java
/**
 * 查询我发布的商品列表
 * @param request 用于从请求头获取Token
 * @return 我发布的商品列表
 */
@GetMapping("/my")
public Result<List<Product>> getMyProducts(HttpServletRequest request) {
    String token = request.getHeader("token");
    Long userId = JwtUtil.getUserIdFromToken(token);
    
    return productService.getMyProducts(userId);
}
```

4. 在`Apifox`中测试并验证我的商品接口

![[Pasted image 20260501010758.png]]

> 返回的是该 Token 对应用户发布的商品列表，包含所有状态（上架、下架、已售）。

---
## 五、今日成果总结

🐉 龙骨（Core）

- 完成商品体系所有接口基础功能&商品基础链路

- [x] 完成商品分类模块基础搭建（建表 → 实体类 → Mapper → Service → Controller → 接口测试）
- [x] 完成商品列表接口 `/api/product/list`（上架商品按创建时间倒序返回）
- [x] 完成商品详情接口 `/api/product/detail/{id}`（只查上架商品，支持商品不存在校验）
- [x] 完成分类筛选功能（`/api/product/list?categoryId=1`）
- [x] 完成关键词搜索功能（`/api/product/list?keyword=xxx`，支持标题/描述模糊匹配）
- [x] 完成价格区间筛选功能（`/api/product/list?minPrice=xxx&maxPrice=xxx`）
- [x] 完成商品发布接口 `/api/product/publish`（需带 Token，支持基础字段校验）
- [x] 完成商品编辑接口 `/api/product/update`（只能编辑自己发布的商品，权限校验）
- [x] 完成商品删除接口 `/api/product/delete/{id}`（只能删除自己发布的商品，权限校验）
- [x] 完成我的商品接口 `/api/product/my`（返回当前卖家发布的全部商品）
- [x] 验证所有接口链路在 Apifox 中测试通过

🌿 绿叶（Enhancement）

- 完善商品体系各功能扩展，详见 [[项目日记-day06🌿-商品体系 — 商品模块扩展补强]]

**查询扩展**
- [x] 分页查询（MyBatis-Plus 分页插件 + selectPage）
- [x] 动态排序（sortBy 支持价格/浏览量/发布时间）
- [x] 查询参数封装（ProductQuery 对象统一接收）
- [x] 新旧程度筛选（conditionLevel 字段）

**返回结构扩展**
- [x] ProductListVO（列表展示用，过滤冗余字段）
- [x] ProductDetailVO（详情展示用，包含完整字段）
- [x] images 字段 JSON → List 转换

**业务增强**
- [x] 浏览量统计（详情接口访问时 view_count +1）
- [x] 发布商品 DTO（ProductPublishDTO + 校验注解）
- [x] 编辑商品 DTO（ProductUpdateDTO + 选择性字段更新）
- [x] 防重复提交（1 分钟内不能重复发布相同标题+价格）

**性能优化**
- [x] Redis 浏览量基础缓存（product:viewCount:{id} key）
- [x] 分类列表缓存（product:category:list key）
- [x] 热门商品接口 /api/product/hot（按浏览量倒序 Top N）
- [x] Redis Key 命名规范（业务模块:对象:含义）

---
## 六、下一步任务(day07)

## 六、下一步任务(day07)

- [x] ~~完成收藏模块基础设计~~
  - ~~在 MySQL 中新建 `favorite` 表~~
  - ~~创建 `Favorite` 实体类、Mapper、Service、Controller~~
  - ~~明确收藏表本质是 `userId + productId` 的用户行为关系表~~

- [x] ~~实现收藏商品接口~~
  - ~~接口路径：`/api/favorite/add/{productId}`~~
  - ~~根据当前登录用户 `userId` 收藏指定商品~~
  - ~~处理重复收藏问题~~

- [x] ~~实现取消收藏接口~~
  - ~~接口路径：`/api/favorite/cancel/{productId}`~~
  - ~~根据 `userId + productId` 删除当前用户自己的收藏记录~~

- [x] ~~实现我的收藏列表接口~~
  - ~~接口路径：`/api/favorite/list`~~
  - ~~返回用户收藏过的商品信息，而不是只返回关系表数据~~

- [x] ~~完成购物车模块基础设计~~
  - ~~在 MySQL 中新建 `cart` 表~~
  - ~~创建 `Cart` 实体类、Mapper、Service、Controller~~
  - ~~明确二手交易平台购物车暂不设计商品数量字段~~

- [x] ~~实现加入购物车接口~~
  - ~~接口路径：`/api/cart/add/{productId}`~~
  - ~~处理重复加入购物车问题~~

- [x] ~~实现删除购物车商品接口~~
  - ~~接口路径：`/api/cart/remove/{productId}`~~
  - ~~根据 `userId + productId` 删除当前用户自己的购物车记录~~

- [x] ~~实现我的购物车列表接口~~
  - ~~接口路径：`/api/cart/list`~~
  - ~~为后续 Day08 订单创建提供前置数据来源~~

- [x] ~~梳理 Day07 与 Day08 的衔接关系~~
  - ~~收藏表示弱购买意向~~
  - ~~购物车表示强购买意向~~
  - ~~Day07 只记录用户想买什么，Day08 再正式生成订单~~

---
## 七、踩坑记录

| 问题                              | 原因                                        | 解决                                        |
| ------------------------------- | ----------------------------------------- | ----------------------------------------- |
| 查询不存在商品返回“操作成功”但 data 为 null    | 接口成功 ≠ 业务成功，没有做判空处理                       | Service 层判断 `product == null`，不存在返回 error |
| 商品分类和商品列表概念混淆                   | 把“分类”和“商品数据”当成同一层级                        | 分类是筛选条件，商品才是实际数据                          |
| 按商品ID查询和按分类查询逻辑混乱               | 没区分“唯一查询”和“条件查询”                          | 商品ID → 单个商品；分类ID → 商品列表                   |
| 分类查询只返回一个商品（理解错误）               | 不理解分类与商品是 1:N 关系                          | 一个分类对应多个商品，本质是列表查询                        |
| 搜索关键词"手机"返回的是游戏手柄，而不是标题含"手机"的商品 | 当前搜索逻辑是"标题 OR 描述"匹配，没有优先级区分               | 绿叶篇优化：标题匹配优先于描述匹配                         |
| 编辑商品时没传的字段被覆盖成 null             | 直接使用 `updateById`，null 字段也被更新             | 先查询原数据，再按需 set 有值字段更新                     |
| categoryId 传 null 时查不到数据        | 使用 `eq(..., null)` 导致 SQL 为 `= NULL`，条件无效 | 先判断 `categoryId != null` 再拼接条件            |
| DELETE 请求参数放错位置                 | 将 ID 放在 Body 中，不符合 RESTful 规范             | 使用路径参数 `/delete/{id}`                     |
| PUT 请求未传 id                     | 不清楚更新必须指定数据主键                             | PUT 请求 JSON 中必须包含 id                      |
| 接口分组混乱（分类接口归属不清）                | 按数据表划分模块，而不是按业务划分                         | 分类接口归属商品模块                                |
| 笔记结构混乱（龙骨与绿叶不分）                 | 把扩展功能写进主流程                                | 主流程写核心功能，扩展内容放绿叶篇                         |
| 商品列表返回数据过多                      | 未做分页控制                                    | 使用分页查询（Page）限制返回数量                        |
| Service 和 Mapper 职责不清           | 直接返回 mapper 查询结果，缺少业务处理                   | Service 层处理业务逻辑，Mapper 只负责查询              |
| 接口设计不清晰（返回结构混乱）                 | 没有提前设计返回单个还是列表                            | 先明确返回结构，再设计接口                             |

**绿叶篇核心收获🚀**

| 能力         | 内容                   | 本质理解           |
| ---------- | -------------------- | -------------- |
| ⭐ 分层设计     | 引入 DTO / VO / Query  | 输入、查询、输出解耦     |
| ⭐ VO 设计    | ListVO / DetailVO 拆分 | 列表和详情是不同场景     |
| ⭐ DTO 设计   | 发布 / 编辑 分离           | 不同操作使用不同输入模型   |
| ⭐ Query 抽象 | 查询条件独立封装             | 查询也是一种“输入模型”   |
| ⭐ 局部更新     | 只更新非空字段              | 避免数据被覆盖        |
| ⭐ 参数校验升级   | if → 注解校验            | 从手写校验升级为框架校验   |
| ⭐ 业务校验意识   | DTO ≠ 业务校验           | Service 专注业务规则 |
| ⭐ 搜索优化意识   | 标题优先                 | 初步建立“相关性”概念    |
| ⭐ 缓存引入     | Redis 浏览量缓存          | 数据从 DB → Cache |
|⭐ 架构思维|Controller / Service / Mapper 分层|职责清晰|

---
## 八、我说得不多：(◍・ᴗ・◍)(2026/05/01)

【作者说：从昨天开始(准确来说是十三个小时前)我已经迎来了我的五一假期了(｡・́ω・̀｡)，！happy ！没错现在是五月一号的1：20，我正在完成这个日记的【作者说】环节，昨天周四我没有课，嘻嘻๑˃̵ᴗ˂̵๑，写到这里确实很犯困། – _ – །，但是我还是想把它完成，这样的话明天就继续完成这个绿叶🌿篇的扩展就可以了，这样的话整个商品体系就算是完成了，今天确实没有什么值得记录在日记里面的事情，确实是平淡无趣的，三个舍友都回家了，只留我一人独守寝室，这也成就了我可以在这个点进行笔记完善！--确实不错！(´-ω-)
    对于这个笔记来讲，完善完成之后肯定还是会有三万字的篇幅，虽然在day04的时候我说了篇幅保证的话语，但是我确实不是很想因为篇幅太长来进行笔记中关键内容的缩减，我觉得是得不偿失的，所以我决定还是按照一套优雅的逻辑框架来进行笔记的撰写，这很重要，我也很喜欢这个框架！ฅ՞・ﻌ・՞ฅ  这套笔记框架可以说是非常完美了，怎么说呢！它将整个商品体系拆分成一个前置依赖+三个大功能，这三个功能分别贯穿整个体系，分别是浏览+查询+管理，这每一个大功能下面都会有具体的接口实现方式，完善这个接口实现方式又是严格按照一定的科学的操作流程与逻辑顺序进行的，所以可以说是高级版的俄罗斯套娃，我套啊套......
今天这个篇幅确实超长，完全可以顶我前面两篇笔记，但是没办法嘛，我在不断完善笔记写笔记的过程中也是在提高我的架构思想，我认为这是我做笔记写系列日记的重大财富，所以这个思想不断的完善已经足够，其他的一切都不是那么重要！( ´▽｀)
     另外，这篇文章它也是一个"龙骨"+"绿叶"🐉+🌿 的组合，也就是"核心主篇"+"补强附篇"的组合，这篇就是🐉，也就是整个商品体系的所有接口功能，我会在明天完成这篇的扩展补强内容滴！尽情期待吧！！！🌿✨(・̀ᴗ・́)و🌿
     今天也是依旧很累啊˚‧º·(˚ ˃̣̣̥᷄⌓˂̣̣̥᷅ )‧º·˚，确实没啥能写的内容，所以我就只能说些废话了！
    ok，至此，我在平平无奇的一天成功完成了day06主篇的笔记，明天我将会进行day06的绿叶篇扩展，这些扩展内容也是与上面的章节有着深深的绑定的，到时候会反向链接出来，方便随时复习，也可以更轻松地查阅。⚔️(｀・ω・´)
收工收工，困倦收工☁️ 🌊 ⛵未完待续，敬请期待day06🌿的内容吧(๑˃̵ᴗ˂̵)و！！！ 】



