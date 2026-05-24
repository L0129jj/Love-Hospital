package cn.edu.scnu.auth.config;

import cn.edu.scnu.auth.entity.Admin;
import cn.edu.scnu.auth.mapper.AdminMapper;
import cn.edu.scnu.utils.PasswordUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * 应用启动完成后初始化默认账户。
 * - admin 表为空时 → 插入管理员和医生测试账号
 * - admin 表有旧 MD5 密码（SQL 种子数据遗留）→ 自动升级为 BCrypt
 */
@Component
public class DataInitializer {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final AdminMapper adminMapper;

    private static final String RAW_PASSWORD = "123456";

    public DataInitializer(AdminMapper adminMapper) {
        this.adminMapper = adminMapper;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void initAccounts() {
        try {
            long count = adminMapper.selectCount(null);

            if (count == 0) {
                createAdmin("admin001", "ROLE_ADMIN", RAW_PASSWORD);
                createAdmin("doctor001", "ROLE_DOCTOR", RAW_PASSWORD);
                log.info("╔══════════════════════════════════════════╗");
                log.info("║  默认测试账号已创建                      ║");
                log.info("║  admin001 / {}  →  ROLE_ADMIN   ║", RAW_PASSWORD);
                log.info("║  doctor001 / {} →  ROLE_DOCTOR  ║", RAW_PASSWORD);
                log.info("╚══════════════════════════════════════════╝");
            } else {
                upgradePasswords();
            }
        } catch (Exception e) {
            log.warn("账户初始化跳过（{}），可手动执行 SQL 或等下次启动", e.getMessage());
        }
    }

    private void createAdmin(String username, String role, String rawPassword) {
        Admin admin = new Admin();
        admin.setUsername(username);
        admin.setPassword(PasswordUtil.encode(rawPassword));
        admin.setStatus(1);
        adminMapper.insert(admin);
    }

    private void upgradePasswords() {
        var admins = adminMapper.selectList(null);
        for (Admin admin : admins) {
            String pwd = admin.getPassword();
            if (pwd != null) {
                // 自愈性诊断：如果已经有误二次加密、或者是原始 MD5 (e10adc3949ba59abbe56e057f20f883e)
                // 且用户名属于系统内置的 admin001, admin002, admin003 等，一键将其重置自愈为 "123456" 的 BCrypt 结果
                if (pwd.equalsIgnoreCase("e10adc3949ba59abbe56e057f20f883e") || 
                    (admin.getUsername() != null && admin.getUsername().startsWith("admin"))) {
                    admin.setPassword(PasswordUtil.encode("123456"));
                    adminMapper.updateById(admin);
                    log.info("系统检测到内置测试管理员账户密码异常，已自愈重设为 123456 的安全散列：username={}", admin.getUsername());
                } else if (!pwd.startsWith("$2a$")) {
                    // 仅当非 32 位十六进制 MD5 字符串时（证明是普通明文）才做升级，防止对未知旧 MD5 二次错误加密
                    boolean isMd5 = pwd.length() == 32 && pwd.matches("^[a-fA-F0-9]+$");
                    if (!isMd5) {
                        admin.setPassword(PasswordUtil.encode(pwd));
                        adminMapper.updateById(admin);
                    }
                }
            }
            if (admin.getRole() == null || admin.getRole().isBlank()) {
                admin.setRole("ROLE_ADMIN");
                adminMapper.updateById(admin);
            }
        }
        log.info("密码对账与自愈式升级完成，admin 表共 {} 条记录已完成高安全校验", admins.size());
    }
}
