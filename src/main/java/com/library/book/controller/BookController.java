package com.library.book.controller;

import com.library.auth.common.Result;
import com.library.book.entity.Book;
import com.library.book.service.BookService;
import gaarason.database.appointment.Paginate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import java.util.List;

/**
 * 图书管理控制器
 */
@RestController
@RequestMapping("/api/book")
public class BookController {

    @Autowired
    private BookService bookService;

    /**
     * 1.1 查看所有图书列表
     * GET /api/book/list
     * 
     * @param page 页码（默认1）
     * @param limit 每页数量（默认10）
     * @param title 书名（可选，模糊查询）
     * @param author 作者（可选，模糊查询）
     * @param isbn ISBN号（可选，模糊查询）
     * @param classification 分类（可选，模糊查询）
     * @return 分页结果
     */
    @GetMapping("/list")
    public Result getBookList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer limit,
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String author,
            @RequestParam(required = false) String isbn,
            @RequestParam(required = false) String classification) {
        
        Paginate<Book> result = bookService.getBookList(page, limit, title, author, isbn, classification);
        return Result.success(result);
    }

    /**
     * 1.2 图书检索（按标题、读者、ISBN、分类进行模糊查询）
     * GET /api/book/search
     * 
     * @param keyword 搜索关键词
     * @param page 页码（默认1）
     * @param limit 每页数量（默认10）
     * @return 匹配的图书列表
     */
    @GetMapping("/search")
    public Result searchBooks(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer limit) {
        
        Paginate<Book> result = bookService.searchBooks(keyword, page, limit);
        return Result.success(result);
    }

    /**
     * 1.3 录入图书基本信息
     * POST /api/book
     * 
     * @param book 图书信息（包含title、author、isbn、classification等字段）
     * @return 操作结果
     */
    @PostMapping
    public Result addBook(@RequestBody Book book) {
        Boolean success = bookService.addBook(book);
        if (success) {
            return Result.success();
        } else {
            return Result.error(500, "添加图书失败");
        }
    }

    /**
     * 1.4 图书入库
     * POST /api/book/stock-in
     * 
     * @param bookId 图书ID
     * @param location 位置信息
     * @return 操作结果
     */
    @PostMapping("/stock-in")
    public Result stockIn(
            @RequestParam Integer bookId,
            @RequestParam String location) {
        
        Boolean success = bookService.stockIn(bookId, location);
        if (success) {
            // 返回成功信息和最新的库存数量
            return Result.success("图书入库成功");
        } else {
            return Result.error(500, "图书入库失败，图书不存在");
        }
    }

    /**
     * 1.5 图书下架
     * POST /api/book/stock-out
     * 
     * @param addressId 地址表的id
     * @return 操作结果
     */
    @PostMapping("/stock-out")
    public Result stockOut(@RequestParam Integer addressId) {
        Boolean success = bookService.stockOut(addressId);
        if (success) {
            return Result.success();
        } else {
            return Result.error(500, "图书下架失败，库存不足或图书不存在");
        }
    }

    /**
     * 1.6 调整图书库存
     * POST /api/book/adjust
     * 
     * @param bookId 图书ID
     * @param trueInventory 真实库存数量
     * @return 操作结果
     */
    @PostMapping("/adjust")
    public Result adjustInventory(
            @RequestParam Integer bookId,
            @RequestParam Integer trueInventory) {
        
        Boolean success = bookService.adjustInventory(bookId, trueInventory);
        if (success) {
            return Result.success();
        } else {
            return Result.error(500, "调整库存失败，库存数量不能为负数或图书不存在");
        }
    }

    /**
     * 1.8 查看图书封面图片
     * GET /api/book/cover/get
     * 
     * @param bookId 图书ID
     * @return 封面图片URL
     */
    @GetMapping("/cover/get")
    public Result getCoverImage(@RequestParam Integer bookId) {
        String coverImage = bookService.getCoverImage(bookId);
        if (coverImage != null) {
            return Result.success(coverImage);
        } else {
            return Result.error(404, "图书不存在或未设置封面图片");
        }
    }

    /**
     * 1.9 删除图书封面图片
     * DELETE /api/book/cover/delete
     * 
     * @param bookId 图书ID
     * @return 操作结果
     */
    @DeleteMapping("/cover/delete")
    public Result deleteCoverImage(@RequestParam Integer bookId) {
        Boolean success = bookService.deleteCoverImage(bookId);
        if (success) {
            return Result.success();
        } else {
            return Result.error(500, "删除封面图片失败，图书不存在");
        }
    }

    /**
     * 1.10 修改图书封面图片
     * POST /api/book/cover/modify
     * 
     * @param bookId 图书ID
     * @param coverImageUrl 新的封面图片URL
     * @return 操作结果
     */
    @PostMapping("/cover/modify")
    public Result modifyCoverImage(
            @RequestParam Integer bookId,
            @RequestParam String coverImageUrl) {
        
        Boolean success = bookService.modifyCoverImage(bookId, coverImageUrl);
        if (success) {
            return Result.success();
        } else {
            return Result.error(500, "修改封面图片失败，参数错误或图书不存在");
        }
    }

    /**
     * 1.11 查看图书标签列表
     * GET /api/book/tag/get
     * 
     * @param bookId 图书ID
     * @return 标签列表
     */
    @GetMapping("/tag/get")
    public Result getTags(@RequestParam Integer bookId) {
        List<String> tags = bookService.getTags(bookId);
        if (tags != null && !tags.isEmpty()) {
            return Result.success(tags);
        } else {
            return Result.error(404, "图书不存在或未设置标签");
        }
    }

    /**
     * 1.12 添加图书标签
     * POST /api/book/tag/add
     * 
     * @param bookId 图书ID
     * @param tagName 要添加的标签名称
     * @return 操作结果
     */
    @PostMapping("/tag/add")
    public Result addTag(
            @RequestParam Integer bookId,
            @RequestParam String tagName) {
        
        Boolean success = bookService.addTag(bookId, tagName);
        if (success) {
            return Result.success();
        } else {
            return Result.error(500, "添加标签失败，参数错误或图书不存在");
        }
    }

    /**
     * 1.13 修改图书标签（清空原有标签，添加新标签列表）
     * POST /api/book/tag/modify
     * 
     * @param bookId 图书ID
     * @param tagNames 新的标签名称列表（JSON数组格式）
     * @return 操作结果
     */
    @PostMapping("/tag/modify")
    public Result modifyTags(
            @RequestParam Integer bookId,
            @RequestBody List<String> tagNames) {
        
        Boolean success = bookService.modifyTags(bookId, tagNames);
        if (success) {
            return Result.success();
        } else {
            return Result.error(500, "修改标签失败，参数错误或图书不存在");
        }
    }

    /**
     * 1.14 删除图书标签
     * DELETE /api/book/tag/delete
     * 
     * @param bookId 图书ID
     * @param tagName 要删除的标签名称
     * @return 操作结果
     */
    @DeleteMapping("/tag/delete")
    public Result deleteTag(
            @RequestParam Integer bookId,
            @RequestParam String tagName) {
        
        Boolean success = bookService.deleteTag(bookId, tagName);
        if (success) {
            return Result.success();
        } else {
            return Result.error(500, "删除标签失败，参数错误或图书不存在");
        }
    }
}
