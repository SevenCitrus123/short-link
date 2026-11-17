package admin.dao.mapper;

import admin.dao.entity.UserDO;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

/**
 * 用户接口层
 */
public interface UserMapper extends BaseMapper<UserDO> {
}
