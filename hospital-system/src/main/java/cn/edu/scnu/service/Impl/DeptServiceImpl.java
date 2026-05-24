package cn.edu.scnu.service.Impl;

import cn.edu.scnu.entity.Dept;
import cn.edu.scnu.mapper.DeptMapper;
import cn.edu.scnu.service.DeptService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

@Service
public class DeptServiceImpl extends ServiceImpl<DeptMapper, Dept> implements DeptService {
}
