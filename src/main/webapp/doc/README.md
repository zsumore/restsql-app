# RESTFUL数据共享平台-----使用说明
VERSION:1.0 修改时间：2014-06-24

### 简介
> 本应用是基于HTTP协议的轻量级的数据访问层，支持MySQL和PostgreSQL，只实现HTTP **GET**方法，本文假设读者已具备基本SQL知识。

###### 特性
1. 结合Java和HTTP API技术的一个SQL生成器
2. 使用一个简单的RESTFUL HTTP API 把数据库数据序列化生成json、xml和csv
3. 支持关系数据库表的一对一，一对多和多对多关系
4. 灵活、简单，只需会SQL即可发布自定义数据接口

###### 使用
自己定义一个SQL资源（SQL Resource）xml文件，放到相应文件夹，即可通过HTTP接口访问使用；本应用是通过登录ftp添加资源定义xml，发布接口，ftp地址：10.151.96.18，用户密码：restsql:restsql

--------------------------------------------------------------------------------------------------

### 一分钟教程
1.把下面内容复制到文本编辑器，保存为任意名称的xml文件（最好是英文名），如：sample.xml

~~~
<?xml version="1.0" encoding="UTF-8"?>
<rs:sqlResource xmlns:rs="http://restsql.org/schema">
<query>
<![CDATA[
SELECT station_id, datetime, r8
  FROM v_aws_day
]]>
</query>
<metadata>
<database default="meteo"/>
<table name="v_aws_day" role="Parent"/>
</metadata>
</rs:sqlResource>
~~~

2.然后通过ftp客户端，把文件sample.xml上传到根目录

3.最后在浏览器输入以下地址,看看结果：http://10.151.96.18/restsql/rest/res/sample?_filter=station_id=%27G2213%27+and+datetime+between+2014-04-01T00:00:00+and+2014-04-30T00:00:00

也可以试试：http://10.151.96.18/restsql/rest/res/sample?_filter=station_id=%27G2213%27+and+datetime+between+2014-04-01T00:00:00+and+2014-04-30T00:00:00&_output=csv

The End.

了解更多，请往下看

--------------------------------------------------------------------------------------------------------

### SQL资源:（SQL Resource）
每一个SQL资源就是一个数据接口，下面请看一个SQL资源定义例子。

例子：**mll.nh_sum_r8**,Resource xml中不要包含虚线部分
~~~
<?xml version="1.0" encoding="UTF-8"?>
<rs:sqlResource xmlns:rs="http://restsql.org/schema"> ------------------------------------------1:sqlResource
<description>-----------------------------------------------------------------------------------2:description
<![CDATA[
查询南海区日雨量统计数据，用于ncl画图。
自动站点范围：(s.longitude between 112.5 and 113.5) ，(s.latitude between 22.5 and 23.5 )
---------------------------------------------------------------------------------
注意：
含有group by的例子最好使用<test></test>增加查询条件，缩小查询范围，从而减少元数据查询时间,
本范例在<test></test>中增加(datetime between '2014-05-01' and '2014-05-31')条件，用于减少查询范围
--------------------------------------------------------------------------------
使用范例：
[1.查询2014年4月合雨量]:http://10.151.96.18/restsql/rest/res/pretty/mll.nh_sum_r8?_filter=datetime+DURING+2014-04-01T00:00:00Z/2014-04-30T00:00:00Z&_output=csv
]]>
</description>
<test>------------------------------------------------------------------------------------------3:test
<![CDATA[
SELECT  station_id sid, s.name sn, s.longitude lon, s.latitude lat, sum(r8) sum_r8 ,count(r8) count_r8 FROM 
v_aws_day left join dict.v_station_cn s on s.id=station_id
	WHERE (s.longitude between 112.5 and 113.5) 
	AND (s.latitude between 22.5 and 23.5 )
	AND r8 is not null
	AND (datetime between '2014-04-01' and '2014-04-30')
And s.type = 'A'
And s.id not in ('G2186','G3152','G3256','G3157','G3249','G1088')
group by sid,sn,lon,lat
]]>
</test>
<query>------------------------------------------------------------------------------------------4:query
<![CDATA[
SELECT  station_id sid, s.name sn, s.longitude lon, s.latitude lat, sum(r8) sum_r8 ,count(r8) count_r8 FROM 
        v_aws_day left join dict.v_station_cn s on s.id=station_id
WHERE (s.longitude between 112.5 and 113.5) 
	AND (s.latitude between 22.5 and 23.5 )
	AND r8 is not null
    AND s.type = 'A'
    AND s.id not in ('G2186','G3152','G3256','G3157','G3249','G1088')
group by sid,sn,lon,lat
]]>
</query>
<metadata>---------------------------------------------------------------------------------------5:metadata
<database default="meteo"/>----------------------------------------------------------------------6:database
<table name="v_aws_day" role="Parent"/>----------------------------------------------------------7:table
<table name="dict.v_station_cn" role="ParentExtension" />
</metadata>
<validatedAttribute name="lon" type="Numeric"  format="0.0000" />------------------------8:validateAttribute
<validatedAttribute name="lat" type="Numeric"  format="0.0000" />
<validatedAttribute name="sum_r8" type="Numeric"  format="0.0" />
</rs:sqlResource>
~~~

