package com.reggie.controller;

import com.reggie.common.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.UUID;

@RestController
@RequestMapping("/common")
@Slf4j
public class CommonUpDownController {
    @Value("${reggie.uploadPath}")
    private String basePath;

    //图片文件上传
    @PostMapping("/upload")
    public Result<String> upload(MultipartFile file) {

        //获取原始文件名
        String originalFilename = file.getOriginalFilename();
        //获取原始文件名的后缀
        String suffix = originalFilename.substring(originalFilename.lastIndexOf("."));
        //使用UUID重新生成文件名，防止文件名重复
        String fileName = UUID.randomUUID() + suffix;
        //如果文件夹不存在则创建
        File isNewDir = new File(basePath);
        if (!isNewDir.exists()) {
            //目录不存在则创建
            isNewDir.mkdirs();
        }
        //将文件转存到指定位置
        try {
            file.transferTo(new File(basePath + fileName));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return Result.success(fileName);
    }

    //图片文件下载，即回显
    @GetMapping("/download")
    public void download(String name, HttpServletResponse response) {
        try {
            //输入流，读取文件内容
            FileInputStream fileInputStream = new FileInputStream(new File(basePath + name));
            //设置相应类型
            response.setContentType("image/jpeg");
            //输出流，将读取的文件内容写回浏览器
            ServletOutputStream outputStream = response.getOutputStream();
            int len = 0;
            byte[] bytes = new byte[1024];
            while ((len = fileInputStream.read(bytes)) != -1) {
                outputStream.write(bytes, 0, len);
                outputStream.flush();
            }
            outputStream.close();
            fileInputStream.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
