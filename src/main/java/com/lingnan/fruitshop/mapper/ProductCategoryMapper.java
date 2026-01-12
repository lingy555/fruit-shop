package com.lingnan.fruitshop.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lingnan.fruitshop.entity.ProductCategory;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

@Mapper
public interface ProductCategoryMapper extends BaseMapper<ProductCategory> {

    @Select("select category_id as categoryId, count(*) as productCount from product group by category_id")
    List<Map<String, Object>> selectCategoryProductCount();
}
