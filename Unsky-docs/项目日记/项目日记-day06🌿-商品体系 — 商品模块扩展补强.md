
> 🌿日期：2026/05/01-05/03
> 🌿目标：用于补充主篇未覆盖的扩展功能与优化细节，完善商品模块整体实现。

--- 

> 🌿绿叶篇按照“结构 → 查询 → 返回 → 单点增强 → 业务补强 → 性能优化”的顺序逐步展开，而非简单按功能模块划分。

> 本篇为 Day06 商品体系主篇的补强篇，主流程详见：
> [[项目日记-day06-商品体系 — 商品浏览 & 搜索过滤]]

---

## 一、查询结构工程化设计
### 1.1 DTO/Query分层

1. 核心概念

- **DTO**（Data Transfer Object）：数据传输对象，用于接口层接收或返回数据
- **Query**：DTO 的一种特化形式，专门用于封装查询条件

2. 存在问题

> 当查询条件较多时，如果直接用 `@RequestParam` 逐个接收，会出现：

- Controller 方法参数列表过长，影响可读性
- 后续新增参数需要改接口签名，影响前端对接
- Service 层方法参数也要跟着改，牵一发动全身

3. **解决思路：** 创建一个专门的 Query 类，把所有查询参数装进去，接口只接收一个对象。

```Java
public Result list(ProductQuery query)
```

优点：

- 简化接口定义
- 提高扩展性（新增字段无需修改方法签名）
- 便于后续统一处理（如分页、排序、条件构建

