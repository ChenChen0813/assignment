import java.util.ArrayList;
import java.util.List;

// 这个类的名字和文件名 College.java 一致，所以保留 public
public class College {
    private String name;
    
    // 聚合关系：学院包含建筑
    private List<Building> buildings = new ArrayList<>();
    
    // 组合关系：学院包含课程
    private List<Course> courses = new ArrayList<>();

    public void addBuilding(Building building) {
        this.buildings.add(building);
    }

    public void addCourse(Course course) {
        this.courses.add(course);
    }
}

// 下面的类去掉 public，保证一个文件只有一个 public 类
class Building {
    private String name;
    private String address;

    public String getName() {
        return name;
    }
}

class Course {
    private String code;
    private String title;
    
    // 约束：Course一定在某个Building中进行
    private Building building; 

    public Building getBuilding() {
        return building;
    }
} 
