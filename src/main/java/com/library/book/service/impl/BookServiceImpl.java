package com.library.book.service.impl;

import com.library.book.entity.Book;
import com.library.book.entity.BookImg;
import com.library.book.entity.BookTag;
import com.library.book.model.BookImgModel;
import com.library.book.model.BookModel;
import com.library.book.model.BookTagModel;
import com.library.book.service.BookService;
import gaarason.database.appointment.OrderBy;
import gaarason.database.appointment.Paginate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * 图书服务实现类
 */
@Service
public class BookServiceImpl implements BookService {

    @Autowired
    private BookModel bookModel;

    @Autowired
    private BookImgModel bookImgModel;

    @Autowired
    private BookTagModel bookTagModel;

    /**
     * 1.1 查看所有图书列表（分页查询）
     */
    @Override
    public Paginate<Book> getBookList(Integer page, Integer limit, String title,
                                      String author, String isbn, String classification) {
        // 构建基础查询
        var query = bookModel.baseQuery();

        // 添加筛选条件（模糊查询）
        if (StringUtils.hasText(title)) {
            query.where("title", "like", "%" + title + "%");
        }
        if (StringUtils.hasText(author)) {
            query.where("author", "like", "%" + author + "%");
        }
        if (StringUtils.hasText(isbn)) {
            query.where("isbn", "like", "%" + isbn + "%");
        }
        if (StringUtils.hasText(classification)) {
            query.where("classification", "like", "%" + classification + "%");
        }

        // 按图书ID正序排列
        return query.orderBy("book_id", OrderBy.ASC)
                .paginate(page, limit);
    }

    /**
     * 1.2 图书检索（按标题、读者、ISBN、分类进行模糊查询）
     */
    @Override
    public Paginate<Book> searchBooks(String keyword, Integer page, Integer limit) {
        if (!StringUtils.hasText(keyword)) {
            return bookModel.baseQuery()
                    .orderBy("book_id", OrderBy.ASC)
                    .paginate(page, limit);
        }

        String keywordPattern = "%" + keyword + "%";

        return bookModel.baseQuery()
                .where("title", "like", keywordPattern)
                .orWhere(w -> w.where("author", "like", keywordPattern))
                .orWhere(w -> w.where("isbn", "like", keywordPattern))
                .orWhere(w -> w.where("classification", "like", keywordPattern))
                .orderBy("book_id", OrderBy.ASC)
                .paginate(page, limit);
    }

    /**
     * 1.3 录入图书基本信息
     */
    @Override
    public Boolean addBook(Book book) {
        if (book.getInventory() == null) {
            book.setInventory(0);
        }
        if (book.getTotalBorrowingTime() == null) {
            book.setTotalBorrowingTime(0L);
        }

        int rows = bookModel.newQuery().data(book).insert();
        return rows > 0;
    }

    /**
     * 1.4 图书入库
     */
    @Override
    public Boolean stockIn(Integer bookId, String location) {
        var bookRecord = bookModel.baseQuery()
                .where("book_id", bookId)
                .limit(1)
                .first();

        if (bookRecord == null) {
            return false;
        }

        Book existingBook = bookRecord.getEntity();
        Integer currentInventory = existingBook.getInventory() != null ? existingBook.getInventory() : 0;

        int rows = bookModel.newQuery()
                .where("book_id", bookId)
                .data("inventory", currentInventory + 1)
                .update();

        return rows > 0;
    }

    /**
     * 1.5 图书下架
     */
    @Override
    public Boolean stockOut(Integer addressId) {
        var bookRecord = bookModel.baseQuery()
                .where("book_id", addressId)
                .limit(1)
                .first();

        if (bookRecord == null) {
            return false;
        }

        Book existingBook = bookRecord.getEntity();
        Integer currentInventory = existingBook.getInventory() != null ? existingBook.getInventory() : 0;

        if (currentInventory <= 0) {
            return false;
        }

        int rows = bookModel.newQuery()
                .where("book_id", addressId)
                .data("inventory", currentInventory - 1)
                .update();

        return rows > 0;
    }

    /**
     * 1.6 调整图书库存
     */
    @Override
    public Boolean adjustInventory(Integer bookId, Integer trueInventory) {
        if (trueInventory < 0) {
            return false;
        }

        var bookRecord = bookModel.baseQuery()
                .where("book_id", bookId)
                .limit(1)
                .first();

        if (bookRecord == null) {
            return false;
        }

        int rows = bookModel.newQuery()
                .where("book_id", bookId)
                .data("inventory", trueInventory)
                .update();

        return rows > 0;
    }

    /**
     * 1.8 查看图书封面图片
     * 从 bookimg 表查询
     */
    @Override
    public String getCoverImage(Integer bookId) {
        // 先查询图书是否存在
        var bookRecord = bookModel.baseQuery()
                .where("book_id", bookId)
                .limit(1)
                .first();

        if (bookRecord == null) {
            return null;
        }

        // 从 bookimg 表查询封面图片
        var imgRecord = bookImgModel.baseQuery()
                .where("book_id", bookId)
                .limit(1)
                .first();

        if (imgRecord == null) {
            return null;
        }

        BookImg bookImg = imgRecord.getEntity();
        return bookImg.getCoverImage();
    }

