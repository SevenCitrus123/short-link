package project.service.impl;

import cn.hutool.core.util.StrUtil;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Service;
import project.service.UrlTitleService;

@Slf4j
@Service
public class UrlTitleServiceImpl implements UrlTitleService {

    private static final int TIMEOUT_MILLIS = 3000;

    static final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0 Safari/537.36";

    @Override
    public String getTitleByUrl(String url) {
        if (StrUtil.isBlank(url)) {
            return null;
        }
        try {
            Connection.Response response = Jsoup.connect(url)
                    .timeout(TIMEOUT_MILLIS)
                    .userAgent(USER_AGENT)
                    .followRedirects(true)
                    .execute();
            if (response.statusCode() != 200) {
                return null;
            }
            Document document = response.parse();
            String title = document.title();
            return StrUtil.isBlank(title) ? null : title;
        } catch (Exception ex) {
            log.warn("获取网站标题失败，url={}", url, ex);
            return null;
        }
    }
}