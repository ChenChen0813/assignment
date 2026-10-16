import java.util.ArrayList;
import java.util.List;

// 保留主类的 public
public class School {
    // 组合：一个学院由1到多个系构成
    private List<Department> departments = new ArrayList<>();
    // 聚合：学院拥有0到多名学生
    private List<Student> students = new ArrayList<>();
}

// 以下类均去掉 public
class Department {
    // 关联：每个系至少拥有1位指导教师
    private List<Instructor> instructors = new ArrayList<>();
    // 关联：提供至少1门课程
    private List<Subject> subjects = new ArrayList<>();
}

class Instructor {
    // 关联：教授1-3门课程
    private List<Subject> teachingSubjects = new ArrayList<>();
}

class Subject {
    // 课程结构
}

class Student {
    // 关联：学生参加1-5门课程学习
    private List<Subject> enrolledSubjects = new ArrayList<>();
}