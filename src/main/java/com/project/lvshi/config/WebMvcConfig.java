package com.project.lvshi.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * @author AA
 * @version 1.0
 * @description: 图片、视频、文件拦截
 * @date 2024/2/26 19:50
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry){
        //歌手头像地址
        registry.addResourceHandler("/img/**").addResourceLocations(
                "file:"+System.getProperty("user.dir")+System.getProperty("file.separator")+"img"
                        +System.getProperty("file.separator")+System.getProperty("file.separator")
        );

        registry.addResourceHandler("/video/**").addResourceLocations(
                "file:"+System.getProperty("user.dir")+System.getProperty("file.separator")+"video"
                        +System.getProperty("file.separator")+System.getProperty("file.separator")
        );

        registry.addResourceHandler("/file/**").addResourceLocations(
                "file:"+System.getProperty("user.dir")+System.getProperty("file.separator")+"file"
                        +System.getProperty("file.separator")+System.getProperty("file.separator")
        );
    }

}

