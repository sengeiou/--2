package com.recharge.mapper;

import com.recharge.domain.Product;
import com.recharge.domain.ProductProp;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * Product Repository
 */
public interface IProductMapper {

    /**
     * 给前端用的
     * @param name
     * @return
     */
    Product selectByName(@Param("name") String name);

    List<Product> selectList(@Param("pid") String pid, @Param("pName") String pName,
                             @Param("curr") Integer curr, @Param("pageSize") Integer pageSize);

    List<Product> selectByProp(@Param("pid") String pid, @Param("pName") String pName,
                               @Param("productPropList") List<ProductProp> productPropList,
                               @Param("curr") Integer curr, @Param("pageSize") Integer pageSize);

    int updateDisable(@Param("ids") String[] ids);

    void updateEnable(@Param("ids") String[] ids);

    List<Product> selectAll();

    Product selectById(@Param("id") String id);

    int delByIds(@Param("ids") String[] ids);

    int insert(Product product);

    int updateInfo(Product product);
}