4. 具体 Query 对象的字段设计与使用方式，将会展示在[[项目日记-day06🌿-商品体系 — 商品模块扩展补强#2.3 查询参数封装(Query)]]里面详细展开

### 1.2 Service结构优化

在完成基础功能实现后，可以对 Service 层结构进行适当优化，以提升代码的可维护性与扩展性。初始阶段的 Service 往往以“功能优先”为主，可能会将查询条件构建、排序逻辑、分页处理等内容集中在同一方法中，虽然能够快速实现功能，但随着业务复杂度增加，代码会逐渐变得臃肿且难以复用。

因此，在结构优化过程中，可以将通用逻辑进行适当拆分与抽离，例如将查询条件封装为统一的参数对象（如 ProductQuery），将排序与分页逻辑进行规范化处理，使 Service 层更加专注于业务流程的组织，而非具体实现细节。同时，通过统一返回结构（如分页结果），可以保证接口风格一致，降低前后端对接成本。

通过上述优化，Service 层能够从“功能堆叠”逐步演进为“结构清晰、职责明确”的设计，为后续功能扩展（如搜索、筛选、多条件组合查询等）提供良好的基础。

---

## 二、商品列表能力扩展

### 2.1 分页查询的实现

> 本节基于主篇商品列表接口继续增强：
> [[项目日记-day06-商品体系 — 商品浏览 & 搜索过滤#2.2 实现商品列表接口/api/product/list]]

1. 创建分页插件配置类

> - 在使用 `MyBatis-Plus` 实现分页查询时，不能只在代码中调用 `Page` 和 `selectPage` 方法，还需要提前配置分页插件（`MyBatis Plus Config`）。这是因为分页功能并不是默认生效的，它依赖底层拦截器对 `SQL` 进行自动改写，从而实现分页逻辑（如添加 `limit` 语句）。
> 
> - 通过引入分页插件配置类，可以让框架在执行查询时自动完成分页处理，无需手动拼接 `SQL`，提高开发效率，同时保证分页查询的规范性与稳定性。

```Java
@Configuration// ← 告诉Spring这是一个配置类，Spring会自动扫描加载  
public class MybatisPlusConfig {  
  
    @Bean // ← 把这个方法的返回值交给Spring管理，相当于注册到容器里  
    public MybatisPlusInterceptor mybatisPlusInterceptor() {  
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();  
        //分页插件  
        interceptor.addInnerInterceptor(new PaginationInnerInterceptor(DbType.MYSQL));  
        return interceptor;  
    }  
}
```

**PaginationInnerInterceptor** 是"分页拦截器"，它的作用是：

- 每次执行 SELECT 查询时，自动在 SQL 末尾拼接 `LIMIT`
- 自动计算 `COUNT(*)` 查询总条数
- `DbType.MYSQL` 是告诉它"你的数据库是MySQL"

> `MybatisPlusConfig` 的作用：**告诉 MyBatis-Plus，遇到分页查询时，自动在 SQL 后面拼接 LIMIT 语句。**

2. 在`ProductQuery`里面补充分页参数。

```Java
//分页参数（带默认值）  
private Integer pageNum;//默认第一页  
private Integer pageSize;//默认每页10条
```

> 在查询参数封装的`ProductQuery`里面补充上述分页参数

3. 在`ProductService`接口里将返回值类型修改为分页查询

```Java
// 修改返回值类型为分页参数 <IPage<Product>>
Result<IPage<Product>> searchProducts(ProductQuery productQuery);
```

4. 在`ProductServiceimpl`里面补充分页查询操作实现方法

```Java
    // 4. 按创建时间倒序；若时间相同，则按ID倒序，保证排序稳定性（分页场景必须） 
    wrapper.orderByDesc(Product::getCreateTime);  
    wrapper.orderByDesc(Product::getId); //绿叶篇补充----龙骨篇已修正
  
    // 5. 分页查询  
    IPage<Product> page = new Page<>(  
            productQuery.getPageNum(),  
            productQuery.getPageSize()  
    );  
    IPage<Product> resultPage = productMapper.selectPage(page, wrapper);  
    //List<Product>productList=productMapper.selectList(wrapper);分页查询替换查询列表  
    return Result.success(resultPage);  
}
```

>该问题最初出现在列表查询（list）中，在迁移到分页查询（page）后，如果不处理排序稳定性，会进一步导致分页数据错乱问题。 

5. 在`Apifox`里测试并验证分页查询的实现

![[Pasted image 20260502135007.png]]

![[Pasted image 20260502135321.png]]

> 1. pageSize 控制“每页返回数据条数”
> 2. pageSize = 0 → 不返回数据（records 为空）
> 3. total 始终表示数据库中满足条件的总记录数
> 4. pages = 0 表示分页无效（无法分页）

- 分页参数不仅影响数据展示，还直接决定查询结果结构，必须进行有效性控制。

### 2.2 多条件排序扩展

 **目前只有一种排序：按创建时间倒序。** 
 
>  除此之外，还应该拓展：
> - 按价格从低到高（便宜优先）
> - 按价格从高到低（贵的在前）
> - 按浏览量从高到低（热门优先）

- 注意：具体操作部分中不用大修改的`ProductService` 和`ProductController`就不在展示了

1. 在 `ProductQuery` 中增加排序参数

> 前端传一个 `sortBy` 参数，后端根据它的值决定排序方式。

```java
// 排序字段（可选）
// 可选值：price_asc（价格升序）、price_desc（价格降序）、
//        view_count（浏览量降序）、create_time_desc（最新优先）
private String sortBy;
```

2. 在 `ProductServiceImpl` 中实现动态排序

```Java
// 4. 按创建时间倒序；若时间相同，则按ID倒序，保证排序稳定性（分页场景必须）  
//这是2.1的版本，2.2将它合并为多条件排序  
//wrapper.orderByDesc(Product::getCreateTime);  
//wrapper.orderByDesc(Product::getId);  
  
// 4.动态排序，可以个根据价格，浏览量，发布时间来具体排序  
if (productQuery.getSortBy() != null&& !productQuery.getSortBy().isEmpty()) {  
    switch (productQuery.getSortBy()) {  
        case "price_asc":  
            wrapper.orderByAsc(Product::getPrice);  
            break;  
        case "price_desc":  
            wrapper.orderByDesc(Product::getPrice);  
            break;  
        case "view_count":  
            wrapper.orderByDesc(Product::getViewCount);  
            break;  
        case "create_time_desc":  
            wrapper.orderByDesc(Product::getCreateTime);  
            break;  
        default:  
            wrapper.orderByDesc(Product::getCreateTime);  
            break;  
    }  
} else {  
    // 默认按创建时间倒序（来源于2.1）  
    wrapper.orderByDesc(Product::getCreateTime);  
}  
//全局兜底，保证在任意排序条件下，当字段值相同时，结果顺序稳定，直接按照id倒序排序  
wrapper.orderByDesc(Product::getId);
```

3. 在Apifox里面测试多条件排序功能

![[Pasted image 20260502152600.png]]

> 如图所示，测试成功；我上面的参数是查询一页九条按照价格从低到高的数据，因截图接不完全，后面的内容就不展示了，包括还有按照价格从高到低；浏览量排序会在4.1浏览的扩展小模块里实现浏览量后在进行排序的测试验证・́ω・̀

### 2.3 查询参数封装(Query)

> 我将查询参数单独抽离为 Query 对象，放在 product.query 包中，与 DTO 和 VO 分层管理。  
> 这样可以避免不同用途的数据对象混在一起，提高代码的可维护性和扩展性。

1. 修改`ProductQuery.java`来封装分页查询条件

```Java
/**  
 * 商品查询条件封装  
 * 用于接收前端传入的查询参数  
 *  示  - * @param categoryId 分类ID（可选，传null表示不限制）  
 *  例  - * @param keyword    搜索关键词（可选，传null表示查全部）  
 *  展  - * @param minPrice   最低价格（可选，传null表示不限制最低价）  
 *  示  - * @param maxPrice   最高价格（可选，传null表示不限制最高价）
 *  ！  - * @param pageNum    当前页码（默认第1页）
 *  ！  - * @param pageSize   每页条数（默认10条）
 *  ！  - * @param sortBy     排序字段（可选）
 */  
@Data  
public class ProductQuery {  
  
    // 分类ID（可选）  
    private Long categoryId;  
  
    // 搜索关键词（可选）  
    private String keyword;  
  
    // 最低价格（可选）  
    private BigDecimal minPrice;  
  
    // 最高价格（可选）  
    private BigDecimal maxPrice;  
  
    //分页参数（带默认值）  
    private Integer pageNum;//默认第一页  
    private Integer pageSize;//默认每页10条  
  
    // 排序字段（可选）  
    // 可选值：price_asc（价格升序）、price_desc（价格降序）、  
    //        view_count（浏览量降序）、create_time_desc（最新优先）  
    private String sortBy;  
}
```

2. 修改 `ProductService` 接口的分页查询参数

```Java
// 修改返回值类型为分页参数 <IPage<Product>>----day06🌿2.1
Result<IPage<Product>> searchProducts(ProductQuery productQuery);
```

3. 修改 `ProductServiceImpl` 分页查询参数实现

```java
/**  
 * 商品列表查询（支持分类筛选 + 关键词搜索 + 价格区间筛选）  
 * @param productQuery 封装查询参数  
 * @return 匹配的上架商品列表  
 */  
@Override  
public Result<IPage<Product>> searchProducts(ProductQuery productQuery) {  
    LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<>();  
  
    // 1. 只查上架商品  
    wrapper.eq(Product::getStatus, 1);  
  
    // 2. 如果传了categoryId，就加分类筛选条件  
    if (productQuery.getCategoryId() != null) {  
        wrapper.eq(Product::getCategoryId, productQuery.getCategoryId());  
    }  
  
    // 3. 如果传了keyword，就做模糊匹配  
    if (StringUtils.hasText(productQuery.getKeyword())) {  
        wrapper.and(w -> w  
                .like(Product::getTitle, productQuery.getKeyword())  
                .or()  
                .like(Product::getDescription, productQuery.getKeyword())  
        );  
        //productQuery.getKeyword().trim().isEmpty() 多加一个判断：用户可能传了空格，空格不应该被当作有效关键词  
        //使用 like 而不是 eq：like 是模糊匹配，eq 是精确匹配，搜索场景必须用 like    }  
  
    // 4. 如果传了最低价格，就筛选 price >= minPrice    if (productQuery.getMinPrice() != null) {  
        wrapper.ge(Product::getPrice, productQuery.getMinPrice());  
    }  
  
    // 5. 如果传了最高价格，就筛选 price <= maxPrice    if (productQuery.getMaxPrice() != null) {  
        wrapper.le(Product::getPrice, productQuery.getMaxPrice());  
    }  
  
    // 4. 按创建时间倒序；若时间相同，则按ID倒序，保证排序稳定性（分页场景必须）  
    //这是2.1的版本，2.2将它合并为多条件排序  
    //wrapper.orderByDesc(Product::getCreateTime);  
    //wrapper.orderByDesc(Product::getId);  
    // 4.动态排序，可以个根据价格，浏览量，发布时间来具体排序  
    if (productQuery.getSortBy() != null&& !productQuery.getSortBy().isEmpty()) {  
        switch (productQuery.getSortBy()) {  
            case "price_asc":  
                wrapper.orderByAsc(Product::getPrice);  
                break;  
            case "price_desc":  
                wrapper.orderByDesc(Product::getPrice);  
                break;  
            case "view_count":  
                wrapper.orderByDesc(Product::getViewCount);  
                break;  
            case "create_time_desc":  
                wrapper.orderByDesc(Product::getCreateTime);  
                break;  
            default:  
                wrapper.orderByDesc(Product::getCreateTime);  
                break;  
        }  
    } else {  
        // 默认按创建时间倒序（来源于2.1）  
        wrapper.orderByDesc(Product::getCreateTime);  
    }  
    //全局兜底，保证在任意排序条件下，当字段值相同时，结果顺序稳定，直接按照id倒序排序  
    wrapper.orderByDesc(Product::getId);  
  
    // 5. 分页查询  
    IPage<Product> page = new Page<>(  
            productQuery.getPageNum(),  
            productQuery.getPageSize()  
    );  
    IPage<Product> resultPage = productMapper.selectPage(page, wrapper);  
    //List<Product> productList = productMapper.selectList(wrapper);分页查询替换查询列表  
    return Result.success(resultPage);  
}
```

4. 修改 `ProductController` 接口中查询参数

```Java
/**  
 * 查询商品列表（支持分类筛选 + 关键词搜索 + 价格区间筛选）  
 * @param productQuery 封装查询参数  
 * @return 商品列表  
 */  
@GetMapping("/list")  
public Result<IPage<Product>> listProduct(ProductQuery productQuery) {  
    return productService.searchProducts(productQuery);  
}
```

5. 在`Apifox`中测试并验证查询参数

> 下面的测试截图没有做修改，只展示原有的查询参数查询，但是上面的代码已经改成分页查询的结果了，这里截图就不做修改，分页查询的功能已经成功实现

![[Pasted image 20260501160806.png]]

> 由于数据库里的种类和商品不是很多，测试有一定的局限性，但仍展示了正确查询结果

> 当前实现为基础查询能力，仅完成查询条件的封装与动态拼装。
> 在实际项目中，列表查询通常需要结合分页能力以避免数据量过大带来的性能问题。
> 分页功能将在 2.1 中进行统一实现与升级，伴随着2.1的完成，已对上面笔记做了返回值类型修改，新增了分页查询的参数，成功实现了分页查询。

---
## 三、商品返回结构设计

> 在这个返回结构中，当前 `searchProducts` 返回的是 `IPage<Product>`，也就是直接返回了商品实体类 `Product`里所有的字段，但是会面临一些问题----关于后端内部字段用返回吗？前端不需要的字段要返回吗？所以直接返回这个实体类是不合适的，商品列表只需要展示字段，完整字段是详情页的事，所以需要重新设计返回结构◔.̮◔

- 核心概念：**VO**（View Object）：专门给前端返回用的对象。只返回前端需要的字段，不需要的字段不返回----(自定义的返回)!!!
- 核心作用：

>    VO 用于封装返回给前端的数据，与数据库实体（Entity）解耦。  
> - 控制返回字段（避免敏感数据泄露）
> - 优化数据结构（如格式转换）
> - 提升接口可维护性

> 拓展----解耦：
> 
>  解耦就是通过增加中间层，使系统各部分之间的依赖关系降低， 从而实现各模块可以独立变化而互不影响。
> 
>  在本项目中，通过引入 VO，将数据库实体与前端返回结构分离，使数据库字段变化不会直接影响前端，提高了系统的可维护性和扩展性。

### 3.1 VO对象设计

- 📌 补充说明（核心理解）：  
  
在分页查询场景中，原始返回类型为 `IPage<Product>`，但其中 `records` 字段是 `Product` 类型， 
无法自动转换为 `ProductVO`，因此必须手动进行数据转换并重新封装分页对象。  
  
- 整体流程如下：  
  
```txt
数据库查询 → IPage<Product>  
→ 转换 records → List<ProductVO> 
→ 重新构建 → IPage<ProductVO>
```

1. 创建`ProductVO`来展示前端需要的字段

```Java
/**  
 * 商品列表 VO（列表展示用）  
 * 只包含前端展示需要的字段  
 */  
@Data  
public class ProductVO {  
    // 商品ID  
    private Long id;    
    // 卖家ID  
    private Long sellerId;  
    // 商品标题  
    private String title;   
    // 商品描述（列表只展示前50字）  
    private String description;   
    // 售价  
    private BigDecimal price;   
    // 原价  
    private BigDecimal originalPrice;  
    // 分类ID  
    private Long categoryId;  
    // 商品图片（JSON转List）  
    private List<String> images;  
    // 浏览量  
    private Integer viewCount;  
    // 收藏量  
    private Integer favoriteCount;  
    // 新旧程度（1-5）  
    private Byte conditionLevel;  
    // 商品状态（1=上架）  
    private Byte status;  
    // 发布时间  
    private LocalDateTime createTime;  
}
```

2. 修改 `ProductService`中的返回值类型

> - 📌 注意：
> 
> - 仅“查询类接口”需要修改为 VO 返回类型，如：商品列表、商品详情等。
> - 而“写操作接口”（新增、修改）仍然使用 Entity，因为其主要作用是接收数据而不是返回展示数据。

```Java
//* 修改返回值类型为VO----day06🌿3.1
Result<IPage<ProductVO>> searchProducts(ProductQuery productQuery);
```

3. 在 `ProductServiceImpl`实现 `Entity → VO` 转换

- 在 Service 实现类中，新增转换方法：`convertToVO(Product product)`
- 在这里直接实现`JSON`数据的处理，需要引入`fastjson` 依赖，用于完成 `JSON` 数据解析

```Java
    /**
     * Entity → VO 转换方法
     * 作用：
     * 1. 完成字段映射（数据库字段 → 前端展示字段）
     * 2. 处理数据格式转换（如 JSON → List）
     * 3. 提供异常兜底，保证系统稳定性
     */
    private ProductVO convertToVO(Product product) {  
        ProductVO vo = new ProductVO();  
        vo.setId(product.getId());  
        vo.setSellerId(product.getSellerId());  
        vo.setTitle(product.getTitle());  
        vo.setDescription(product.getDescription());  
        vo.setPrice(product.getPrice());  
        vo.setOriginalPrice(product.getOriginalPrice());  
        vo.setCategoryId(product.getCategoryId());  
        vo.setViewCount(product.getViewCount());  
        vo.setFavoriteCount(product.getFavoriteCount());  
        vo.setConditionLevel(product.getConditionLevel());  
        vo.setStatus(product.getStatus());  
        vo.setCreateTime(product.getCreateTime());  
        // 将 images 的 JSON 字符串转换为 List        
        if (product.getImages() != null && !product.getImages().isEmpty()) {  
            try {  
             // 将数据库中的 JSON 字符串（如 ["a.jpg","b.jpg"]）
             // 转换为 Java 的 List<String>，方便前端直接使用
            vo.setImages(JSON.parseArray(product.getImages(), String.class));  
            } catch (Exception e) {  
                vo.setImages(new ArrayList<>());  
            }  
        } else {  
            vo.setImages(new ArrayList<>());  
        }  
        return vo;  
    }  
```

4. 在`ProductController`里面进行返回值类型的修改

```Java
@GetMapping("/list")  
public Result<IPage<ProductVO>> listProduct(ProductQuery productQuery) {  
    return productService.searchProducts(productQuery);  
}
```

> - 📌 说明：
> - Controller 层只负责接收请求与返回结果，不参与数据转换逻辑。
> - `Entity → VO` 的转换应在 `Service` 层完成，保证分层职责清晰。

5. 在Apifox里面测试images字段是否按要求返回

![[Pasted image 20260502165006.png]]

> 如上图标注所示，images的字段返回成功变成数组格式

> 通过引入 VO 对象并进行数据转换，实现了：
> 
> - 返回结构可控（避免冗余字段）
> - 数据格式优化（JSON → List）
> - 前后端解耦（Entity ≠ 返回结构）
> 
> 这是后端接口设计从“能用”向“规范化”的重要一步。

### 3.2 列表VO与详情VO拆分

> 当前商品详情接口 `getProductDetail` 返回的是 `Product` 实体类，包含所有字段，这也要修改为`ProductVO`，虽然列表和详情都返回`ProductVO`，但是很明显，它们实际上的字段根本不同

| 场景                  | 需要哪些字段                                             |
| ------------------- | -------------------------------------------------- |
| 列表`ProductListVO`   | `id`、`title`、`price`、`images`、`viewCount` 等概要信息    |
| 详情`ProductDetailVO` | 上述全部 + `sellerId`、`description`、`createTime` 等完整信息 |
> 因此就要对两个VO进行区别，由于笔记已经对列表VO进行了完善，所以接下来补充详情VO
> 注意：我在这里把列表`VO`重命名为`ProductListVO`以作区分

1. 创建`ProductDetailVO`来包含详情页需要的全部字段

```Java
@Data  
public class ProductDetailVO {  
    // 商品ID  
    private Long id;   
    // 卖家ID  
    private Long sellerId;  
    // 商品标题  
    private String title;   
    // 商品描述（详情页展示完整描述）  
    private String description;  
    // 售价  
    private BigDecimal price;  
    // 原价  
    private BigDecimal originalPrice;  
    // 分类ID  
    private Long categoryId;  
    // 商品图片  
    private List<String> images;  
    // 浏览量  
    private Integer viewCount;  
    // 收藏量  
    private Integer favoriteCount;  
    // 新旧程度（1-5）  
    private Byte conditionLevel;  
    // 商品状态（1=上架）  
    private Byte status;  
    // 发布时间  
    private LocalDateTime createTime;  
}
```

2. 修改 `ProductService` 返回类型

```Java
Result<ProductDetailVO> getProductDetail(Long id);
```

3. 修改`ProductServiceImpl` 中的返回值类型

```Java
@Override  
public Result<ProductDetailVO> getProductDetail(Long id) {  
    // 1. 先查询商品（只查上架的）  
    LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<>();  
    wrapper.eq(Product::getId, id)  
            .eq(Product::getStatus, 1);  // 只查上架的商品  
  
    Product product = productMapper.selectOne(wrapper);  
  
    // 2. 判断商品是否存在  
    if (product == null) {  
        return Result.error("商品不存在或已下架");  
    }  
    //后续会扩展浏览量业务  
    return Result.success(convertToDetailVO(product));  
}
```

4. 在`ProductServiceImpl`里面添加转换方法

```Java
/**  
 * Entity → 详情VO 转换方法  
 * 作用：  
 * 1. 完成字段映射（数据库字段 → 前端详情展示字段）  
 * 2. 返回完整商品信息（区别于列表VO的精简字段）  
 * 3. 处理数据格式转换（如 JSON → List）  
 * 4. 提供异常兜底，保证系统稳定性  
 * @param product  
 * @return  
 */  
private ProductDetailVO convertToDetailVO(Product product) {  
    ProductDetailVO vo = new ProductDetailVO();  
    vo.setId(product.getId());  
    vo.setSellerId(product.getSellerId());  
    vo.setTitle(product.getTitle());  
    vo.setDescription(product.getDescription());  
    vo.setPrice(product.getPrice());  
    vo.setOriginalPrice(product.getOriginalPrice());  
    vo.setCategoryId(product.getCategoryId());  
    vo.setViewCount(product.getViewCount());  
    vo.setFavoriteCount(product.getFavoriteCount());  
    vo.setConditionLevel(product.getConditionLevel());  
    vo.setStatus(product.getStatus());  
    vo.setCreateTime(product.getCreateTime());  
  
    // images JSON 转 List    if (product.getImages() != null && !product.getImages().isEmpty()) {  
        try {  
            vo.setImages(JSON.parseArray(product.getImages(), String.class));  
        } catch (Exception e) {  
            vo.setImages(new ArrayList<>());  
        }  
    } else {  
        vo.setImages(new ArrayList<>());  
    }  
  
    return vo;  
}
```

5. 修改 `ProductController` 返回类型

```Java
@GetMapping("/detail/{id}")  
public Result<ProductDetailVO> getProductDetail(@PathVariable Long id) {  
    return productService.getProductDetail(id);  
}
```

6. 在`Apifox`里面测试并验证详情`VO`的接口

![[Pasted image 20260502225102.png]]

> 详情接口测试重点在于验证数据完整性，包括 images 字段是否成功转换为数组格式，以及 description、时间等字段是否完整返回。

---
## 四、商品详情能力增强

### 4.1 浏览量统计实现

> 浏览量统计属于是这个项目的业务开发部分，当用户查看商品详情时，浏览量应该 +1，整个项目也会有用到浏览量的扩展，包括前面的按照浏览量排序，所以需要实现浏览量的统计；浏览量是商品表的一个字段 `view_count`，只需要在查询详情的同时更新它。

1. 在 `ProductServiceImpl` 的商品详情方法中，实现浏览量 +1 并更新数据库

```Java
// 浏览量 +1
  product.setViewCount(product.getViewCount() + 1);  
  productMapper.updateById(product);
```

2. 在 `Apifox` 中多次调用详情接口，验证 `view_count` 是否随访问次数递增

- 第一次调用，显示浏览量为1：

![[Pasted image 20260503001409.png|462]]

- 第二次调用，显示浏览量为2：
![[Pasted image 20260503001504.png|463]]

> 经过上面测试，实现了浏览量的增加，浏览量业务的扩展成功实现

> 浏览量统计属于业务增强逻辑，通过在详情接口中增加自增操作，实现用户访问行为的记录。

### 4.2 浏览量Redis缓存优化

- 在高并发场景下，如果每次查看商品都直接更新数据库，会带来较大压力，因此引入 `Redis` 缓存，将浏览量统计从数据库转移到缓存中，提高性能。

 1. 引入 `Redis` 依赖(`pom.xml`)

```xml
<!-- 配置Redis -->  
<dependency>  
    <groupId>org.springframework.boot</groupId>  
    <artifactId>spring-boot-starter-data-redis</artifactId>  
</dependency>  
```

📌 **作用说明：**  
提供 `Redis` 连接与操作能力（如 `RedisTemplate`），用于后续缓存操作。

2. 配置 `Redis` 连接（`application.yml`）

```yml
# =================== Redis 配置 ===================
spring
  redis:  
     host: localhost        # Redis 服务器地址  
     port: 6379             # Redis 端口  
     database: 0            # Redis 数据库索引  
     timeout: 5000ms        # 连接超时时间  
     lettuce:  
        pool:  
          max-active: 8      # 最大连接数  
          max-idle: 8        # 最大空闲连接数  
          min-idle: 0        # 最小空闲连接数  
          max-wait: -1ms     # 最大等待时间
```

📌 **作用说明：**  
让 Spring Boot 项目能够成功连接本地 Redis 服务。

3. 在`ProductServiceImpl`里面写具体实现操作

```Java
// Redis Key（按商品ID区分）
String key = "product:viewCount:" + id;

// 浏览量 +1（写入Redis）
redisTemplate.opsForValue().increment(key, 1);

// 数据库同步更新（当前为简单实现）
product.setViewCount(product.getViewCount() + 1);
productMapper.updateById(product);
```

📌 **作用说明：**

- 将浏览量统计写入 Redis（高性能）
- 同时更新数据库（保证数据可持久化）
- 为后续缓存优化（第七大点）做铺垫

> 📌 注意：当前属于“缓存 + 数据库双写”模式，在高并发场景下可能出现数据不一致问题（如丢失更新）。

4. 在`Apifox`里面进行浏览量的测试

![[Pasted image 20260503114912.png]]

> 返回成功，商品详情数据正常，`viewCount`正常显示

5. `Redis`数据验证----终端测试与返回

```
127.0.0.1:6379> keys *
1) "product:viewCount:1"
```

```
127.0.0.1:6379> get product:viewCount:1
"1"   多次调用接口后，该值会持续递增（如 1 → 2 → 3）
```

- 成功生成 Redis key：`product:viewCount:1`
- 每次访问接口，浏览量都会递增
- 说明缓存逻辑生效 ✅

#### ⚠️ 当前实现说明

> 当前方案为：  
> **Redis + 数据库同步更新（简单实现）**

存在问题：

- 高并发下可能产生数据不一致
- 数据库仍然有更新压力

#### 🚀 后续优化方向（第七大点）

> 将升级为：

- Redis 实时计数
- 定时任务批量写入数据库
- 实现“最终一致性”

---

## 五、商品搜索体系优化

### 5.1 搜索匹配逻辑说明

> keyword 基础搜索已在主篇完成：
> [[项目日记-day06-商品体系 — 商品浏览 & 搜索过滤#3.2 实现按关键词搜索]]

- keyword 搜索已经在龙骨篇中完成，因此这里不再重复实现搜索接口，只对当前搜索匹配逻辑进行补充说明。

> 在基础 keyword 搜索中发现问题：部分结果虽然包含关键词，但相关性较低（如“手机和游戏手柄”匹配“手机”），因此通过同时匹配标题和描述字段，扩大搜索覆盖范围，从而提升搜索结果的相关性。

具体实现方式：

```Java
wrapper.and(w -> w
    .like(Product::getTitle, keyword)
    .or()
    .like(Product::getDescription, keyword)
);
```

> 项目代码已经修改，这里只是说明原因(° ー °〃)
> 当前基于数据库模糊查询，无法实现精确的相关性排序，仅能通过字段匹配范围进行优化。
> 后续可引入 Elasticsearch 实现更精细的相关性排序（权重、分词等）

### 5.2 查询条件组合逻辑整理

> 随着商品列表接口不断扩展，当前 `/api/product/list` 已经不再是简单的查询全部商品，而是同时承担了多种查询能力，由于目前关于查询的条件越来越多，使得`searchProducts`这个方法逻辑有些混乱，为了要让它有固定顺序和默认规则，所以这一节不做大改动，只做**结构调整**。
> 
- 形成规范：固定条件先写，动态筛选再写，排序最后写，分页查询最后执行

整理后完整代码：

```Java
@Override  
public Result<IPage<ProductListVO>> searchProducts(ProductQuery productQuery) {  
    // 1. 创建分页对象  
    Page<Product> page = new Page<>(  
            productQuery.getPageNum(),  
            productQuery.getPageSize()  
    );  
    // 2. 创建查询条件构造器  
    LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<>();  
  
    // 3. 固定条件：只查上架商品  
    wrapper.eq(Product::getStatus, 1);  
  
    // 4. 动态条件：如果传了categoryId，就加分类筛选条件  
    if (productQuery.getCategoryId() != null) {  
        wrapper.eq(Product::getCategoryId, productQuery.getCategoryId());  
    }  
  
    // 5. 动态条件：如果传了keyword，就做模糊匹配----关键词搜索  
    if (StringUtils.hasText(productQuery.getKeyword())) {  
        wrapper.and(w -> w  
                .like(Product::getTitle, productQuery.getKeyword())  
                .or()  
                .like(Product::getDescription, productQuery.getKeyword())  
        );  
        //productQuery.getKeyword().trim().isEmpty() 多加一个判断：用户可能传了空格，空格不应该被当作有效关键词  
        //使用 like 而不是 eq：like 是模糊匹配，eq 是精确匹配，搜索场景必须用 like    }  
  
    // 6. 动态条件：如果传了最低价格，就筛选 price >= minPrice    if (productQuery.getMinPrice() != null) {  
        wrapper.ge(Product::getPrice, productQuery.getMinPrice());  
    }  
  
    // 7. 动态条件：如果传了最高价格，就筛选 price <= maxPrice    if (productQuery.getMaxPrice() != null) {  
        wrapper.le(Product::getPrice, productQuery.getMaxPrice());  
    }  
  
    // 7.5 按创建时间倒序；若时间相同，则按ID倒序，保证排序稳定性（分页场景必须）  
    //这是2.1的版本，2.2将它合并为多条件排序  
    //wrapper.orderByDesc(Product::getCreateTime);  
    //wrapper.orderByDesc(Product::getId);  
    // 8. 动态排序：可以个根据价格，浏览量，发布时间来具体排序  
    if (productQuery.getSortBy() != null&& !productQuery.getSortBy().isEmpty()) {  
        switch (productQuery.getSortBy()) {  
            case "price_asc":  
                wrapper.orderByAsc(Product::getPrice);  
                break;  
            case "price_desc":  
                wrapper.orderByDesc(Product::getPrice);  
                break;  
            case "view_count":  
                wrapper.orderByDesc(Product::getViewCount);  
                break;  
            case "create_time_desc":  
                wrapper.orderByDesc(Product::getCreateTime);  
                break;  
            default:  
                wrapper.orderByDesc(Product::getCreateTime);  
                break;  
        }  
    } else {  
        // 9. 默认排序：最新发布优先（来源于2.1）  
        wrapper.orderByDesc(Product::getCreateTime);  
    }  
    // 10. 稳定排序：避免分页时相同时间数据顺序不稳定  
    //全局兜底，保证在任意排序条件下，当字段值相同时，结果顺序稳定，直接按照id倒序排序  
    wrapper.orderByDesc(Product::getId);  
  
    // 11. 执行分页查询，查询结果中的 records 仍然是 Product 实体对象  
    IPage<Product> resultPage = productMapper.selectPage(page, wrapper);  
    //List<Product> productList = productMapper.selectList(wrapper);分页查询替换查询列表  
  
    // 12. 将当前页的 Product 列表转换为 ProductListVO 列表  
    List<ProductListVO> voList = resultPage.getRecords().stream()  
            .map(this::convertToVO)  
            .collect(Collectors.toList());  
  
    // 13. 创建一个新的 VO 分页对象，用于返回给前端  
    IPage<ProductListVO> voPage = new Page<>(  
            resultPage.getCurrent(),  
            resultPage.getSize()  
    );  
    // 14. 复制分页总条数  
    voPage.setTotal(resultPage.getTotal());  
    // 15. 设置转换后的 VO 列表  
    voPage.setRecords(voList);  
  
    // 16. 返回分页结果VO  
    return Result.success(voPage);  
}
```

整理后，`searchProducts` 方法的职责更加清晰：

- `ProductQuery` 负责接收查询参数
- `LambdaQueryWrapper` 负责构建动态查询条件
- `Page` 负责分页参数
- `ProductMapper` 负责执行数据库查询
- `ProductListVO` 负责控制返回给前端的数据结构

这样后续如果继续增加新的筛选条件，例如成色筛选、校区筛选、发布时间范围筛选，只需要在动态条件区域继续追加即可，不会破坏整体结构。


---

## 六、商品发布与接口增强

### 6.1 发布商品DTO与参数校验

- 核心目标：不再让 Controller 直接接收 `Product` 实体，而是创建一个专门的`ProductPublishDTO`，用它接收发布商品请求参数，并使用注解完成基础校验。

1. 创建`ProductPublishDTO`用来接收请求参数

```Java
@Data  
public class ProductPublishDTO {   
    // 商品标题  
    @NotBlank(message = "商品标题不能为空")  
    @Size(max = 50, message = "商品标题不能超过50个字符")  
    private String title;  
    //商品描述 
    @Size(max = 500, message = "商品描述不能超过500个字符")  
    private String description;  
    //商品价格
    @NotNull(message = "商品价格不能为空")  
    @DecimalMin(value = "0.01", message = "商品价格必须大于0")  
    private BigDecimal price;  
    //商品原价
    @DecimalMin(value = "0.01", message = "商品原价必须大于0")  
    private BigDecimal originalPrice;  
    //商品分类ID  
    @NotNull(message = "请选择商品分类")  
    private Long categoryId;  
    //商品成色
    private Byte conditionLevel;  
    //商品图片，当前先用字符串保存  
    //后续如果实现多图上传，可以再改成 List<String>
    private String images;  
}
```

> conditionLevel 使用枚举值（1-5）表示商品新旧程度，  避免直接使用中文字符串带来的解析问题。

`ProductPublishDTO` 只保留发布商品时前端需要提交的字段：

|字段|是否必填|说明|
|---|---|---|
|title|必填|商品标题|
|description|可选|商品描述|
|price|必填|商品价格|
|originalPrice|可选|商品原价|
|categoryId|必填|商品分类|
|conditionLevel|可选|商品成色|
|images|可选|商品图片信息|
2. 修改`ProductService` 接口方法入参

```Java
Result<Void> publishProduct(ProductPublishDTO productPublishDTO, Long userId);
```

> 发布商品接口只应该接收前端需要提交的字段，而不是直接暴露数据库实体类，`sellerId`，`status`、`viewCount` 等系统字段应由后端在 Service 层统一设置。

3. 修改`ProductServiceImpl` 实现类方法入参

```Java
@Override  
public Result<Void> publishProduct(ProductPublishDTO productPublishDTO, Long userId) {  
    // 1. DTO 转 Entity    
    Product product = new Product();  
    product.setTitle(productPublishDTO.getTitle().trim());  
    product.setDescription(productPublishDTO.getDescription());  
    product.setPrice(productPublishDTO.getPrice());  
    product.setOriginalPrice(productPublishDTO.getOriginalPrice());  
    product.setCategoryId(productPublishDTO.getCategoryId());  
    product.setConditionLevel(productPublishDTO.getConditionLevel());  
    product.setImages(productPublishDTO.getImages());  
  
    // 2. 设置系统字段  
    product.setSellerId(userId);  
    product.setStatus((byte) 1);  
    product.setViewCount(0);  
    product.setFavoriteCount(0);  
  
    // 3. 写入数据库  
    productMapper.insert(product);  
  
    return Result.success();  
}
```

4. 修改`ProductController`里面的方法参数

```Java
@PostMapping("/publish")  
public Result<Void> publishProduct(@Valid @RequestBody ProductPublishDTO productPublishDTO, HttpServletRequest request){  
    String token = request.getHeader("token");  
    Long userId = JwtUtil.getUserIdFromToken(token);  
    return productService.publishProduct(productPublishDTO, userId);  
}
```

> `@Valid` 的作用：触发 `ProductPublishDTO` 里的参数校验注解，避免 `Controller` 直接接收 `Product` 实体。

5. 在`Apifox`里面测试参数校验

![[Pasted image 20260503144549.png]]

> 上面测显示操作成功，数据库也同样更新这条测试数据，说明发布商品的参数校验正式完成，后面一些异常场景测试截图就不在这里展示了，下面有异常场景表格总结(●°u°●) 」

异常场景：

| 测试场景          | 预期结果      |
| ------------- | --------- |
| title 为空      | 商品标题不能为空  |
| price 为空      | 商品价格不能为空  |
| price 为负数     | 商品价格必须大于0 |
| categoryId 为空 | 请选择商品分类   |

### 6.2 防重复提交

> 这个方案不是最强防重复提交，但适合当前阶段，它主要防止用户连续点击发布按钮两次，从而导致商品被重复插入

在 `ProductServiceImpl`里的发布商品实现方法里补充：

```Java
// 1. 防重复提交：同一用户短时间内不能重复发布相同商品  
LambdaQueryWrapper<Product> repeatWrapper = new LambdaQueryWrapper<>();  
repeatWrapper.eq(Product::getSellerId, userId)  
        .eq(Product::getTitle, productPublishDTO.getTitle().trim())  
        .eq(Product::getPrice, productPublishDTO.getPrice())  
        .ge(Product::getCreateTime, LocalDateTime.now().minusMinutes(1));  
  
Long count = productMapper.selectCount(repeatWrapper);  
if (count > 0) {  
    return Result.error("请勿重复发布相同商品");  
}
```

> 当前采用轻量级数据库校验方案：同一用户在 1 分钟内不能重复发布相同标题、相同价格的商品。后续如果**并发要求更高**，可以使用 `Redis Token` 或唯一请求号实现更严格的幂等控制。

> 该方案主要解决普通重复点击场景，不作为高并发下的严格幂等方案。

### 6.3 商品编辑能力封装（DTO）

> 编辑商品基础接口来自主篇：
> [[项目日记-day06-商品体系 — 商品浏览 & 搜索过滤#4.2 实现编辑商品接口]]

1. 创建`ProductUpdateDTO`用来接收请求参数

```java
@Data
public class ProductUpdateDTO {
    //商品ID
    @NotNull(message = "商品ID不能为空")
    private Long id;
    //商品标题
    @Size(max = 50, message = "商品标题不能超过50个字符")
    private String title;
    //商品描述
    @Size(max = 500, message = "商品描述不能超过500个字符")
    private String description;
    //商品价格
    @DecimalMin(value = "0.01", message = "商品价格必须大于0")
    private BigDecimal price;
    //商品原价
    @DecimalMin(value = "0.01", message = "商品原价必须大于0")
    private BigDecimal originalPrice;
    //商品分类ID
    private Long categoryId;
    //商品成色
    private Byte conditionLevel;
    //商品图片
    private String images;
}
```

>相比 `ProductPublishDTO`，`ProductUpdateDTO` 多了商品 id，并且除 id 外其他字段都是可选更新。

> 这里和 `ProductPublishDTO` 的设计不同：
>
> - `ProductPublishDTO` 用于新增商品，因此标题、价格、分类等核心字段必须填写。
> - `ProductUpdateDTO` 用于编辑商品，当前采用“只更新非空字段”的方式，因此除了商品 id 必填外，其余字段可以不传。
>
> 也就是说：发布商品需要一份完整的创建数据；编辑商品只需要传要修改的字段。


2.  修改`ProductService` 接口方法入参

```JAVA 
Result<Void> updateProduct(ProductUpdateDTO productUpdateDTO, Long userId);
```

3. 修改`ProductServiceImpl` 实现类方法入参

```Java
/**  
 * 编辑商品  
 * @param productUpdateDTO 商品信息（包含商品ID和要修改的字段）  
 * @param userId  当前登录用户ID  
 * @return 编辑结果  
 */  
@Override  
public Result<Void> updateProduct(ProductUpdateDTO productUpdateDTO , Long userId) {  
    // 1. 查询  
    Product existProduct = productMapper.selectById(productUpdateDTO.getId());  
    if (existProduct == null) {  
        return Result.error("商品不存在");  
    }  
    // 2. 权限校验  
    if (!existProduct.getSellerId().equals(userId)) {  
        return Result.error("无权操作他人的商品");  
    }  
    // 3. 更新字段（只更新非空字段）  
    if (productUpdateDTO.getTitle() != null) {  
        existProduct.setTitle(productUpdateDTO.getTitle().trim());  
    }  
    if (productUpdateDTO.getDescription() != null) {  
        existProduct.setDescription(productUpdateDTO.getDescription());  
    }  
    if (productUpdateDTO.getPrice() != null) {  
        existProduct.setPrice(productUpdateDTO.getPrice());  
    }  
    if (productUpdateDTO.getOriginalPrice() != null) {  
        existProduct.setOriginalPrice(productUpdateDTO.getOriginalPrice());  
    }  
    if (productUpdateDTO.getCategoryId() != null) {  
        existProduct.setCategoryId(productUpdateDTO.getCategoryId());  
    }  
    if (productUpdateDTO.getConditionLevel() != null) {  
        existProduct.setConditionLevel(productUpdateDTO.getConditionLevel());  
    }  
    if (productUpdateDTO.getImages() != null) {  
        existProduct.setImages(productUpdateDTO.getImages());  
    }  
    // 4. 更新数据库  
    productMapper.updateById(existProduct);  
  
    return Result.success();  
}
```

4. 修改`ProductController`里面的方法参数

```Java
@PutMapping("/update")//----> PUT = 修改  
public Result<Void> updateProduct(@Valid @RequestBody ProductUpdateDTO productUpdateDTO, HttpServletRequest request) {  
    String token = request.getHeader("token");  
    Long userId = JwtUtil.getUserIdFromToken(token);  
    return productService.updateProduct(productUpdateDTO, userId);  
}
```

5. 在`Apifox`里面测试参数校验

![[Pasted image 20260503154404.png]]

> 测试依旧返回成功，上面的 JSON 字段可以按需选择性传入，未传字段保持原值，为了测试样本，除了图片外全部修改，数据库中的相应字段也成功被编辑修改，当然这也还有一些异常场景，在测试通过后，说明编辑接口已经完成 DTO 封装和选择性字段更新。

|测试场景|预期结果|
|---|---|
|id 为空|商品ID不能为空|
|修改他人商品|无权操作他人的商品|
|price 为负数|商品价格必须大于0|

---
## 七、缓存与性能优化

### 7.1 项目Redis缓存前置

1. 在 `pom.xml` 中引入`Redis`依赖 

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-redis</artifactId>
</dependency>
```

> 核心作用：提供 Redis 连接和基本操作 API，提供 Redis 操作能力（如 `RedisTemplate`）

2. 在 `application.yml`中配置 Redis 连接


```yml
# =================== Redis 配置 ===================
spring:
  redis:
      host: localhost        # Redis 服务器地址
      port: 6379             # Redis 端口  
      database: 0            # Redis 数据库索引  
      timeout: 5000ms        # 连接超时时间  
      lettuce: 
         pool:  
           max-active: 8      # 最大连接数   
           max-idle: 8        # 最大空闲连接数   
           min-idle: 0        # 最小空闲连接数   
           max-wait: -1ms     # 最大等待时间

```

**作用：** 配置 Spring Boot 连接 Redis 的基本信息，让项目能够访问 Redis 服务。

3. 使用 `StringRedisTemplate` 操作 Redis

本项目当前使用 `StringRedisTemplate` 操作 Redis，原因是分类缓存、浏览量缓存等数据都可以通过字符串或 JSON 字符串保存，使用方式简单直观。

后续如果需要直接缓存 Java 对象，也可以再配置自定义 `RedisTemplate` 序列化方式。

4. Redis Key 命名规范

为了避免不同业务缓存 key 混乱，当前项目采用模块化命名方式：

```text
业务模块:业务对象:具体含义
```

例如：

```text
product:category:list
product:viewCount:{productId}
```

### 7.2 分类缓存

#### 🎯目标

把商品分类列表缓存到 Redis，因为分类数据变化少，但访问频率高。

- 主要实现：先实现查询分类时优先读 Redis

- 核心逻辑：
```text
第一次请求：
Redis 没有 → 查 MySQL → 写入 Redis → 返回

后续请求：
Redis 有 → 直接返回 → 不查 MySQL
```

- 核心代码展示

```Java
String key = "product:category:list";
String cacheJson = stringRedisTemplate.opsForValue().get(key);
if (cacheJson != null && !cacheJson.isEmpty()) {
    List<ProductCategory> categoryList = JSON.parseArray(cacheJson, ProductCategory.class);
    return Result.success(categoryList);
}
LambdaQueryWrapper<ProductCategory> wrapper = new LambdaQueryWrapper<>();
wrapper.eq(ProductCategory::getStatus, 1)
        .orderByAsc(ProductCategory::getSort);
List<ProductCategory> categoryList = productCategoryMapper.selectList(wrapper);

stringRedisTemplate.opsForValue().set(
        key,
        JSON.toJSONString(categoryList),
        1,
        TimeUnit.HOURS
);

return Result.success(categoryList);
```

> 上面只展示需要新添的代码内容，完整代码不过多展示
> 当前使用 `StringRedisTemplate`，因此需要将分类列表转换为 JSON 字符串后再写入 Redis。

- 测试代码展示

#### 阶段一：清空 Redis 中的分类缓存

```bash
PS C:\Users\15> redis-cli
127.0.0.1:6379> select 0
OK
127.0.0.1:6379> del product:category:list
(integer) 1
127.0.0.1:6379> get product:category:list
(nil)
```

> 到这里，Redis 中已经没有 `product:category:list` 缓存。接下来调用分类列表接口，触发“Redis 未命中 → 查询 MySQL → 写入 Redis”的流程。

#### 阶段二：调用分类列表接口

在 Apifox 中调用分类列表接口：

```http
GET /api/product/category/list
```

> 接口正常返回分类列表后，再回到 Redis 终端查看缓存是否生成。

#### 阶段三：查看 Redis 缓存是否生成

```bash
127.0.0.1:6379> keys *
1) "product:category:list"
127.0.0.1:6379> get product:category:list
"[{\"id\":1,\"name\":\"\xe6\x95\xb0\xe7\xa0\x81\",\"sort\":1,\"status\":1},{\"id\":2,\"name\":\"\xe6\x95\x99\xe6\x9d\x90\",\"sort\":2,\"status\":1},{\"id\":3,\"name\":\"\xe7\x94\x9f\xe6\xb4\xbb\",\"sort\":3,\"status\":1},{\"id\":4,\"name\":\"\xe8\xa1\xa3\xe7\x89\xa9\",\"sort\":4,\"status\":1},{\"id\":5,\"name\":\"\xe8\x99\x9a\xe6\x8b\x9f\xe7\x89\xa9\xe5\x93\x81\",\"sort\":5,\"status\":1},{\"id\":6,\"name\":\"\xe5\x85\xb6\xe4\xbb\x96\",\"sort\":6,\"status\":1}]"
```

> 可以看到 `product:category:list` 已经写入 Redis，说明分类缓存逻辑生效。

### 7.3 热门商品

#### 🎯 目标

基于已有的：`Product.viewCount`来实现一个热门商品接口：按浏览量倒序查询前 N 个商品
比如首页展示：热门商品 Top 10

#### 🚀 实现
1. 在`ProductService`里新增方法

```Java
/**  
 * 热门商品展示  
 * @param limit  
 * @return  
 */  
Result<List<ProductListVO>> listHotProducts(Integer limit);
```

2. 在`ProductServiceImpl`里新增实现方法

```Java
/**  
 * 热门商品展示  
 * @param limit  
 * @return  
 */  
@Override  
public Result<List<ProductListVO>> listHotProducts(Integer limit) {  
    // 1. 处理默认条数  
    if (limit == null || limit <= 0) {  
        limit = 10;  
    }  
    if (limit > 20) {
        limit = 20;
    }
    // 2. 查询上架商品，按浏览量倒序  
    LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<>();  
    wrapper.eq(Product::getStatus, 1)  
            .orderByDesc(Product::getViewCount)  
            .orderByDesc(Product::getId)  
            .last("LIMIT " + limit);  
  
    List<Product> productList = productMapper.selectList(wrapper);  
  
    // 3. Entity 转 VO    
    List<ProductListVO> voList = productList.stream()  
            .map(this::convertToVO)  
            .collect(Collectors.toList());  
  
    return Result.success(voList);  
}
```

这个接口做的是：

```text
查询上架商品
按 viewCount 从高到低排序
取前 limit 条
返回 ProductListVO
```

3. 在`ProductController`里面新增接口

```Java
/**  
 * 热门商品展示  
 * @param limit  
 * @return  
 */  
@GetMapping("/hot")  
public Result<List<ProductListVO>> listHotProducts(  
        @RequestParam(required = false) Integer limit) {  
    return productService.listHotProducts(limit);  
}
```

4. 在Apifox里测试并验证`"/hot"`接口

![[Pasted image 20260503165044.png]]

补充测试：

| 测试场景      | 预期结果        |
| --------- | ----------- |
| 不传 limit  | 默认返回 10 条   |
| limit=100 | 最多返回 20 条   |
| 多个商品浏览量相同 | 按 id 倒序兜底排序 |

> 上述返回结果成功按照浏览量倒序返回，热门商品展示接口测试并验证成功

---

## 八、Day06 商品模块补强总结

本篇作为 Day06 商品模块的绿叶篇，主要对龙骨篇中已经跑通的商品主流程进行补强。

当前已完成：

- 商品查询参数封装：`ProductQuery`
- 商品列表分页查询
- 多条件筛选：分类、关键词、价格区间
- 动态排序：价格、时间、浏览量
- 列表 VO 与详情 VO 拆分
- 商品详情浏览量统计
- Redis 浏览量基础缓存
- 发布商品 DTO 与参数校验
- 编辑商品 DTO 与选择性字段更新
- 防重复提交基础处理
- 分类缓存
- 热门商品接口

通过本次补强，商品模块已经从“功能可用”进一步整理为“结构清晰、接口更规范、具备基础缓存能力”的状态。

后续可继续优化：

- 商品图片上传
- 商品详情缓存
- 搜索历史与热门搜索
- 更完整的商品状态流转
- 发布商品前的学生认证校验


## 九、🌿绿叶小结 (ಡωಡ)----2026/05/03

【作者说：本篇🌿历时三天洋洋洒洒三万六千字成功完结 (๑˃̵ᴗ˂̵)و ，这不仅是绿叶的完结也是整个day06商品体系的完结，我想了很久需不需要来总结一下这个笔记内容，第八点已经做了大致的内容总结，我这里单纯是想写点东西(⊙_⊙?)，这篇🌿处于五一假期之间，现在假期已经过半(ಥ_ಥ)，我也仅仅只有这将近五六万字的产出，不敢说把里面的东西全部吸收总结，我学到的内容要是能有一半能用上就已经很好了(￣o￣)，其实做这部分内容我有很多纠结的地方，也有很多懵逼的地方(；一_一)，但也都在试着理解，一切都要慢慢来嘛！写这篇的时候我确实小看了这篇笔记的体量，但是还是那句话，这篇笔记的第一个读者永远都是自己，所以我无所担忧，不用纠结(＾▽＾)👍
    确实还有很多内容需要我慢慢啃，这很重要我在`claudian`和`chatgpt`中反复横跳，也算是理解了七七八八，说是理解完全那倒是没有很多必要，现在的代码ai肯定是比很多代码高手懂的，我们这需要懂这种架构思想，逻辑思维就可以了，只有这样才能做ai的使用者！(｡•̀ᴗ-)✧
至此，我的绿叶篇伴随着我的五一假期完美谢幕，我也是初次体会到了这个模块以及这个项目的庞大知识体系，学习的进程仍然还在起步之中(ง •̀_•́)ง，我也在很多时候都在思考一个可以给给予我动力的方法----完成跟我项目匹配的前端页面，这样我能实时的看到我的产出，能让我顺着成就感继续学习，但这不现实(╥﹏╥)，我没有前端知识储备，即便完善当前进度的页面，我还会在后端进行修改，这极大浪费我的心神和时间，得不偿失还是选择了放弃！(｡･ω･｡) 我越来越期待这个项目的完工，我看到前端项目运作的那一刻，我无法想象我的兴奋 (ﾉ◕ヮ◕)ﾉ*:･ﾟ✧
   收工收工，🌿正式完工！☁️ 🌊 ⛵未完待续，敬请期待day07的内容吧(๑˃̵ᴗ˂̵)و！！！ 】

