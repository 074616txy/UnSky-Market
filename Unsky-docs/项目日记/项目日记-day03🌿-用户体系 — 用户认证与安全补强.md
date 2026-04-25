
> 🌿日期：2026/04/24-04/25
> 🌿目标：补充用户体系在重复注册校验、密码加密、JWT 认证等方面的完善过程

---

## 一、🌿内容说明

本篇作为 day03 主篇的补充记录，不再重复展开注册与登录主链路本身，而是集中整理用户体系在认证与安全方向上的进一步完善内容。主篇解决的是“链路跑通”的问题，本篇更关注“链路跑通之后，系统如何变得更规范、更安全、更接近真实项目可用状态”。

本篇主要围绕以下三个方面展开：

- 重复注册校验：避免相同手机号或账号被重复写入数据库
- 密码加密：避免密码明文存储，补齐最基础的安全处理
- JWT 认证：在登录成功后引入 Token 机制，为后续接口鉴权与登录态校验做准备

这三个部分虽然代码量未必特别大，但都属于用户体系中非常关键的增强点，因此单独整理为附属篇，便于和主篇形成“主链路跑通 + 关键能力补强”的对应关系。

---

## 二、重复注册校验机制补充

### 2.1 补强原因

在注册主链路已经跑通之后，当前注册流程仍然存在一个明显问题：如果用户使用同一个手机号重复发起注册请求，系统仍然会继续执行插入操作，导致数据库中出现重复用户数据。

因此，这一步补充的重点不是重新实现注册功能，而是在原有注册逻辑的基础上，增加“重复注册校验”这一层前置判断，让注册流程具备最基础的数据约束意识。

---

### 2.2 实现思路

这次补充的实现思路比较直接：

1. 在 `UserServiceImpl` 的 `register(User user)` 方法中，先根据手机号查询数据库  
2. 如果数据库中已经存在相同手机号对应的用户数据，则直接返回错误结果  
3. 如果查询结果为空，说明当前手机号尚未注册，此时再继续执行插入操作  
4. 最后返回注册成功结果

判断逻辑展示：
```Java
/**  
 *用户重复注册的判断（绿叶补强）  
 */  
if(result != null){  
    return Result.error("该手机号已被注册");  
}
```

> 也就是说，注册接口不再是“直接写库”，而是先判断、再决定是否插入。

这一部分真正改动的核心代码并不多，重点在于把注册逻辑的执行顺序调整清楚：原本是“接收请求后直接写库”，现在变成了“先查重，再决定是否允许写库”。后续如果补代码展示，最适合放在这里的就是 `register(User user)` 方法中查询手机号与返回错误结果的那一段判断逻辑。

---

### 2.3 测试结果


补充重复注册校验之后，我使用已经注册过的手机号再次发起注册请求，接口返回失败信息，提示该手机号已被注册。

![[Pasted image 20260425142116.png]]


同时，在这次重复注册测试中，数据库没有新增重复用户数据，说明当前这层校验已经能够起到最基本的拦截作用。

---

### 2.4 阶段评价

这一部分代码量并不大，但意义很明确：它让注册接口从“只要请求到了就写库”，变成了“先判断数据是否合法，再决定是否落库”。

虽然当前还没有继续上升到参数校验、异常分类、并发场景处理等更完整的层面，但对于 day03 当前阶段来说，重复注册校验已经算是把注册流程从“能用”推进到了“基本可靠”。

--- 

## 三、密码加密逻辑完善

### 3.1 补强原因

在注册与登录基础链路已经跑通之后，当前用户密码仍然是以明文形式直接存入数据库。虽然这种写法便于前期测试，但从真实项目开发的角度来看，明文密码存储存在明显的安全问题，也不符合用户认证模块最基础的安全要求。

因此，这一步补充的重点，是先把用户密码从“明文入库”调整为“加密后入库”，并同步改造登录时的密码校验逻辑，为后续 JWT 认证和接口鉴权打下基础。

---

### 3.2 实现思路

这次密码加密的处理主要发生在 `UserServiceImpl` 中，并同时影响注册与登录两个环节。

在注册逻辑中：

1. 先保留原有的重复注册校验  
2. 校验通过后，使用 `BCryptPasswordEncoder` 对用户传入的明文密码进行加密  
3. 将加密后的密文重新写回 `user` 对象  
4. 再执行 `userMapper.insert(user)`，把密文密码写入数据库

在登录逻辑中：

1. 不再继续使用“手机号 + 明文密码”直接查库  
2. 而是先根据手机号查询对应用户  
3. 再使用 `passwordEncoder.matches(明文密码, 数据库密文密码)` 完成密码匹配  
4. 匹配成功则返回登录成功，否则返回错误结果

- 如果没有`BCryptPasswordEncoder`加密工具怎么办？

 1. 我们可以创建这个加密工具，首先需要在实现类创建`BCryptPasswordEncoder`

```Java
//创建一个密码加密工具passwordEncoder，对密码进行加密  
private BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
```

2. 这个时候会产生报错，我们需要在这个后端包的依赖中添加关于这个工具的依赖

