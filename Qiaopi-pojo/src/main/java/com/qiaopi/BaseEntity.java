package com.qiaopi;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serializable;
import java.util.Date;

/**
 * Entity基类
 * 
 * @author Abin
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BaseEntity implements Serializable {
    private static final long serialVersionUID = 1L;

//    /** 搜索值 */
//    private String searchValue;

    /** 创建者 */
    private String createBy;

    /** 创建时间 */
    private Date createTime;

    /** 更新者 */
    private String updateBy;

    /** 更新时间 */
    private Date updateTime;

    /** 备注 */
    private String remark;

//    /** 请求参数 */
//    private Map<String, Object> params;


//    public Map<String, Object> getParams()
//    {
//        if (params == null)
//        {
//            params = new HashMap<>();
//        }
//        return params;
//    }

}
