package cn.edu.scnu.auth.service.Impl;

import cn.edu.scnu.auth.dto.LoginDTO;
import cn.edu.scnu.auth.dto.PatientLoginDTO;
import cn.edu.scnu.auth.dto.RefreshDTO;
import cn.edu.scnu.auth.dto.TokenVO;
import cn.edu.scnu.auth.entity.Admin;
import cn.edu.scnu.auth.entity.Patient;
import cn.edu.scnu.auth.mapper.AdminMapper;
import cn.edu.scnu.auth.mapper.PatientMapper;
import cn.edu.scnu.auth.service.AuthService;
import cn.edu.scnu.result.Result;
import cn.edu.scnu.utils.JwtUtil;
import cn.edu.scnu.utils.PasswordUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import io.jsonwebtoken.Claims;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Date;

@Slf4j

@Service
public class AuthServiceImpl implements AuthService {

    private final AdminMapper adminMapper;
    private final PatientMapper patientMapper;
    private final cn.edu.scnu.auth.mapper.DoctorAuthMapper doctorMapper;
    private final StringRedisTemplate redisTemplate;

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${jwt.expiration}")
    private long jwtExpiration;

    public AuthServiceImpl(AdminMapper adminMapper, PatientMapper patientMapper,
                           cn.edu.scnu.auth.mapper.DoctorAuthMapper doctorMapper,
                           StringRedisTemplate redisTemplate) {
        this.adminMapper = adminMapper;
        this.patientMapper = patientMapper;
        this.doctorMapper = doctorMapper;
        this.redisTemplate = redisTemplate;
    }

    @Override
    public TokenVO login(LoginDTO dto) {
        Admin admin = adminMapper.selectOne(new LambdaQueryWrapper<Admin>()
                .eq(Admin::getUsername, dto.getUsername()));
        if (admin == null) {
            log.warn("登录失败：用户名 {} 不存在", dto.getUsername());
            throw new RuntimeException("用户名或密码错误");
        }
        if (admin.getStatus() != null && admin.getStatus() == 0) {
            log.warn("登录失败：账号 {} 已被禁用", dto.getUsername());
            throw new RuntimeException("账号已被禁用");
        }
        if (!PasswordUtil.matches(dto.getPassword(), admin.getPassword())) {
            log.warn("登录失败：密码错误（用户名={}）", dto.getUsername());
            throw new RuntimeException("用户名或密码错误");
        }
        admin.setLoginStatus(1);
        adminMapper.updateById(admin);
        log.info("登录成功：username={}, role={}, name={}",
                dto.getUsername(), admin.getRole(), admin.getName());
        return buildToken(admin.getUsername(), admin.getId(), admin.getRole(), admin.getName());
    }

    @Override
    public TokenVO patientLogin(PatientLoginDTO dto) {
        Patient patient = patientMapper.selectOne(new LambdaQueryWrapper<Patient>()
                .eq(Patient::getPhone, dto.getPhone())
                .or()
                .eq(Patient::getUsername, dto.getPhone()));
        if (patient == null) {
            throw new RuntimeException("手机号/用户名或密码错误");
        }
        if (!PasswordUtil.matches(dto.getPassword(), patient.getPassword())) {
            throw new RuntimeException("手机号/用户名或密码错误");
        }
        // 更新登录状态为在线
        patient.setLoginStatus(1);
        patientMapper.updateById(patient);
        return buildToken(patient.getPhone(), patient.getId(), "ROLE_PATIENT", patient.getName());
    }

