package com.lingnan.fruitshop.service.impl;

import com.lingnan.fruitshop.service.AdminCategoryService;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lingnan.fruitshop.common.exception.BizException;
import com.lingnan.fruitshop.dto.admin.category.AdminCategoryAddRequest;
import com.lingnan.fruitshop.dto.admin.category.AdminCategoryUpdateRequest;
import com.lingnan.fruitshop.dto.admin.category.vo.AdminCategoryTreeNodeResponse;
import com.lingnan.fruitshop.entity.Product;
import com.lingnan.fruitshop.entity.ProductCategory;
import com.lingnan.fruitshop.mapper.ProductCategoryMapper;
import com.lingnan.fruitshop.mapper.ProductMapper;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class AdminCategoryServiceImpl implements AdminCategoryService {

    private final ProductCategoryMapper productCategoryMapper;
    private final ProductMapper productMapper;

    public AdminCategoryServiceImpl(ProductCategoryMapper productCategoryMapper, ProductMapper productMapper) {
        this.productCategoryMapper = productCategoryMapper;
        this.productMapper = productMapper;
    }

/**
 * 获取分类树结构的方法
 * @return 返回一个包含所有分类节点的列表，以树形结构组织
 */
    @Override
    public List<AdminCategoryTreeNodeResponse> tree() {
    // 查询所有分类，并按照排序字段升序排列
        List<ProductCategory> all = productCategoryMapper.selectList(new LambdaQueryWrapper<ProductCategory>()
                .orderByAsc(ProductCategory::getSort));

    // 创建一个Map用于存储每个分类的产品数量
        Map<Long, Long> categoryCountMap = new HashMap<>();
    // 查询每个分类对应的产品数量
        for (Map<String, Object> row : productCategoryMapper.selectCategoryProductCount()) {
        // 获取分类ID和产品数量
            Object cid = row.get("categoryId");
            Object cnt = row.get("productCount");
        // 如果分类ID和产品数量都不为空，则将其添加到Map中
            if (cid != null && cnt != null) {
                categoryCountMap.put(Long.parseLong(String.valueOf(cid)), Long.parseLong(String.valueOf(cnt)));
            }
        }

        Map<Long, List<AdminCategoryTreeNodeResponse>> childrenMap = new HashMap<>();
        Map<Long, AdminCategoryTreeNodeResponse> nodeMap = new HashMap<>();

        for (ProductCategory c : all) {
            AdminCategoryTreeNodeResponse node = new AdminCategoryTreeNodeResponse(
                    c.getCategoryId(),
                    c.getCategoryName(),
                    c.getIcon(),
                    c.getParentId(),
                    c.getLevel(),
                    c.getSort(),
                    c.getStatus(),
                    categoryCountMap.getOrDefault(c.getCategoryId(), 0L),
                    new ArrayList<>()
            );
            nodeMap.put(node.getCategoryId(), node);
            childrenMap.computeIfAbsent(c.getParentId(), k -> new ArrayList<>()).add(node);
        }

        for (Map.Entry<Long, List<AdminCategoryTreeNodeResponse>> e : childrenMap.entrySet()) {
            Long parentId = e.getKey();
            AdminCategoryTreeNodeResponse parent = nodeMap.get(parentId);
            if (parent != null) {
                parent.setChildren(e.getValue());
            }
        }

        return childrenMap.getOrDefault(0L, List.of());
    }

/**
 * 添加商品分类的方法
 * @param req 包含商品分类信息的请求对象，包含分类名称、图标、父级ID、层级、排序和状态等信息
 */
    @Override
    public void add(AdminCategoryAddRequest req) {
    // 创建商品分类对象
        ProductCategory c = new ProductCategory();
    // 设置分类名称
        c.setCategoryName(req.getCategoryName());
    // 设置分类图标
        c.setIcon(req.getIcon());
    // 设置父级分类ID
        c.setParentId(req.getParentId());
    // 设置分类层级
        c.setLevel(req.getLevel());
    // 设置分类排序
        c.setSort(req.getSort());
    // 设置分类状态
        c.setStatus(req.getStatus());
    // 将分类信息插入数据库
        productCategoryMapper.insert(c);
    }

/**
 * 更新商品分类信息
 * @param categoryId 分类ID
 * @param req 包含更新后分类信息的请求对象
 */
    @Override
    public void update(long categoryId, AdminCategoryUpdateRequest req) {
    // 根据分类ID查询分类信息
        ProductCategory c = productCategoryMapper.selectById(categoryId);
    // 如果分类不存在，抛出异常
        if (c == null) {
            throw BizException.notFound("分类不存在");
        }
    // 设置分类名称
        c.setCategoryName(req.getCategoryName());
    // 设置分类图标
        c.setIcon(req.getIcon());
    // 设置父级分类ID
        c.setParentId(req.getParentId());
    // 设置分类级别
        c.setLevel(req.getLevel());
    // 设置排序值
        c.setSort(req.getSort());
    // 设置分类状态
        c.setStatus(req.getStatus());
    // 更新分类信息
        productCategoryMapper.updateById(c);
    }

    @Override
    public void delete(long categoryId) {
        ProductCategory c = productCategoryMapper.selectById(categoryId);
        if (c == null) {
            return;
        }

        Long childCount = productCategoryMapper.selectCount(new LambdaQueryWrapper<ProductCategory>()
                .eq(ProductCategory::getParentId, categoryId));
        if (childCount != null && childCount > 0) {
            throw BizException.badRequest("请先删除子分类");
        }

        Long productCount = productMapper.selectCount(new LambdaQueryWrapper<Product>()
                .eq(Product::getCategoryId, categoryId));
        if (productCount != null && productCount > 0) {
            throw BizException.badRequest("分类下存在商品，无法删除");
        }

        productCategoryMapper.deleteById(categoryId);
    }
}
