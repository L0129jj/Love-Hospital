package cn.edu.scnu.auth.mapper;

import cn.edu.scnu.auth.entity.Doctor;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface DoctorAuthMapper extends BaseMapper<Doctor> {
}
