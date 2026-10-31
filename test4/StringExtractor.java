import java.util.ArrayList;
import java.util.List;

/**
 * B3 源代码字符串提取状态机实现
 * 对应状态：普通文本、字符串内、转义
 */
public class StringExtractor {
    // 状态枚举
    private enum State {
        S0_NORMAL,  // 普通文本状态
        S1_IN_STR,  // 字符串内状态
        S2_ESCAPE   // 转义状态
    }

    public static List<String> extractStrings(String source) {
        List<String> resultList = new ArrayList<>();
        State state = State.S0_NORMAL;
        StringBuilder currentStr = new StringBuilder();

        for (char c : source.toCharArray()) {
            switch (state) {
                case S0_NORMAL:
                    // 遇到起始双引号，进入字符串解析
                    if (c == '\"') {
                        state = State.S1_IN_STR;
                        currentStr.setLength(0);
                    }
                    break;

                case S1_IN_STR:
                    if (c == '\\') {
                        // 遇到反斜杠，进入转义状态
                        state = State.S2_ESCAPE;
                    } else if (c == '\"') {
                        // 遇到未转义双引号，字符串结束
                        resultList.add(currentStr.toString());
                        state = State.S0_NORMAL;
                    } else {
                        // 普通字符直接追加
                        currentStr.append(c);
                    }
                    break;

                case S2_ESCAPE:
                    // 转义字符原样存入，回到字符串内状态
                    currentStr.append(c);
                    state = State.S1_IN_STR;
                    break;
            }
        }
        return resultList;
    }

    public static void main(String[] args) {
        // 题目官方样例测试
        String testInput = "\"一般学习一门编程语言的时候，基本上第一个程序是输出 \\\"Hello World!\\\"\"";
        List<String> result = extractStrings(testInput);

        System.out.println("===== 字符串提取结果 =====");
        for (String s : result) {
            System.out.println(s);
        }
    }
}
