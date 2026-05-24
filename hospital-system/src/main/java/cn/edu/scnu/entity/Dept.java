package cn.edu.scnu.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("department")
public class Dept {
    @TableId(value = "dept_id", type = IdType.AUTO)
    private Long id;

    @TableField("dept_name")
    private String name;

    @TableField("dept_type")
    private String deptType;

    private String location;

    private String description;
}

