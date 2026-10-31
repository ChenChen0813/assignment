/**
 * B2 SafeHome控制面板状态机实现
 * 对应状态：空闲等待、读取键入字符、锁定、系统功能选择
 */
public class ControlPanel {
    // 状态枚举
    public enum State {
        IDLE,       // 空闲等待状态
        INPUT,      // 读取键入字符状态
        LOCKED,     // 锁定状态
        FUNCTION    // 系统功能选择状态
    }

    private final String correctPassword = "1234"; // 预设4位正确密码
    private State currentState;
    private StringBuilder inputBuffer;
    private int errorCount;
    private long lockStartTime;
    private static final int MAX_ERROR_TIMES = 3;
    private static final long LOCK_DURATION_MS = 120 * 1000L; // 锁定120秒

    public ControlPanel() {
        currentState = State.IDLE;
        inputBuffer = new StringBuilder();
        errorCount = 0;
        System.out.println("初始状态：空闲等待状态");
    }

    // 按键输入
    public void pressKey(char key) {
        // 先检查是否满足自动解锁条件
        checkAutoUnlock();

        if (currentState == State.LOCKED) {
            System.out.println("[提示] 面板已锁定，暂时无法输入");
            return;
        }

        if (currentState == State.IDLE) {
            currentState = State.INPUT;
            inputBuffer.setLength(0);
            System.out.println("[事件] 开始输入密码 → 进入读取键入字符状态");
        }

        if (currentState == State.INPUT) {
            inputBuffer.append(key);
            System.out.println("输入字符: " + key + "，当前输入: " + inputBuffer);

            // 输入满4位，执行密码校验
            if (inputBuffer.length() == 4) {
                verifyPassword();
            }
        }
    }

    // 密码校验
    private void verifyPassword() {
        if (inputBuffer.toString().equals(correctPassword)) {
            currentState = State.FUNCTION;
            errorCount = 0;
            System.out.println("[结果] 密码正确 → 进入系统功能选择状态");
        } else {
            errorCount++;
            if (errorCount >= MAX_ERROR_TIMES) {
                currentState = State.LOCKED;
                lockStartTime = System.currentTimeMillis();
                System.out.println("[结果] 密码连续错误" + MAX_ERROR_TIMES + "次 → 锁定控制面板");
            } else {
                inputBuffer.setLength(0);
                System.out.println("[结果] 密码错误，剩余尝试次数: " + (MAX_ERROR_TIMES - errorCount));
            }
        }
    }

    // 自动解锁检测
    private void checkAutoUnlock() {
        if (currentState == State.LOCKED) {
            long passTime = System.currentTimeMillis() - lockStartTime;
            if (passTime >= LOCK_DURATION_MS) {
                currentState = State.IDLE;
                errorCount = 0;
                inputBuffer.setLength(0);
                System.out.println("[事件] 锁定时长超过120秒 → 自动解锁，回到空闲状态");
            }
        }
    }

    // 退出系统功能
    public void exitFunction() {
        if (currentState == State.FUNCTION) {
            currentState = State.IDLE;
            inputBuffer.setLength(0);
            System.out.println("[事件] 退出功能菜单 → 回到空闲等待状态");
        }
    }

    // 模拟时间流逝，用于测试解锁逻辑
    public void simulatePassSeconds(int seconds) {
        if (currentState == State.LOCKED) {
            lockStartTime -= seconds * 1000L;
            System.out.println("模拟时间流逝 " + seconds + " 秒");
            checkAutoUnlock();
        }
    }

    public static void main(String[] args) {
        ControlPanel panel = new ControlPanel();
        // 连续输错2次
        panel.pressKey('1');panel.pressKey('2');panel.pressKey('3');panel.pressKey('5');
        panel.pressKey('1');panel.pressKey('2');panel.pressKey('3');panel.pressKey('6');
        // 第3次错误，触发锁定
        panel.pressKey('1');panel.pressKey('2');panel.pressKey('3');panel.pressKey('7');
        // 锁定状态下输入无效
        panel.pressKey('1');
        // 模拟120秒后自动解锁
        panel.simulatePassSeconds(120);
        // 输入正确密码
        panel.pressKey('1');panel.pressKey('2');panel.pressKey('3');panel.pressKey('4');
        // 退出功能
        panel.exitFunction();
    }
}
