package com.lingnan.fruitshop.dto.customer.common.vo;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class PageResponse<T> {

    private long total;
    private List<T> list;
}
