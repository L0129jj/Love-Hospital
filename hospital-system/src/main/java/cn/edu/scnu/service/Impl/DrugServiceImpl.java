package cn.edu.scnu.service.Impl;

import cn.edu.scnu.entity.Drug;
import cn.edu.scnu.mapper.DrugMapper;
import cn.edu.scnu.service.DrugService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

@Service
public class DrugServiceImpl extends ServiceImpl<DrugMapper, Drug> implements DrugService {
}
