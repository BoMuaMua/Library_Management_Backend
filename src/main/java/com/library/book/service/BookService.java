package com.library.book.service;

import com.library.book.entity.Book;
import gaarason.database.appointment.Paginate;

import java.util.List;

import java.util.List;

/**
 * 图书服务接口
 */
public interface BookService {

    /**
     * 1.1 查看所有图书列表（分页查询）
     * @param page 页码
     * @param limit 每页数量
     * @param title 书名（模糊查询，可选）
     * @param author 作者（模糊查询，可选）
     * @param isbn ISBN号（模糊查询，可选）
     * @param classification 分类（模糊查询，可选）
     * @return 分页结果
     */
    Paginate<Book> getBookList(Integer page, Integer limit, String title, 
                               String author, String isbn, String classification);

    /**
     * 1.2 图书检索（按标题、读者、ISBN、分类进行模糊查询）
     * @param keyword 搜索关键词
     * @return 匹配的图书列表
     */
    Paginate<Book> searchBooks(String keyword, Integer page, Integer limit);

    /**
     * 1.3 录入图书基本信息
     * @param book 图书信息
     * @return 是否成功
     */
    Boolean addBook(Book book);

    /**
     * 1.4 图书入库
     * @param bookId 图书ID
     * @param location 位置信息
     * @return 是否成功
     */
    Boolean stockIn(Integer bookId, String location);

    /**
     * 1.5 图书下架
     * @param addressId 地址表的id
     * @return 是否成功
     */
    Boolean stockOut(Integer addressId);

    /**
     * 1.6 调整图书库存
     * @param bookId 图书ID
     * @param trueInventory 真实库存数量
     * @return 是否成功
     */
    Boolean adjustInventory(Integer bookId, Integer trueInventory);

    /**
     * 1.8 查看图书封面图片
     * @param bookId 图书ID
     * @return 封面图片URL，如果不存在返回null
     */
    String getCoverImage(Integer bookId);

    /**
     * 1.9 删除图书封面图片
     * @param bookId 图书ID
     * @return 是否成功
     */
    Boolean deleteCoverImage(Integer bookId);

    /**
     * 1.10 修改图书封面图片（如果不存在则新增）
     * @param bookId 图书ID
     * @param coverImageUrl 新的封面图片URL
     * @return 是否成功
     */
    Boolean modifyCoverImage(Integer bookId, String coverImageUrl);

    /**
     * 1.11 查看图书标签列表
     * @param bookId 图书ID
     * @return 标签列表
     */
    List<String> getTags(Integer bookId);

    /**
     * 1.12 添加图书标签
     * @param bookId 图书ID
     * @param tagName 要添加的标签名称
     * @return 是否成功
     */
    Boolean addTag(Integer bookId, String tagName);

    /**
     * 1.13 修改图书标签（清空原有标签，添加新标签列表）
     * @param bookId 图书ID
     * @param tagNames 新的标签名称列表
     * @return 是否成功
     */
    Boolean modifyTags(Integer bookId, List<String> tagNames);

    /**
     * 1.14 删除图书标签
     * @param bookId 图书ID
     * @param tagName 要删除的标签名称
     * @return 是否成功
     */
    Boolean deleteTag(Integer bookId, String tagName);
}