```xml
<!-- 密码加密 -->  
    <dependency>  
        <groupId>org.springframework.security</groupId>  
        <artifactId>spring-security-crypto</artifactId>  
    </dependency>
```

3. 在注册中把用户传进来的密码直接加密，再放入user对象里

```Java
/**  
 * 密码加密逻辑（绿叶补强）  
 * 把用户传进来的明文密码加密，再塞回 user 对象里  
 */  
String encodePassword = passwordEncoder.encode(user.getPassword());  
user.setPassword(encodePassword);
```

4. 在登录中进行输入密码与数据库存储的加密密码进行`matches`并判断结果

```Java
//加密密码配对----passwordEncoder.matches(user.getPassword(), result.getPassword())  
if (result != null && passwordEncoder.matches(user.getPassword(), result.getPassword())) {  
    String token = JwtUtil.generateToken(result.getId());//先生成身份认证  
    LoginVO loginVO = new LoginVO();  
    loginVO.setUser(result);  
    loginVO.setToken(token);  
    return Result.success(loginVO);  
} else {  
    return Result.error("用户名或密码错误");  
}
```

- 需要注意：当注册逻辑改为密文存储之后，登录部分原先按明文密码直接查库的旧写法就不能继续使用了，因此需要将这一层查询条件注释掉或删除，再改为基于 `matches` 的密码匹配逻辑。

```Java
//这里要做密码加密的内容，不能按照原明文密码进行查找登录了  
//wrapper.eq("password", user.getPassword());
```

> 也就是说，密码加密这一节并不只是“把注册密码加密一下”，而是同时推动了登录校验逻辑从“明文查库”转向“密文匹配”。

---

### 3.3 测试结果

完成密码加密逻辑之后，我重新使用新的手机号发起注册请求，接口能够正常返回成功结果。

![[Pasted image 20260425212330.png]]

同时，在数据库中查看这次新插入的用户数据后，可以看到 `password` 字段已经不再是原始明文，而是经过加密处理后的密文内容。这说明当前注册流程中的密码加密逻辑已经生效，ฅ՞•ﻌ•՞ฅ 俺们做到啦！！！

在完成注册密码加密之后，登录逻辑也不能继续沿用原先“手机号 + 明文密码直接查库”的方式，而是改为先根据手机号查询用户，再使用密码匹配方法校验明文密码与数据库密文是否一致。经过调整后，新注册的加密账号已经可以正常登录，而旧的明文账号则不再适用于当前这套认证逻辑。

后续为了继续测试旧账号在新逻辑下的适配情况，我又手动将数据库中旧账号的密码更新为 BCrypt 密文。更新完成之后，该旧账号也已经可以重新适配当前登录流程，说明这套“注册加密 + 登录匹配”的逻辑已经能够稳定覆盖新旧测试账号。

---

### 3.4 阶段评价

这一部分的改动虽然不算多，但意义很明确：它把用户注册流程从“可以正常写库”推进到了“具备最基础的安全处理能力”。

更重要的是，这一步并不只是影响注册本身，还连带推动了登录逻辑的底层变化——登录不再依赖“明文密码直接查库”，而是开始基于加密规则进行密码匹配。这意味着用户体系在 day03 的基础版本之上，已经开始真正进入“认证逻辑逐步规范化”的阶段。

---

## 四、JWT 认证链路补强

### 4.1 当前定位说明

这一部分主要对应登录成功后的身份凭证处理。主篇中登录接口已经能够完成基础校验并返回结果，而在本节中，需要进一步补上 Token 生成、返回、携带和校验这条链路，使系统从“登录接口可用”逐步过渡到“登录状态可验证、接口访问可受保护”的阶段。

不过，day03 当前阶段优先完成的还不是整套 JWT 鉴权闭环，而是先把“登录成功后能够生成并返回 Token”这一前半段链路接上。也就是说，本节当前更偏向于完成身份凭证的发放，而不是完整的接口保护，**接下来，我们要给用户发身份证啦(˃̶͈̀௰˂̶͈́)！！！**

---

### 4.2 实现思路

这一部分的改动主要集中在三个层面：

#### （1）引入 JWT 依赖并封装工具类

1. 首先在后端模块`pom`中引入 JWT 相关依赖，在通用模块中新增`JwtUtil`工具类。

```xml
<!-- JWT认证 -->  
<dependency>  
    <groupId>io.jsonwebtoken</groupId>  
    <artifactId>jjwt-api</artifactId>  
    <version>0.11.5</version>  
</dependency>  
  
<dependency>  
    <groupId>io.jsonwebtoken</groupId>  
    <artifactId>jjwt-impl</artifactId>  
    <version>0.11.5</version>  
    <scope>runtime</scope>  
</dependency>  
  
<dependency>  
    <groupId>io.jsonwebtoken</groupId>  
    <artifactId>jjwt-jackson</artifactId>  
    <version>0.11.5</version>  
    <scope>runtime</scope>  
</dependency>
```


- 当前工具类中主要完成了以下内容：