    @Override
    public TokenVO doctorLogin(cn.edu.scnu.auth.dto.DoctorLoginDTO dto) {
        cn.edu.scnu.auth.entity.Doctor doctor = doctorMapper.selectOne(
                new LambdaQueryWrapper<cn.edu.scnu.auth.entity.Doctor>()
                        .eq(cn.edu.scnu.auth.entity.Doctor::getWorkNo, dto.getWorkNo()));
        if (doctor == null) {
            throw new RuntimeException("工号或密码错误");
        }
        if (!PasswordUtil.matches(dto.getPassword(), doctor.getPassword())) {
            throw new RuntimeException("工号或密码错误");
        }
        // 更新登录状态为在线
        doctor.setLoginStatus(1);
        doctorMapper.updateById(doctor);
        return buildToken(doctor.getWorkNo(), doctor.getId(), "ROLE_DOCTOR", doctor.getName());
    }

    @Override
    public Result<Void> patientRegister(cn.edu.scnu.auth.dto.PatientRegisterDTO dto) {
        // 校验用户名是否已存在
        Long userCount = patientMapper.selectCount(new LambdaQueryWrapper<Patient>()
                .eq(Patient::getUsername, dto.getUsername()));
        if (userCount > 0) {
            throw new RuntimeException("用户名已被占用");
        }
        // 校验手机号是否已存在
        Long phoneCount = patientMapper.selectCount(new LambdaQueryWrapper<Patient>()
                .eq(Patient::getPhone, dto.getPhone()));
        if (phoneCount > 0) {
            throw new RuntimeException("该手机号已注册");
        }

        Patient patient = new Patient();
        patient.setName(dto.getName());
        patient.setGender(dto.getGender());
        patient.setAddress(dto.getAddress());
        patient.setPhone(dto.getPhone());
        patient.setUsername(dto.getUsername());
        patient.setPassword(PasswordUtil.encode(dto.getPassword())); // 新注册密码用 BCrypt 加密
        patient.setLoginStatus(0); // 初始离线

        patientMapper.insert(patient);
        return Result.success(null);
    }

    @Override
    public Result<Void> doctorRegister(cn.edu.scnu.auth.dto.DoctorRegisterDTO dto) {
        // 校验工号是否已存在
        Long workCount = doctorMapper.selectCount(new LambdaQueryWrapper<cn.edu.scnu.auth.entity.Doctor>()
                .eq(cn.edu.scnu.auth.entity.Doctor::getWorkNo, dto.getWorkNo()));
        if (workCount > 0) {
            throw new RuntimeException("医生工号已被占用");
        }
        // 校验手机号是否已存在
        Long phoneCount = doctorMapper.selectCount(new LambdaQueryWrapper<cn.edu.scnu.auth.entity.Doctor>()
                .eq(cn.edu.scnu.auth.entity.Doctor::getPhone, dto.getPhone()));
        if (phoneCount > 0) {
            throw new RuntimeException("该联系电话已注册过医生账号");
        }

        cn.edu.scnu.auth.entity.Doctor doctor = new cn.edu.scnu.auth.entity.Doctor();
        doctor.setWorkNo(dto.getWorkNo());
        doctor.setName(dto.getName());
        doctor.setGender(dto.getGender());
        doctor.setTitle(dto.getTitle());
        doctor.setPhone(dto.getPhone());
        doctor.setDeptId(dto.getDeptId());
        doctor.setConsultationFee(dto.getConsultationFee());
        doctor.setPassword(PasswordUtil.encode(dto.getPassword())); // 新注册密码用 BCrypt 加密
        doctor.setLoginStatus(0); // 初始离线

        doctorMapper.insert(doctor);
        return Result.success(null);
    }

