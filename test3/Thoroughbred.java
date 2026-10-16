public class Thoroughbred {
    // 私有属性
    private String mother;
    private String father;
    private int birthyear;

    // 公有方法
    public int getCurrentAge() {
        int currentYear = 2026; 
        return currentYear - birthyear;
    }

    public String getFather() {
        return father;
    }

    public String getMother() {
        return mother;
    }
}