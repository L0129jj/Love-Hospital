package cn.edu.scnu.auth.controller;

import cn.edu.scnu.auth.dto.LoginDTO;
import cn.edu.scnu.auth.dto.PatientLoginDTO;
import cn.edu.scnu.auth.dto.RefreshDTO;
import cn.edu.scnu.auth.dto.TokenVO;
import cn.edu.scnu.auth.service.AuthService;
import cn.edu.scnu.result.Result;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    @cn.edu.scnu.annotation.NoAuth
    public Result<TokenVO> login(@Valid @RequestBody LoginDTO dto) {
        return Result.success(authService.login(dto));
    }

    @PostMapping("/patient-login")
    @cn.edu.scnu.annotation.NoAuth
    public Result<TokenVO> patientLogin(@Valid @RequestBody PatientLoginDTO dto) {
        return Result.success(authService.patientLogin(dto));
    }

    @PostMapping("/doctor-login")
    @cn.edu.scnu.annotation.NoAuth
    public Result<TokenVO> doctorLogin(@Valid @RequestBody cn.edu.scnu.auth.dto.DoctorLoginDTO dto) {
        return Result.success(authService.doctorLogin(dto));
    }

    @PostMapping("/patient-register")
    @cn.edu.scnu.annotation.NoAuth
    public Result<Void> patientRegister(@Valid @RequestBody cn.edu.scnu.auth.dto.PatientRegisterDTO dto) {
        return authService.patientRegister(dto);
    }

    @PostMapping("/doctor-register")
    @cn.edu.scnu.annotation.NoAuth
    public Result<Void> doctorRegister(@Valid @RequestBody cn.edu.scnu.auth.dto.DoctorRegisterDTO dto) {
        return authService.doctorRegister(dto);
    }

    @PostMapping("/logout")
    public Result<Void> logout(@RequestHeader("Authorization") String authHeader) {
        String token = authHeader.replace("Bearer ", "");
        return authService.logOut(token);
    }

    @PostMapping("/refresh")
    @cn.edu.scnu.annotation.NoAuth
    public Result<TokenVO> refresh(@RequestBody RefreshDTO dto) {
        return Result.success(authService.refresh(dto));
    }

    /**
     * 获取患者详细信息，包括真实的手机号、联系地址与动态就诊卡余额
     */
    @GetMapping("/patient/{id}")
    public Result<cn.edu.scnu.auth.entity.Patient> getPatientById(@PathVariable Long id) {
        return authService.getPatientById(id);
    }

    /**
     * 更新患者详细信息，包括姓名、性别、手机号、联系地址等
     */
    @PutMapping("/patient/update/{id}")
    public Result<Void> updatePatient(@PathVariable Long id, @RequestBody cn.edu.scnu.auth.entity.Patient patient) {
        return authService.updatePatientProfile(id, patient);
    }

    /**
     * 为就诊卡快捷充值余额
     */
    @PutMapping("/patient/{id}/recharge-card")
    public Result<java.math.BigDecimal> rechargeCardBalance(@PathVariable Long id, @RequestParam java.math.BigDecimal amount) {
        return authService.rechargeCardBalance(id, amount);
    }
}