###### 说明
>	1:**sqlResource**  XML Root Element 格式固定
~~~
<rs:sqlResource xmlns:rs="http://restsql.org/schema"> 
~~~
>	2:**description**  非必需 出现次数：0或1;	用于说明本资源如何使用

>	3:**test**	非必需 出现次数：0或1;	用于生成元数据，当**test**存在，使用**test**生成元数据，否则使用**query**;<br />
>		**元数据SQL** = test 或 query的值 + "Limit 1 Offset 0" ;<br />
>		**元数据** 为上述SQL执行结果的列的元数据：metadata；<br />
>		当SQL语句包含**group by**时最好使用**test**增加查询条件，缩小查询范围，从而减少元数据查询时间

>	4:**query** 必需 出现次数：1; 	结构必需与**test**一致，只是相对减少部分过滤条件;<br />
>		SQL语句构造使用**query**生成
~~~
<query>
<![CDATA[
SELECT  station_id sid, s.name sn, s.longitude lon, s.latitude lat, sum(r8) sum_r8 ,count(r8) count_r8 FROM 
        v_aws_day left join dict.v_station_cn s on s.id=station_id
WHERE (s.longitude between 112.5 and 113.5) 
	AND (s.latitude between 22.5 and 23.5 )
	AND r8 is not null
    AND s.type = 'A'
    AND s.id not in ('G2186','G3152','G3256','G3157','G3249','G1088')
group by sid,sn,lon,lat
]]>
</query>
~~~

>	5:**metadata**	必需 出现次数：1;   数据表的元数据

>	6:**database**	必需 出现次数：1; 	数据库名称

>	7:**table**	必需 出现次数：1或多次; 	数据库表<br />
>		**name** 表名；**role**可为:**Parent,ParentExtension,Child,ChildExtension,Join**<br />
>		**role**=**Parent**的表必需存在;**Parent,ParentExtension**为1对1关系;**Parent,Child**为1对多关系;**Join**用于多对多
~~~
<metadata>
	<database default="meteo"/>
	<table name="v_aws_day" role="Parent"/>
	<table name="dict.v_station_cn" role="ParentExtension" />
</metadata>
~~~