    /**
     * 1.9 删除图书封面图片
     * 从 bookimg 表删除记录
     */
    @Override
    public Boolean deleteCoverImage(Integer bookId) {
        // 先查询图书是否存在
        var bookRecord = bookModel.baseQuery()
                .where("book_id", bookId)
                .limit(1)
                .first();

        if (bookRecord == null) {
            return false;
        }

        // 从 bookimg 表删除该图书的封面图片记录
        int rows = bookImgModel.newQuery()
                .where("book_id", bookId)
                .delete();

        return rows > 0;
    }

    /**
     * 1.10 修改图书封面图片（如果不存在则新增）
     * 操作 bookimg 表
     */
    @Override
    public Boolean modifyCoverImage(Integer bookId, String coverImageUrl) {
        if (!StringUtils.hasText(coverImageUrl)) {
            return false;
        }

        // 先查询图书是否存在
        var bookRecord = bookModel.baseQuery()
                .where("book_id", bookId)
                .limit(1)
                .first();

        if (bookRecord == null) {
            return false;
        }

        // 查询 bookimg 表中是否已有该图书的封面图片记录
        var imgRecord = bookImgModel.baseQuery()
                .where("book_id", bookId)
                .limit(1)
                .first();

        if (imgRecord != null) {
            // 已有记录，更新封面图片URL
            int rows = bookImgModel.newQuery()
                    .where("book_id", bookId)
                    .data("cover_image", coverImageUrl)
                    .update();
            return rows > 0;
        } else {
            // 没有记录，新增一条
            BookImg bookImg = new BookImg();
            bookImg.setBookId(bookId);
            bookImg.setCoverImage(coverImageUrl);
            int rows = bookImgModel.newQuery().data(bookImg).insert();
            return rows > 0;
        }
    }

    /**
     * 1.11 查看图书标签列表
     * 从 booktag 表查询
     */
    @Override
    public List<String> getTags(Integer bookId) {
        // 先查询图书是否存在
        var bookRecord = bookModel.baseQuery()
                .where("book_id", bookId)
                .limit(1)
                .first();

        if (bookRecord == null) {
            return new ArrayList<>();
        }

        // 从 booktag 表查询该图书的所有标签
        var tagRecords = bookTagModel.baseQuery()
                .where("book_id", bookId)
                .get();

        List<String> tags = new ArrayList<>();
        if (tagRecords != null && !tagRecords.isEmpty()) {
            for (var record : tagRecords) {
                BookTag bookTag = record.getEntity();
                tags.add(bookTag.getTagName());
            }
        }

        return tags;
    }

    /**
     * 1.12 添加图书标签
     * 在 booktag 表中新增一条记录
     */
    @Override
    public Boolean addTag(Integer bookId, String tagName) {
        if (!StringUtils.hasText(tagName)) {
            return false;
        }

        // 先查询图书是否存在
        var bookRecord = bookModel.baseQuery()
                .where("book_id", bookId)
                .limit(1)
                .first();

        if (bookRecord == null) {
            return false;
        }

        // 检查标签是否已存在（避免重复添加）
        var existingTagRecord = bookTagModel.baseQuery()
                .where("book_id", bookId)
                .where("tag_name", tagName)
                .limit(1)
                .first();

        if (existingTagRecord != null) {
            // 标签已存在，无需重复添加
            return true;
        }

        // 在 booktag 表中新增一条标签记录
        BookTag bookTag = new BookTag();
        bookTag.setBookId(bookId);
        bookTag.setTagName(tagName);
        int rows = bookTagModel.newQuery().data(bookTag).insert();

        return rows > 0;
    }

    /**
     * 1.13 修改图书标签（清空原有标签，添加新标签列表）
     * 先删除 booktag 表中该图书的所有标签，再批量插入新标签
     */
    @Override
    public Boolean modifyTags(Integer bookId, List<String> tagNames) {
        if (tagNames == null || tagNames.isEmpty()) {
            return false;
        }

        // 先查询图书是否存在
        var bookRecord = bookModel.baseQuery()
                .where("book_id", bookId)
                .limit(1)
                .first();

        if (bookRecord == null) {
            return false;
        }

        // 先删除该图书的所有现有标签
        bookTagModel.newQuery()
                .where("book_id", bookId)
                .delete();

        // 再逐一插入新标签
        for (String tagName : tagNames) {
            if (StringUtils.hasText(tagName)) {
                BookTag bookTag = new BookTag();
                bookTag.setBookId(bookId);
                bookTag.setTagName(tagName.trim());
                bookTagModel.newQuery().data(bookTag).insert();
            }
        }

        return true;
    }

    /**
     * 1.14 删除图书标签
     * 从 booktag 表中删除指定的标签记录
     */
    @Override
    public Boolean deleteTag(Integer bookId, String tagName) {
        if (!StringUtils.hasText(tagName)) {
            return false;
        }

        // 先查询图书是否存在
        var bookRecord = bookModel.baseQuery()
                .where("book_id", bookId)
                .limit(1)
                .first();

        if (bookRecord == null) {
            return false;
        }

        // 从 booktag 表中删除指定的标签记录
        int rows = bookTagModel.newQuery()
                .where("book_id", bookId)
                .where("tag_name", tagName)
                .delete();

        return rows > 0;
    }
}
