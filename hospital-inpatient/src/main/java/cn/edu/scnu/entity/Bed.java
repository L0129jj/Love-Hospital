package cn.edu.scnu.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("bed")
public class Bed {
    @TableId("bed_id")
    private Long id;
    private Long wardId;
    private String bedNo;
    private String status;
}
