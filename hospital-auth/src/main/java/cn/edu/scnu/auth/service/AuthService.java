package cn.edu.scnu.auth.service;

import cn.edu.scnu.auth.dto.LoginDTO;
import cn.edu.scnu.auth.dto.PatientLoginDTO;
import cn.edu.scnu.auth.dto.RefreshDTO;
import cn.edu.scnu.auth.dto.TokenVO;
import cn.edu.scnu.auth.entity.Patient;
import cn.edu.scnu.result.Result;

public interface AuthService {
    TokenVO login(LoginDTO dto);

    TokenVO patientLogin(PatientLoginDTO dto);

    TokenVO doctorLogin(cn.edu.scnu.auth.dto.DoctorLoginDTO dto);

    Result<Void> patientRegister(cn.edu.scnu.auth.dto.PatientRegisterDTO dto);

    Result<Void> doctorRegister(cn.edu.scnu.auth.dto.DoctorRegisterDTO dto);

    Result<Void> logOut(String token);

    TokenVO refresh(RefreshDTO dto);

    Result<Patient> getPatientById(Long id);

    Result<Void> updatePatientProfile(Long id, Patient patient);

    Result<java.math.BigDecimal> rechargeCardBalance(Long id, java.math.BigDecimal amount);
}

