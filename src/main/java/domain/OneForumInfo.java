package domain;

/**
 * @program: tiebatool
 * @description: 【关注的一个贴吧】所包含的信息
 * @author: diaolizhi
 * @create: 2018-12-24 11:20
 **/
public class OneForumInfo {

    private String id;
    private String name;
    private String levelId;
    private String curScore;
    private String levelupScore;

    public OneForumInfo(String id, String name, String levelId, String curScore, String levelupScore) {
        this.id = id;
        this.name = name;
        this.levelId = levelId;
        this.curScore = curScore;
        this.levelupScore = levelupScore;
    }


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLevelId() {
        return levelId;
    }

    public void setLevelId(String levelId) {
        this.levelId = levelId;
    }

    public String getCurScore() {
        return curScore;
    }

    public void setCurScore(String curScore) {
        this.curScore = curScore;
    }

    public String getLevelupScore() {
        return levelupScore;
    }

    public void setLevelupScore(String levelupScore) {
        this.levelupScore = levelupScore;
    }
}
