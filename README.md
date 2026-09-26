# assignment

《软件工程》实验报告一 · 基本编程技能实验的源代码仓库，用于软件工程作业检查。

## 项目结构

```
assignment/
├── hello.py                 # 编程基本功练习(1)：Hello World
├── HJ212Parser.py           # 编程基本功练习(2)：HJ212-2017 环保协议报文解析类库
└── tests/
    └── test_hj212.py        # HJ212Parser 回归测试用例
```

## 功能说明

1. **hello.py**：使用 Python 输出 "Hello World"。
2. **HJ212Parser.py**：解析 HJ212-2017 环保协议报文的 Python 类库，实现四大功能：
   - `is_valid_message(message)`：报文格式合法性验证；
   - `validate_crc(message)`：ANSI CRC16 校验（初始值 0xFFFF，多项式 0xA001）；
   - `parse_data_segment(message)`：数据段结构化解析，返回键值对字典；
   - `extract_monitoring_data(message)`：从 CP 字段提取所有监测因子及其数值。

报文结构：`## + 4位长度 + 数据段 + 4位CRC + \r\n`。

## 运行与测试

```bash
# 运行 Hello World
python hello.py

# 运行回归测试
python -m unittest tests.test_hj212 -v
```

## Git 提交注释规范

本项目遵循常用 Git 提交注释规范（Conventional Commits）：
- `feat:` 新增功能
- `fix:` 修复缺陷
- `test:` 新增或修改测试
- `docs:` 文档更新
