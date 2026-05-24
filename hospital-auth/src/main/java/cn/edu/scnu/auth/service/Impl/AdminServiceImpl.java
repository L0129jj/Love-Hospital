package cn.edu.scnu.auth.service.Impl;

import cn.edu.scnu.auth.entity.Admin;
import cn.edu.scnu.auth.mapper.AdminMapper;
import cn.edu.scnu.auth.service.AdminService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

@Service
public class AdminServiceImpl extends ServiceImpl<AdminMapper, Admin> implements AdminService {
}