    @Override
    public Result<Void> logOut(String token) {
        try {
            Claims claims = JwtUtil.parseClaims(token, jwtSecret);
            String role = claims.get("role", String.class);
            Number userIdNum = claims.get("userId", Number.class);
            Long userId = userIdNum != null ? userIdNum.longValue() : null;

            if (userId != null && role != null) {
                if ("ROLE_ADMIN".equals(role)) {
                    Admin admin = adminMapper.selectById(userId);
                    if (admin != null) {
                        admin.setLoginStatus(0); // 退出登录，修改为离线
                        adminMapper.updateById(admin);
                    }
                } else if ("ROLE_DOCTOR".equals(role)) {
                    cn.edu.scnu.auth.entity.Doctor doctor = doctorMapper.selectById(userId);
                    if (doctor != null) {
                        doctor.setLoginStatus(0); // 退出登录，修改为离线
                        doctorMapper.updateById(doctor);
                    }
                } else if ("ROLE_PATIENT".equals(role)) {
                    Patient patient = patientMapper.selectById(userId);
                    if (patient != null) {
                        patient.setLoginStatus(0); // 退出登录，修改为离线
                        patientMapper.updateById(patient);
                    }
                }
            }

            long remaining = claims.getExpiration().getTime() - System.currentTimeMillis();
            if (remaining > 0) {
                redisTemplate.opsForValue().set(
                        "token:blacklist:" + token, "1",
                        Duration.ofMillis(remaining));
            }
        } catch (Exception ignored) {
            // token 无效，无需加入黑名单
        }
        return Result.success(null);
    }

    @Override
    public TokenVO refresh(RefreshDTO dto) {
        TokenVO token = new TokenVO();
        token.setToken("refreshed-demo-token");
        token.setRole("ROLE_ADMIN");
        token.setUserId(1L);
        token.setExpiresAt(System.currentTimeMillis() + jwtExpiration);
        return token;
    }

    private TokenVO buildToken(String subject, Long userId, String role, String name) {
        String token = JwtUtil.createToken(subject, userId, role, jwtSecret, jwtExpiration);
        TokenVO vo = new TokenVO();
        vo.setToken(token);
        vo.setRole(role);
        vo.setUserId(userId);
        vo.setExpiresAt(System.currentTimeMillis() + jwtExpiration);
        vo.setName(name);
        return vo;
    }

    @Override
    public Result<Patient> getPatientById(Long id) {
        Patient patient = patientMapper.selectById(id);
        if (patient == null) {
            return Result.error(404, "患者不存在");
        }
        // 从 Redis 动态获取/初始化就诊卡余额
        String balanceKey = "patient:balance:" + id;
        String balanceStr = redisTemplate.opsForValue().get(balanceKey);
        if (balanceStr == null) {
            // 默认给予 10000.00 元的就诊卡额度并写入 Redis（大幅度提高以保证住院和门诊缴费调试极度畅通）
            redisTemplate.opsForValue().set(balanceKey, "10000.00");
            patient.setCardBalance(java.math.BigDecimal.valueOf(10000.00));
        } else {
            patient.setCardBalance(new java.math.BigDecimal(balanceStr));
        }
        return Result.success(patient);
    }

    @Override
    public Result<Void> updatePatientProfile(Long id, Patient patient) {
        Patient old = patientMapper.selectById(id);
        if (old == null) {
            return Result.error(404, "患者不存在");
        }
        old.setName(patient.getName());
        old.setGender(patient.getGender());
        old.setPhone(patient.getPhone());
        old.setAddress(patient.getAddress());
        patientMapper.updateById(old);
        log.info("修改患者个人资料成功：patientId={}, name={}", id, patient.getName());
        return Result.success(null);
    }

    @Override
    public Result<java.math.BigDecimal> rechargeCardBalance(Long id, java.math.BigDecimal amount) {
        Patient patient = patientMapper.selectById(id);
        if (patient == null) {
            return Result.error(404, "患者不存在");
        }
        String balanceKey = "patient:balance:" + id;
        String balanceStr = redisTemplate.opsForValue().get(balanceKey);
        java.math.BigDecimal curBalance = balanceStr == null ? java.math.BigDecimal.valueOf(10000.00) : new java.math.BigDecimal(balanceStr);
        java.math.BigDecimal nextBalance = curBalance.add(amount);
        redisTemplate.opsForValue().set(balanceKey, nextBalance.toString());
        log.info("快捷就诊卡充值成功：patientId={}, 充值金额={}, 充后余额={}", id, amount, nextBalance);
        return Result.success(nextBalance);
    }
}

