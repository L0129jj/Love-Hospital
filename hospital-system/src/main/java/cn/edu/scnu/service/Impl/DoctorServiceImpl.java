package cn.edu.scnu.service.Impl;

import cn.edu.scnu.entity.Doctor;
import cn.edu.scnu.mapper.DoctorMapper;
import cn.edu.scnu.service.DoctorService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

@Service
public class DoctorServiceImpl extends ServiceImpl<DoctorMapper, Doctor> implements DoctorService {
}
