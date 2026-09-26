# -*- coding: utf-8 -*-
"""
HJ212Parser —— 用于解析 HJ212-2017 环保协议报文的 Python 类库。

报文结构（依据《污染物在线监控（监测）系统数据传输标准》HJ 212-2017）：
    ## + 4位长度(数据段ASCII字符数) + 数据段 + 4位CRC校验 + \r\n

数据段由 "字段=值" 通过 ';' 连接，其中指令参数 CP 形如 CP=&&数据区&&，
数据区内部同样以 ';' 分隔不同字段。

CRC 校验：ANSI CRC16，初始值 0xFFFF，多项式 0xA001，
校验码按先高字节后低字节的顺序存放。
"""

import re


class HJ212Parser:
    """HJ212-2017 协议报文解析器。"""

    HEADER = "##"
    POLYNOMIAL = 0xA001          # CRC16 多项式
    INIT_CRC = 0xFFFF            # CRC16 初始值
    LEN_DIGITS = 4               # 数据段长度字段位数
    CRC_DIGITS = 4               # CRC 校验码位数
    TAIL = "\r\n"                # 包尾

    # ------------------------------------------------------------------
    # 1. 报文格式合法性验证
    # ------------------------------------------------------------------
    def _normalize(self, message):
        """去除报文两端的空格/制表符等空白，但保留包尾 \\r\\n（不为其自动补全）。"""
        if not isinstance(message, str):
            return None
        return message.strip(" \t\x0b\x0c")

    def is_valid_message(self, message):
        """
        检查报文格式是否正确。

        规则：
        1) 必须以 "##" 开头，以 "\r\n" 结尾；
        2) "##" 后必须有 4 位十进制数字表示数据段长度；
        3) 数据段实际长度必须与长度字段声明一致；
        4) 数据段后的 4 位字符必须是十六进制 CRC 码；
        5) 数据段中的 CP 字段必须成对闭合（CP=&&...&&）。

        返回：True 表示格式合法，False 表示不合法。
        """
        msg = self._normalize(message)
        if msg is None:
            return False
        if not msg.startswith(self.HEADER):
            return False
        if not msg.endswith(self.TAIL):
            return False

        # 头部 + 长度(4) + 数据段 + CRC(4) + 包尾(2)
        if len(msg) < len(self.HEADER) + self.LEN_DIGITS + self.CRC_DIGITS + len(self.TAIL):
            return False

        length_part = msg[len(self.HEADER): len(self.HEADER) + self.LEN_DIGITS]
        if not length_part.isdigit():
            return False

        declared_len = int(length_part)
        data_segment = msg[len(self.HEADER) + self.LEN_DIGITS:
                           len(self.HEADER) + self.LEN_DIGITS + declared_len]
        # 数据段长度校验
        if len(data_segment) != declared_len:
            return False

        crc_part = msg[len(self.HEADER) + self.LEN_DIGITS + declared_len:
                       len(self.HEADER) + self.LEN_DIGITS + declared_len + self.CRC_DIGITS]
        if not re.fullmatch(r"[0-9A-Fa-f]{%d}" % self.CRC_DIGITS, crc_part):
            return False

        # CP 字段成对闭合检查
        cp_matches = re.findall(r"CP=&&(.*?)&&", data_segment)
        if cp_matches is None or len(cp_matches) == 0:
            return False
        return True

    # ------------------------------------------------------------------
    # 2. ANSI CRC16 校验
    # ------------------------------------------------------------------
    def crc16(self, data):
        """
        计算 ANSI CRC16 校验码。
        :param data: 需要校验的字节串（数据段以 UTF-8 编码后的字节）
        :return: 16 位 CRC 校验值
        """
        if isinstance(data, str):
            data = data.encode("utf-8")
        crc_reg = self.INIT_CRC
        for byte in data:
            crc_reg = (crc_reg >> 8) ^ byte
            for _ in range(8):
                if crc_reg & 0x0001:
                    crc_reg = (crc_reg >> 1) ^ self.POLYNOMIAL
                else:
                    crc_reg >>= 1
        return crc_reg & 0xFFFF

    def validate_crc(self, message):
        """
        实现 ANSI CRC16 算法，校验报文的 CRC 值是否与报文中的数据段一致。
        返回：True 表示 CRC 校验通过，False 表示不通过。
        """
        msg = self._normalize(message)
        if msg is None:
            return False
        if not msg.startswith(self.HEADER) or not msg.endswith(self.TAIL):
            return False

        length_part = msg[len(self.HEADER): len(self.HEADER) + self.LEN_DIGITS]
        if not length_part.isdigit():
            return False

        declared_len = int(length_part)
        data_segment = msg[len(self.HEADER) + self.LEN_DIGITS:
                           len(self.HEADER) + self.LEN_DIGITS + declared_len]
        crc_part = msg[len(self.HEADER) + self.LEN_DIGITS + declared_len:
                       len(self.HEADER) + self.LEN_DIGITS + declared_len + self.CRC_DIGITS]

        computed = self.crc16(data_segment)
        # 校验码先高字节后低字节存放，转为 4 位大写十六进制
        computed_hex = "{:04X}".format(computed)
        return computed_hex.upper() == crc_part.upper()

    # ------------------------------------------------------------------
    # 3. 数据段结构化解析
    # ------------------------------------------------------------------
    def parse_data_segment(self, message):
        """
        解析数据段，返回包含所有键值对的字典。
        其中 CP 字段被解析为子字典（数据区内的键值对）。
        返回示例：
            {
              "QN": "20160801085857223", "ST": "32", "CN": "1062",
              "PW": "100000", "MN": "...", "Flag": "5",
              "CP": {"RtdInterval": "30"}
            }
        """
        msg = self._normalize(message)
        if msg is None:
            return {}
        length_part = msg[len(self.HEADER): len(self.HEADER) + self.LEN_DIGITS]
        declared_len = int(length_part)
        data_segment = msg[len(self.HEADER) + self.LEN_DIGITS:
                           len(self.HEADER) + self.LEN_DIGITS + declared_len]

        result = {}

        # 先取出 CP=&&数据区&&，避免其内部 ';' 干扰顶层字段切分
        cp_content = None
        cp_match = re.search(r"CP=&&(.*?)&&", data_segment, flags=re.DOTALL)
        remaining = data_segment
        if cp_match:
            cp_content = cp_match.group(1)
            remaining = data_segment[:cp_match.start()] + data_segment[cp_match.end():]

        # 解析顶层字段（QN、ST、CN、PW、MN、Flag、PNUM、PNO 等）
        for pair in remaining.split(";"):
            pair = pair.strip()
            if pair and "=" in pair:
                key, value = pair.split("=", 1)
                result[key.strip()] = value.strip()

        # 解析 CP 数据区为子字典
        if cp_content is not None:
            cp_dict = {}
            for pair in cp_content.split(";"):
                pair = pair.strip()
                if pair and "=" in pair:
                    key, value = pair.split("=", 1)
                    cp_dict[key.strip()] = value.strip()
            result["CP"] = cp_dict

        return result

    # ------------------------------------------------------------------
    # 4. 监测因子数据提取
    # ------------------------------------------------------------------
    def extract_monitoring_data(self, message):
        """
        从 CP 字段中提取所有监测因子及其数值。
        返回：一个字典，键为监测因子编码（如 w01001-Rtd、w00000-Cou 等），
              值为对应的监测数值（字符串形式）。
        """
        data = self.parse_data_segment(message)
        cp = data.get("CP", {})
        monitoring = {}
        for key, value in cp.items():
            # 监测因子字段一般形如 "xxxxxx-类型"（如 w01001-Rtd、e01001-Rtd）
            if "-" in key or re.fullmatch(r"[a-zA-Z]+\d+", key):
                monitoring[key] = value
            else:
                # 其余如 DataTime 等控制字段一并保留，便于完整查看
                monitoring[key] = value
        return monitoring


# ----------------------------------------------------------------------
# 使用示例（可直接运行 python HJ212Parser.py 查看效果）
# ----------------------------------------------------------------------
if __name__ == "__main__":
    # 报文示例来自 HJ212-2017 附录 C 通讯命令示例（字段与 CRC 已按规范编写）
    sample = ("##0101QN=20160801085857223;ST=32;CN=1062;PW=100000;"
              "MN=010000A8900016F000169DC0;Flag=5;CP=&&RtdInterval=30&&1C80\r\n")

    parser = HJ212Parser()
    print("报文格式合法:", parser.is_valid_message(sample))
    print("CRC 校验通过:", parser.validate_crc(sample))
    print("数据段解析:", parser.parse_data_segment(sample))