>	8:**validateAttribute** 非必需 出现次数：0或1或多次; 	用于格式化数据输出<br />
>		**name**对应列名；**type**可为：**Numeric,String,Datetime**<br />
>		**format** pattern;日期数据datetime格式化后类型为String，非格式化日期数据类型为Long;更多请查看:<br />
>		[java.text.DecimalFormat](http://download.oracle.com/technetwork/java/javase/6/docs/zh/api/java/text/DecimalFormat.html)
>		[java.text.SimpleDateFormat](http://download.oracle.com/technetwork/java/javase/6/docs/zh/api/java/text/SimpleDateFormat.html)
~~~
<validatedAttribute name="lon" type="Numeric"  format="0.0000" />
<validatedAttribute name="lat" type="Numeric"  format="0.0000" />
<validatedAttribute name="sum_r8" type="Numeric"  format="0.0" />
~~~

###### 定义资源Resource备注
>	**description,test,query,metadata,validateAttribute**出现的次序是固定的<br />
>		**query**中的SQL关键字**WHERE**和**GROUP BY**必需全为大写或全为小写，如**Where**,**Group By**为错误写法，可能会导致不可知错误<br />
>		**test**和**query**中不要包含SQL关键字**ORDER BY**,**LIMIT**,**OFFSET** <br />
>		定义资源Resource更详细资料可查看：[SqlResource Schema](SqlResource.xsd),这需要一点xml的知识

---------------------------------------------------------------------------------------------------------------------

### 表角色:Table Role

如果你的表结构很简单，本段内容可以略过。

在定义查询中的每个表必需在元数据**metadata**中声明其角色**Role**，不同的表角色**Role**其数据请求处理是有区别的。下图描述了一个命名为**ActorFilm**的多对多**many-to-many**结构的表的物理数据模型：<br />
<img src="/restsql/doc/img/film_actor.png"></img>
<img src="/restsql/doc/img/table_role.png"></img>

1. 对于一个扁平结构的资源SQL Resource，一个**Parent**角色表必须声明；
2. 对于一个一对多 one-to-many 的分级资源SQL Resource，一个**Parent**角色表,一个**Child**角色表都必须声明；
3. 对于一个多对多 many-to-many 的分级资源SQL Resource，一个**Parent**角色表,一个**Join**角色表,一个**Child**角色表都必须声明；
4. **Parent, Child and Join**角色表只能定义一次，**ParentExtensions and ChildExtensions**角色表不是必需的，其定义可以为多个

###### 资源定义 SQL Resource Definition

这是一个命名为FilmRating的1对1结构资源，文件名为FilmRating.xml
~~~
<?xml version="1.0" encoding="UTF-8"?>
<rs:sqlResource xmlns:rs="http://restsql.org/schema">
   <query>
      select film.film_id, title, release_year,language_id, 
      		 rental_duration,rental_rate,replacement_cost, film_rating_id, stars
      from film, film_rating
      where film.film_id = film_rating.film_id
   </query>
   <metadata>
      <database default="sakila" />
      <table name="film" role="Parent" />
      <table name="film_rating" role="ParentExtension" />
   </metadata>
</rs:sqlResource>
~~~

这是一个命名为LanguageFilm的1对多结构资源，文件名为LanguageFilm.xml
~~~
<?xml version="1.0" encoding="UTF-8"?>
<rs:sqlResource xmlns:rs="http://restsql.org/schema">
   <query>
      select language.language_id, language.name, film_id, title, release_year
      from language
      left outer join film on film.language_id = language.language_id
   </query>
   <metadata>
      <database default="sakila" />
      <table name="language" role="Parent" />
      <table name="film" role="Child" />
   </metadata>
</rs:sqlResource>
~~~

这是一个命名为ActorFilm的多对多结构资源，文件名为ActorFilm.xml
~~~
<?xml version="1.0" encoding="UTF-8"?>
<rs:sqlResource xmlns:rs="http://restsql.org/schema">
   <query>
      select actor.actor_id, first_name, last_name, actor_rating.stars,
             film.film_id, title, release_year, film_rating.stars
      from actor
      left outer join actor_rating on actor.actor_id = actor_film.actor_id
      left outer join film_actor on film_actor.actor_id = actor.actor_id
      left outer join film on film_actor.film_id = film.film_id
      left outer join film_rating on film.film_id = film_rating.film_id
   </query>
   <metadata>
      <database default="sakila" />
      <table name="actor" role="Parent" />
      <table name="actor_rating" role="ParentExtension" />
      <table name="film" role="Child" />
      <table name="film_rating" role="ChildExtension" />
      <table name="film_actor" role="Join" />
   </metadata>
</rs:sqlResource>
~~~

---------------------------------------------------------------------------------------------------------------

### 接口 HTTP API

**这里列出的所有接口都是相对路径，须加上应用部署地址才是完成路径**
**本应用的部署地址：http://10.151.96.18/restsql/**

HTTP接口 | 类别 | 备注 | 参数 
----|------|----|------
rest/res/{resName} | 资源SQL Resource | 用于查询数据，默认输出，数据体积小 | 可带参数
rest/res/pretty/{resName} | 资源SQL Resource  | 用于查询数据，格式化输出，便于查看 | 可带参数
rest/res/jsonp/{resName} | 资源SQL Resource  | 用于查询数据，jsonp格式输出 | 可带字符串参数 callback
rest/res/file/{resName} | 资源SQL Resource | 用于获取资源定义xml文件 | 无参数
rest/res/metadata/{resName} | 元数据 Metadata | 用于获取**select**的列的元数据定义xml文件 | 无参数
rest/res/clean/resource | 功能性接口 | 用于清除Resource缓存 | 无参数
rest/res/clean/result | 功能性接口 | 用于清除查询结果缓存 | 无参数
rest/res/load/resourceTree | 功能性接口 | 用于查询资源定义树列表 | 无参数

###### 资源SQL Resource HTTP接口
接口：
* **rest/res/{resName}**
* **rest/res/pretty/{resName}**

说明：
>	1.查询资源数据使用HTTP GET方法按资源名称访问

>	2.**{resName}**：资源名称，其命名方式与资源xml文件存放根目录的相对路径有关，与java的类Class命名相类似，如,设定资源根目录为:D:\res,
>	那么资源定义D:\res\a.xml 的**{resName}**=a;资源定义D:\res\mll\b.xml 的**{resName}**=mll.b，以此类推。

>	3.修改和增加资源接口方法：1.通过在线编辑器；2.通过ftp在相应地方覆盖或添加资源定义xml，ftp地址：10.151.96.18，用户密码：restsql:restsql

>	4.参数在后面详细介绍

###### 功能性接口
**rest/res/clean/resource**

用于清除应用资源Resource缓存，修改资源定义xml文件后须在HTTP客户端（浏览器）调用该接口，也就是在地址栏填入：

http://10.151.96.18/restsql/rest/res/clean/resource

然后回车；或者调用本应用提供的客户端的重载功能，原理一样。

**rest/res/clean/result**

本应用对每次查询的结果集进行缓存1分钟（可自定义），也就是说对于两个一模一样的查询在一分钟内查询结果一样，想立即清空结果集缓存须调用本接口：

http://10.151.96.18/restsql/rest/res/clean/result

注意：调用**rest/res/clean/resource**接口时，会自动调用**rest/res/clean/result**接口

---------------------------------------------------------------------------------------------

### 接口参数
接口**rest/res/{resName}**和**rest/res/pretty/{resName}**，可携带以下参数：

参数名称 | 说明
---------|------------------------------------ 
_filter  | 过滤条件,用于查询数据
_orderby | SQL 排序
_limit   | SQL LIMIT 限定查询结果集的最大数量
_offset  | SQL OFFSET 偏移
_output  | 输出查询结果的文件类型，目前支持三种输出类型：json,xml,csv
callback | jsonp 回调函数名，值为字符串，默认为"success"

###### 参数：**_limit**
本应用配置了2个关于**_limit**的系统参数：

~~~
# 如果查询不设参数_limit,或者_limit<=0, SQL LIMIT=response.sql.limit=1000
response.sql.limit=1000

# 如果查询参数_limit>response.sql.maxLimit, SQL LIMIT=response.sql.maxLimit=5000
response.sql.maxLimit=5000
~~~

###### 参数：**_orderby**

~~~
参数：_orderby用于构造 SQL ORDER BY 子句 使用+（加号）分开排序字段和（DESC或ASC）；使用,（逗号）分开多个不同排序
例子：
_orderby=a+ASC    ORDER BY a ASC
_orderby=a,b+DESC  ORDER BY a,b DESC
_orderby=a+DESC,b+ASC  ORDER BY a DESC,b ASC
_orderby=a,b+DESC,c+ASC  ORDER BY a,b DESC, c ASC

注意必须包含每个排序必须包含（DESC或ASC）
_orderby的值不要包含空格
~~~

###### 参数：**_filter**
本应用使用HTTP URL作为调用接口，因此须注意HTML URL 编码问题，详情请查看：
[w3school HTML URL编码说明](http://www.w3school.com.cn/tags/html_ref_urlencode.html)

~~~
这里提一下几个常用转义符:
空格：本应用" "空格可以用%20代替，觉得%20麻烦难看，就用+号;
% （百分号）: % 用 %25 代替（必须）
<（小于号） : < 用 %3c 代替
>（大于号） : > 用 %3e 代替
' （单引号）: ' 用 %27 代替
" （双引号）: " 用 %22 代替
其他字符不用转义也没问题。
~~~

本应用参数**_filter**使用ECQL语言来描述,其定义请查看：[ECQL Reference](http://docs.geoserver.org/latest/en/user/filter/ecql_reference.html)

---------------------------------------------------------------------------------------------

### ECQL使用说明
###### **Condition** 条件

一个**filter condition**（过滤条件）通常是单个断言表达式，或者多个断言表达式的逻辑组合。

**Syntax**                   | **Description**                                    | 描述
---------------------------- | ------------------------------------------------   | ----------------------------------------------------
Predicate                    | Single predicate expression                        | 单个断言表达式
Condition AND , OR Condition | Conjunction or disjunction of conditions           | 使用（AND）或（OR）组合多个断言表达式（conditions）
NOT Condition                | Negation of a condition                            | 否定条件
\(   \[ Condition \]   \)    | Bracketing with \( or \[ controls evaluation order | 使用 \( \) 或者 \[ \] 来控制计算顺序

###### Predicate 断言表达式
Predicate（断言表达式）是指查询字段与某些值的布尔关系表达式

~~~
Expression = | <> | < | <= | > | >= Expression              比较操作（COMPARISON）
Expression [ NOT ] BETWEEN Expression AND Expression        判别查询字段是否在范围之内（之外)（BETWEEN）
Expression [ NOT ] LIKE | ILIKE like-pattern                简单的模式匹配，通常使用％字符作为通配符匹配任意数量的字符
                                                            ILIKE不区分大小写的匹配（LIKE）
Expression [ NOT ] IN ( Expression { ,Expression } )        判别查询字段是否在的一组值之中（IN）
Expression IS [ NOT ] NULL                                  判别查询字段是否为空（IS NULL）
~~~

例子：查询自动站的站号、时间、日最高气温、日最大3秒风、8-8雨量

~~~
<query>
<![CDATA[
SELECT station_id, datetime,  tmax, wf3smax, r8
  FROM v_aws_day
]]>
</query>
~~~

~~~
Predicate

（COMPARISON）: _filter=r8%3e=50                      查询所有8-8雨量大于等于50毫米的自动站日记录
   （BETWEEN）: _filter=r8+BETWEEN+50+AND+100         查询所有8-8雨量在50～100毫米之间的自动站日记录
   （BETWEEN）: _filter=r8+NOT+BETWEEN+50+AND+100     查询所有8-8雨量在50～100毫米区间之外的自动站日记录
      （LIKE）: _filter=station_id+LIKE+%27G22%25%27  查询(station_id like 'G22%')的自动站日记录
        （in）: _filter=station_id+IN+(%27G2213%27,%27G2218%27) 查询(station_id in ('G2213','G2218')的自动站日记录
   （IS NULL）: _filter=r8+IS+NOT+NULL                查询(r8 is not null)的自动站日记录
~~~

~~~
Condition AND , OR Condition

（AND）: _filter=r8%3e=50+and+wf3smax%3e=17.2   查询所有8-8雨量大于等于50毫米且日最大阵风大于等于8级的自动站日记录
 （OR）: _filter=(r8%3e=50)+and+(r8%3c=10)      查询所有8-8雨量大于等于50毫米或8-8雨量小于等于10毫米的自动站日记录
~~~

~~~
Temporal Predicate
时间的表达式，日期时间类型除了可用BETWEEN，>=,<=,=,>,<这些操作符外，还可以用以下特定操作符
Expression BEFORE Time                   判定时间值是否在某个时间点之前（BEFORE）
Expression BEFORE OR DURING Time Period  判定时间值是否在某个时间段之内或时间段之前（BEFORE OR DURING）
Expression DURING Time Period            判定时间值是否在某个时间段之内（DURING）
Expression DURING OR AFTER Time Period   判定时间值是否在某个时间段之内或时间段之前（DURING OR AFTER）
Expression AFTER Time                    判定时间值是否在某个时间点之后（AFTER）

Time（时间）
时间的格式固定为（必需精确到秒）：yyyy-mm-hhThh:mm:ss 
例子：2014年1月1日 2014-01-01T00:00:00  
如果在时间后面加字符Z  2014-01-01T00:00:00Z  则表示为世界时，不加Z则表示服务器时区，也就是北京时  


Time Period（时间段）
用/把2个时间（Time）点连起来表示时间段
例子：2014年1月1日～2014年1月7日 2014-01-01T00:00:00/2014-01-07T00:00:00

_filter=datetime+during+2014-01-01T00:00:00Z/2014-01-07T00:00:00Z
等价于
_filter=datetime+between+2014-01-01T00:00:00Z+and+2014-01-07T00:00:00Z
~~~

###### 备注
1. ECQL的关键字最好全为大写或全为小写，譬如：BETWEEN也可以写作between
2. 日期类型不须单引号包括（用也没问题）:_filter=datetime=2014-01-01T00:00:00，字符串类必需用单引号包括:_filter=station_id=%27G2213%27
3. [ECQL Reference提到的操作符](http://docs.geoserver.org/latest/en/user/filter/ecql_reference.html)，而本文没提到的可能无效，譬如**Spatial Predicate**
4. id字段不能用like
5. 由于气象数据与时间密切相关，因此针对PostgreSql数据库实现了date_part\(text, timestamp\) date_trunc\(text, timestamp\)和to_char\(timestamp, text\)方法，可用于_filter;具体用法请参照[functions-datetime](http://www.postgresql.org/docs/9.3/static/functions-datetime.html)和[functions-formatting](http://www.postgresql.org/docs/9.3/static/functions-formatting.html)
