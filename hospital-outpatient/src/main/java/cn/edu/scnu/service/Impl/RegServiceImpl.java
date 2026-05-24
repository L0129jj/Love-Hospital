package cn.edu.scnu.service.Impl;

import cn.edu.scnu.entity.Registration;
import cn.edu.scnu.mapper.RegMapper;
import cn.edu.scnu.service.RegService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class RegServiceImpl extends ServiceImpl<RegMapper, Registration> implements RegService {

}
