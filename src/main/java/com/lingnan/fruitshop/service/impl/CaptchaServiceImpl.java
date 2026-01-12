package com.lingnan.fruitshop.service.impl;

import com.lingnan.fruitshop.service.CaptchaService;

import com.lingnan.fruitshop.common.exception.BizException;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.security.SecureRandom;
import java.time.Duration;
import java.util.Base64;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class CaptchaServiceImpl implements CaptchaService {

    private static final SecureRandom RANDOM = new SecureRandom();
    private static final char[] CHARS = "23456789ABCDEFGHJKLMNPQRSTUVWXYZ".toCharArray();
    private static final Duration EXPIRE = Duration.ofMinutes(5);

    private final Map<String, CaptchaEntry> store = new ConcurrentHashMap<>();

    @Override
    public CaptchaResult generate() {
        String code = randomCode(4);
        String key = UUID.randomUUID().toString();
        long expireAt = System.currentTimeMillis() + EXPIRE.toMillis();
        store.put(key, new CaptchaEntry(code, expireAt));

        String base64 = renderPngBase64(code);
        return new CaptchaResult(key, "data:image/png;base64," + base64);
    }

    @Override
    public boolean verify(String captchaKey, String captcha) {
        if (captchaKey == null || captchaKey.isBlank() || captcha == null || captcha.isBlank()) {
            return false;
        }

        CaptchaEntry entry = store.remove(captchaKey);
        if (entry == null) {
            return false;
        }
        if (System.currentTimeMillis() > entry.expireAt) {
            return false;
        }
        return entry.code.equalsIgnoreCase(captcha.trim());
    }

    private String randomCode(int len) {
        char[] buf = new char[len];
        for (int i = 0; i < len; i++) {
            buf[i] = CHARS[RANDOM.nextInt(CHARS.length)];
        }
        return new String(buf);
    }

    private String renderPngBase64(String code) {
        int width = 120;
        int height = 40;
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = image.createGraphics();
        try {
            g.setColor(Color.WHITE);
            g.fillRect(0, 0, width, height);

            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            for (int i = 0; i < 6; i++) {
                g.setColor(new Color(RANDOM.nextInt(200), RANDOM.nextInt(200), RANDOM.nextInt(200)));
                int x1 = RANDOM.nextInt(width);
                int y1 = RANDOM.nextInt(height);
                int x2 = RANDOM.nextInt(width);
                int y2 = RANDOM.nextInt(height);
                g.drawLine(x1, y1, x2, y2);
            }

            g.setFont(new Font("Arial", Font.BOLD, 24));
            for (int i = 0; i < code.length(); i++) {
                g.setColor(new Color(RANDOM.nextInt(80), RANDOM.nextInt(80), RANDOM.nextInt(80)));
                int x = 15 + i * 25 + RANDOM.nextInt(4);
                int y = 28 + RANDOM.nextInt(6);
                g.drawString(String.valueOf(code.charAt(i)), x, y);
            }

            try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
                ImageIO.write(image, "png", baos);
                return Base64.getEncoder().encodeToString(baos.toByteArray());
            }
        } catch (Exception e) {
            throw BizException.serverError("服务器内部错误");
        } finally {
            g.dispose();
        }
    }

    private record CaptchaEntry(String code, long expireAt) {
    }
}