1. 定义签名密钥  
2. 定义 Token 的过期时间  
3. 封装 `generateToken(Long userId)` 方法，根据用户 id 生成带签名与过期时间的 JWT Token
4. 工具类代码展示：

```Java
public class JwtUtil {  
  
    /**  
     * SECRET_KEY：JWT 签名密钥  
     * EXPIRE_TIME：过期时间，这里先写成 1 天  
     */  
    private static final String SECRET_KEY = "unsky-market-secret-key-unsky-market";  
    private static final long EXPIRE_TIME = 1000 * 60 * 60 * 24;  
    /**  
     * 接收 userId  
     * 生成 token  
     * 返回 token 字符串  
     * 根据用户 id，生成一个“带身份信息 + 带过期时间 + 带签名”的 token。  
     * @param userId  
     * @return  
     */  
    public static String generateToken(Long userId){  
    //表示当时时间  
    Date now = new Date();  
    //把用户id包装成一个有签名、带过期时间的token字符串  
    Date expireDate = new Date(now.getTime() + EXPIRE_TIME);  
    return Jwts.builder()  
            .setSubject(String.valueOf(userId))  
            .setIssuedAt(now)  
            .setExpiration(expireDate)  
            .signWith(Keys.hmacShaKeyFor(SECRET_KEY.getBytes()), SignatureAlgorithm.HS256)  
            .compact();  
    }  
}
```
> 这一步的核心作用，是把“生成 Token”这件事从业务逻辑中抽离出来，交给专门的工具类统一处理。后续如果补代码展示，这里最适合放的是 `JwtUtil` 中静态常量与 `generateToken` 方法的核心实现。

#### （2）调整登录接口的返回结构

由于登录成功后需要同时返回用户信息和 Token，因此原本只返回 `User` 对象的方式已经不再适用，**所以只能重新找到一个容器，将User和Token打包起来，我们把这个容器叫做`LoginVO`** ˗ˏˋ ✦ ˎˊ˗

基于这一点，当前新增了 `LoginVO` 作为登录成功后的返回对象，其中主要封装了两部分内容：

- `user`：当前登录成功的用户信息
- `token`：当前用户对应的 JWT 身份凭证

核心代码展示：
```Java
@Data //自动生成需要使用的方法  
public class LoginVO {  
  
    private User user;  
    private String token;  
}
```

**同时，`UserService`、`UserServiceImpl` 和 `UserController` 中登录方法的返回值，也同步从 `Result<User>` 调整为了 `Result<LoginVO>`。**

#### （3）在登录成功分支中生成并返回 Token

当用户手机号存在且密码匹配成功之后，登录逻辑不再直接返回用户对象，而是：

1. 调用 `JwtUtil.generateToken(result.getId())` 生成当前用户的 Token  
2. 创建 `LoginVO` 对象  
3. 将用户信息与 Token 一并封装进 `LoginVO`  
4. 最终通过统一返回结构将其返回给前端

这样一来，登录接口就不只是“判断账号密码是否正确”，而是开始具备“**登录成功后发放身份凭证**”的能力。

---

### 4.3 测试结果

完成这部分接入之后，使用已经适配当前认证逻辑的账号发起登录请求，接口已经能够正常返回成功结果。

![[Pasted image 20260425213620.png]]

同时，返回数据中除了用户信息以外，也已经额外包含了 JWT Token，说明当前登录链路中的 Token 生成与返回流程已经跑通。也就是说，当前用户模块已经能够在登录成功后，正式向前端返回一份可继续用于身份认证的凭证。

---

### 4.4 当前阶段说明

需要说明的是，当前 JWT 部分完成的范围主要还是“生成并返回 Token”。

也就是说，day03 目前已经完成了登录成功后的凭证发放，但还没有继续扩展到以下内容：

- Token 解析与校验
- 登录拦截器接入
- 注册/登录接口放行配置
- 受保护接口的访问验证

因此，这一节当前更准确的定位，是“JWT 认证链路的前半段接入完成”，后续还需要继续围绕鉴权与接口保护进行补充。

> 细节的朋友可能会注意到，上面测试返回的图片中仍然带有`password`加密后的密码。虽然此时返回的已经不是明文密码，但从接口设计角度来看，后续仍应继续做返回数据脱敏处理，避免将密码相关字段继续暴露给前端。

---

## 五、阶段小结🌿

通过本篇几个补充点的完善，day03 的用户体系不再只是停留在“注册 + 登录基础可用”的状态，而是开始向“**具备基础安全意识与认证能力**”的方向推进。虽然距离完整的正式业务体系仍有差距，但重复注册校验、密码加密和 JWT 认证这三部分的加入，已经让用户模块从单纯跑通链路，进一步迈向了更规范的工程化实现。

> ☁️(˘͈ᵕ˘͈) 🌊 ⛵  至此，这篇day03的附属小词也算是迎来它的终章！˚ ༘♡ ⋆｡˚但在这篇也给后续的日记埋下了伏笔，我将会慢慢展开，你将会满满探索(˶˃ ᵕ ˂˵)，我们的学习旅程仍未结束，我们仍在前行！(๑˃̵ᴗ˂̵)و！！！



