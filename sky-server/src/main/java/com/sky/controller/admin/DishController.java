package com.sky.controller.admin;

import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.DishService;
import com.sky.vo.DishVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.sql.ResultSet;
import java.util.List;
import java.util.Set;

/**
 * 菜品管理
 */
@RestController
@RequestMapping("/admin/dish")
@Api(tags = "菜品相关接口")
@Slf4j
public class DishController {

    @Autowired
    private DishService dishService;

    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 新增菜品
     * @param dishDTO
     * @return
     */
    @PostMapping("/save")
    @ApiOperation(value = "添加菜品")
    public Result save(@RequestBody DishDTO dishDTO) {

        dishService.saveWithFlavor(dishDTO);

        // 清理缓存数据
        String key = "dish_" + dishDTO.getCategoryId();
        clearCache(key);

        return Result.success();
    }

    /**
     * 菜品分页查询
     * @param dishPageQueryDTO
     * @return
     */
    @GetMapping("/page")
    @ApiOperation(value = "菜品分页查询")
    public Result<PageResult> page(DishPageQueryDTO dishPageQueryDTO) {
        PageResult pageResult = dishService.pageQuery(dishPageQueryDTO);
        return Result.success(pageResult);
    }


    /**
     * 菜品的批量删除
     * @param ids
     * @return
     */
    @DeleteMapping("/")
    @ApiOperation(value = "批量删除菜品")
    public Result delete(@RequestParam List<Long> ids) {
        dishService.deleteBatch(ids);

        // 删除所有的 key，清理所有缓存
        clearCache("dish_*");

        return Result.success();
    }

    /**
     * 菜品启售停售
     * @param status
     * @return
     */
    @PostMapping("/status/{status}")
    @ApiOperation("菜品启售停售")
    public Result startOrStop(@PathVariable Integer status, Long id) {
        dishService.updateStatus(status, id);
        // 删除所有的 key，清理所有缓存
        clearCache("dish_*");

        return Result.success();
    }

    /**
     * 根据 id 查询菜品数据
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    @ApiOperation(value = "根据 id 查询菜品")
    public Result<DishVO> getById(@PathVariable Long id) {
        DishVO dishVO = dishService.getByIdWithFlavor(id);
        return Result.success(dishVO);
    }

    /**
     * 修改菜品数据
     * @return
     */
    @PutMapping("/")
    @ApiOperation(value = "修改菜品数据")
    public Result update(@RequestBody DishDTO dishDTO) {
        dishService.updateWithFlavor(dishDTO);

        // 删除所有的 key，清理所有缓存
        clearCache("dish_*");

        return Result.success();
    }

    /**
     * 清理所有缓存数据
     * @param pattern
     */
    private void clearCache(String pattern) {
        Set keys = redisTemplate.keys(pattern);
        redisTemplate.delete(keys);
    }
}
