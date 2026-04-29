
> 日期：2026/04/29----
> 目标：用户能浏览商品列表、查看详情、按条件搜索与筛选过滤

---

## 项目结构
---
## 一、商品模块基础搭建

### 1.1 在 `MySQL` 中创建 `category` 表并初始化分类数据

- 在商品模块正式开发之前，首先需要补充商品分类表 `category`。  
- 这是因为后续商品发布、商品列表筛选和商品详情展示，都会依赖分类数据作为基础支撑，因此分类体系必须优先于商品体系落地。

```sql
-- =============================================  
-- 商品分类表（product_category）  
-- 用于商品分类管理，支持后续扩展（多级分类）  
-- =============================================  
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
INSERT INTO category (name, sort, status)
VALUES
('数码', 1, 1),
('教材', 2, 1),
('生活', 3, 1),
('衣物', 4, 1),
('虚拟物品', 5, 1),
('其他', 6, 1);
```

### 1.2 创建 `Category.java` 实体类




---
## 二、商品浏览实现
---
## 三、商品搜索与筛选
---
## 四、接口测试与链路验证
---
## 五、今日成果总结
---
## 六、下一步任务(day07)
---
## 七、踩坑记录
---
## 八、我说得不多：(◍・ᴗ・◍)(2026/04/29)


