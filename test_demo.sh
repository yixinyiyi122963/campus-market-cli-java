#!/bin/bash

echo "=================================================="
echo "  校园二手交易系统 - 功能演示测试"
echo "=================================================="
echo ""

# Clean up previous data
rm -f campus-market-data.ser

echo "【测试1：用户注册】"
cat > /tmp/test1.txt << 'EOF'
register
testbuyer
password123
password123
1
buyer@test.com
13800138000
exit
n
EOF
java -cp out com.campus.market.App < /tmp/test1.txt 2>&1 | grep -A 5 "用户注册"
echo ""

echo "【测试2：买家登录并搜索商品】"
cat > /tmp/test2.txt << 'EOF'
login
buyer1
123456
search
search Java
logout
exit
n
EOF
java -cp out com.campus.market.App < /tmp/test2.txt 2>&1 | grep -A 10 "搜索结果"
echo ""

echo "【测试3：卖家发布新商品】"
cat > /tmp/test3.txt << 'EOF'
login
seller1
123456
product add
笔记本电脑
联想ThinkPad，i5处理器，8G内存，256G固态硬盘
电子产品
1500
product my
save
logout
exit
n
EOF
java -cp out com.campus.market.App < /tmp/test3.txt 2>&1 | grep -A 6 "商品发布成功"
echo ""

echo "【测试4：管理员查看系统数据】"
cat > /tmp/test4.txt << 'EOF'
load
login
admin
123456
user list
product all
exit
n
EOF
java -cp out com.campus.market.App < /tmp/test4.txt 2>&1 | grep -A 8 "用户列表"
echo ""

echo "【测试5：数据持久化验证】"
cat > /tmp/test5.txt << 'EOF'
load
login
buyer1
123456
search 笔记本
exit
n
EOF
java -cp out com.campus.market.App < /tmp/test5.txt 2>&1 | grep -B 2 -A 5 "笔记本"
echo ""

echo "=================================================="
echo "  所有测试完成！"
echo "=================================================="
