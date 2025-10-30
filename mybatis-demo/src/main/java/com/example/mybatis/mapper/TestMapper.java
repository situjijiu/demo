package mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

/**
 * @author JJBond
 * @date 2025-10-30 21:59
 */
@Mapper
public interface TestMapper {

    @Select("select * from user where id = #{id}")
    String selectUserById(Integer id);
}
