package cn.edu.scnu.mapper;

import org.apache.ibatis.annotations.Mapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import cn.edu.scnu.entity.Drug;

@Mapper
public interface DrugMapper extends BaseMapper<Drug> {
}
