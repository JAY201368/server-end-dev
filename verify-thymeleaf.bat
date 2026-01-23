@echo off
REM Thymeleaf 集成验证脚本

echo ==========================================
echo Thymeleaf 集成验证
echo ==========================================
echo.

echo [1/3] 检查后端服务状态...
curl -s http://localhost:8088/api/health > nul
if %errorlevel% neq 0 (
    echo ❌ 后端服务未启动，请先启动后端应用
    echo    cd backend
    echo    mvn spring-boot:run
    pause
    exit /b 1
)
echo ✅ 后端服务正常运行

echo.
echo [2/3] 访问 Thymeleaf 门户首页...
start http://localhost:8088/api/
timeout /t 2 /nobreak > nul

echo.
echo [3/3] 访问其他 Thymeleaf 页面...
echo    - 系统公告: http://localhost:8088/api/notice
echo    - 关于系统: http://localhost:8088/api/about

echo.
echo ==========================================
echo 验证完成！
echo ==========================================
echo.
echo 请在浏览器中检查以下内容：
echo   ✓ 门户首页是否正常显示
echo   ✓ 系统名称、版本号是否正确
echo   ✓ 当前时间是否显示（服务端渲染）
echo   ✓ "进入管理系统"按钮是否可点击
echo   ✓ 点击后是否跳转到 Vue3 应用
echo.
echo 如果一切正常，说明 Thymeleaf 集成成功！
echo.
pause
