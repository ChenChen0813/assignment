# -*- coding: utf-8 -*-
"""HJ212Parser 回归测试用例。

运行方式：
    python -m unittest tests.test_hj212
    或
    python tests/test_hj212.py
"""
import os
import sys
import unittest

sys.path.insert(0, os.path.dirname(os.path.dirname(os.path.abspath(__file__))))

from HJ212Parser import HJ212Parser


class TestHJ212Parser(unittest.TestCase):
    """对 HJ212-2017 报文解析类的单元测试。"""

    def setUp(self):
        self.parser = HJ212Parser()
        # 报文示例依据 HJ212-2017 附录 C（长度字段 0101，CRC=1C80）
        self.valid_msg = ("##0101QN=20160801085857223;ST=32;CN=1062;"
                          "PW=100000;MN=010000A8900016F000169DC0;"
                          "Flag=5;CP=&&RtdInterval=30&&1C80\r\n")

    def test_is_valid_message_ok(self):
        self.assertTrue(self.parser.is_valid_message(self.valid_msg))

    def test_is_valid_message_wrong_header(self):
        self.assertFalse(self.parser.is_valid_message("xx" + self.valid_msg[2:]))

    def test_is_valid_message_no_tail(self):
        self.assertFalse(self.parser.is_valid_message(self.valid_msg[:-2]))

    def test_is_valid_message_bad_length(self):
        bad = self.valid_msg[:2] + "9999" + self.valid_msg[6:]
        self.assertFalse(self.parser.is_valid_message(bad))

    def test_validate_crc_ok(self):
        self.assertTrue(self.parser.validate_crc(self.valid_msg))

    def test_validate_crc_bad(self):
        # 篡改CRC值，校验应失败
        bad = self.valid_msg[:-6] + "0000\r\n"
        self.assertFalse(self.parser.validate_crc(bad))

    def test_parse_data_segment(self):
        data = self.parser.parse_data_segment(self.valid_msg)
        self.assertEqual(data["ST"], "32")
        self.assertEqual(data["CN"], "1062")
        self.assertEqual(data["PW"], "100000")
        self.assertEqual(data["MN"], "010000A8900016F000169DC0")
        self.assertEqual(data["CP"]["RtdInterval"], "30")

    def test_extract_monitoring_data(self):
        monitoring = self.parser.extract_monitoring_data(self.valid_msg)
        self.assertIn("RtdInterval", monitoring)
        self.assertEqual(monitoring["RtdInterval"], "30")


if __name__ == "__main__":
    unittest.main()
