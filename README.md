# TiebaTools
贴吧工具集

2018年12月22日 创建项目

2019年 3月 4日 查回帖失效


# 示例

## 登录获取用户名和 id
```java
@Test
void testLogin() {
    TBUser tbUser = new TBUser(BDUSSD);
    System.out.println(tbUser.getUid() + tbUser.getUserName());
}
```



## 登录获取关注列表并签到

```java
@Test
void testSign() {
    TBUser tbUser = new TBUser(BDUSSNoName);
    tbUser.updateForumList();
    tbUser.Sign();
}
```



## 发帖

```java
void testPostAdd() { 
    TBUser user = new TBUser(BDUSS);
    user.reply("回复内容", "帖子所在贴吧", "帖子地址");
}
```



## 查看别人关注的贴吧

```java
@Test
void testSeeTie() {
    UserForumsInfo otherForumList = client.getOtherForumList("用户名", "1（查看第几页）");
    System.out.println(new Gson().toJson(otherForumList));
}
```



## 获取某个吧首页的帖子

```java
@Test
void intoAForum() {
    ThreadRecord[] records = client.intoAForum("bug", "1");
    System.out.println(new Gson().toJson(records));
}
```



## 抢二楼

```java
@Test
void reply() {

    while (true) {
        test("抗压");
    }
}

private void test(String name) {
    ThreadRecord[] records = client.intoAForum(name, "1");

    for (int i=0; i<records.length; i++) {
        if (records[i].getReplyNum() == 0) {

            String tlRes = Tuling.send(records[i].getTitle());
            String out = new JsonParser().parse(tlRes).getAsJsonObject().get("text").getAsString();

//                只回复性别是女的
            if (records[i].getSex() == 2) {
                client.postAdd(BDUSS, out, name, records[i].getTid());
                System.out.println("回复：" + records[i].getTitle());
            }
            return;
        }

    }
}
```



## 查看某个帖子(对返回结果还没有处理)

```java
@Test
void seeAThread() {
    String res = client.seeAThread("5983121478", "123305989467");
    System.out.println(res);
}
```



## ~~查看别人的回帖记录~~（已失效）

```java
@Test
void seePost(){
    HttpClient client = new HttpClient();
    ReplyRecord[] records = client.getUserPost("","一帮坑玩LOL", "1");
    System.out.println(new Gson().toJson(records));
}
```



## 关注或取关一个贴吧

```java
@Test
void testLikeForum() {
//        1 是关注; 否则是取关
    client.likeOrUnfavoForum(BDUSS, "bug", 1);
}
```



## 克隆别人关注的贴吧

```java
@Test
void testClone() {
    TBUser user = new TBUser(BDUSS);
    user.cloneForums("弄死楼主");
}
```



## 收集某个帖子的某层楼中某个人的回复

```java
@Test
void test() {

    ArrayList<String> fuckStrs = new ArrayList<>();

    boolean flag = true;
    int pn = 1;
    while (flag) {
        String res = client.seeAFloor("5987855969", "123374470750", String.valueOf(pn), 1);

        ArrayList<String> supPosts = JsonUtils.subPostsParser(res, "学医无法救主C");
        if (supPosts != null) {

            for (int i=0; i<supPosts.size(); i++) {
                if (!fuckStrs.contains(supPosts.get(i))) {
                    fuckStrs.add(supPosts.get(i));
                }
            }

            pn++;
        } else {
            flag = false;
        }
    }

    System.out.println(new Gson().toJson(fuckStrs));
    System.out.println(fuckStrs.size());
}
```

