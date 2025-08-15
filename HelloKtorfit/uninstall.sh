#!/system/bin/sh

echo "正在获取第三方应用列表..."
packages=$(pm list packages -3 | cut -d: -f2)

if [ -z "$packages" ]; then
    echo "没有找到第三方应用"
    exit 0
fi

echo "以下应用将被卸载:"
echo "$packages"
echo "==="

for package in $packages; do
    echo "正在卸载: $package"
    pm uninstall $package
done

echo "卸载完成"