package com.recharge.mapper;

import com.recharge.common.utils.Page;
import com.recharge.domain.ProductSupRelation;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * Created by qi.cao on 2016/5/11.
 */
public interface IProductSupRelationMapper {

    /**
     * 分配供货商的时候用
     * @param productSupRelation
     * @return
     */
    List<ProductSupRelation> selectAll(ProductSupRelation productSupRelation);

    /**
     * 页面用
     * @param page
     * @return
     */
    List<ProductSupRelation> selectAllByPage(Page page);

    ProductSupRelation selectCost(@Param("productId") String productId, @Param("supId") String supId, @Param("level") String level);
}
