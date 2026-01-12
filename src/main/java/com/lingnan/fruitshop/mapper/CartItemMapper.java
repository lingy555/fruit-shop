package com.lingnan.fruitshop.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lingnan.fruitshop.entity.CartItem;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface CartItemMapper extends BaseMapper<CartItem> {
}
