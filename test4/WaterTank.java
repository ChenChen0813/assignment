/**
 * B1 恒温水箱状态机实现
 * 对应状态：休眠状态、待机状态、烧水状态、维修状态
 */
public class WaterTank {
    // 状态枚举
    public enum State {
        SLEEP,      // 休眠状态
        STANDBY,    // 待机状态
        HEATING,    // 烧水状态
        MAINTENANCE // 维修状态
    }

    private State currentState;

    public WaterTank() {
        // 初始状态为休眠
        currentState = State.SLEEP;
        System.out.println("初始状态：休眠状态");
    }

    // 事件：早上7点整
    public void onMorning7Clock() {
        if (currentState == State.SLEEP) {
            currentState = State.STANDBY;
            System.out.println("[事件] 早上7点整 → 进入待机状态");
        }
    }

    // 事件：晚上11点整
    public void onNight11Clock() {
        if (currentState == State.STANDBY || currentState == State.HEATING) {
            currentState = State.SLEEP;
            System.out.println("[事件] 晚上11点整 → 进入休眠状态");
        }
    }

    // 事件：温度低于20℃
    public void onTemperatureBelow20(boolean hasWater) {
        if (currentState == State.STANDBY) {
            if (hasWater) {
                currentState = State.HEATING;
                System.out.println("[事件] 温度<20℃且有水 → 打开继电器，进入烧水状态");
            } else {
                System.out.println("[事件] 温度<20℃但无水 → 不开启电源，保持待机");
            }
        }
    }

    // 事件：温度达到100℃
    public void onTemperatureReach100() {
        if (currentState == State.HEATING) {
            currentState = State.STANDBY;
            System.out.println("[事件] 温度达到100℃ → 断开电源，回到待机状态");
        }
    }

    // 事件：检测到水箱无水
    public void onWaterEmpty() {
        if (currentState == State.HEATING) {
            currentState = State.STANDBY;
            System.out.println("[事件] 检测到无水 → 断开电源，回到待机状态");
        }
    }

    // 事件：水箱烧坏
    public void onTankBroken() {
        if (currentState != State.MAINTENANCE) {
            currentState = State.MAINTENANCE;
            System.out.println("[事件] 水箱烧坏 → 进入维修状态");
        }
    }

    // 事件：维修完成
    public void onRepairFinished() {
        if (currentState == State.MAINTENANCE) {
            currentState = State.STANDBY;
            System.out.println("[事件] 维修完成 → 回到待机状态");
        }
    }

    public State getCurrentState() {
        return currentState;
    }

    public static void main(String[] args) {
        WaterTank tank = new WaterTank();
        tank.onMorning7Clock();
        tank.onTemperatureBelow20(true);
        tank.onTemperatureReach100();
        tank.onTemperatureBelow20(false);
        tank.onTankBroken();
        tank.onRepairFinished();
        tank.onNight11Clock();
    }
}
