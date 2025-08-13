package com.tiffino.service;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class OtpService {

    private static final Integer EXPIRE_MINS = 1440;  //--> 24 hours × 60 minutes = 1440 minutes

    private LoadingCache<String, Integer> cache;

    public OtpService() {
        super();
        cache = CacheBuilder.newBuilder().expireAfterWrite(EXPIRE_MINS, TimeUnit.MINUTES).build(new CacheLoader<String, Integer>() {
            @Override
            public Integer load(String s) throws Exception {
                return 0;
            }
        });
    }

    public int generateOTP(String email) {
        int max = 999999;
        int min = 100000;
        int otp = (int) (Math.random() * (max - min + 1) + min);

        cache.put(email, otp);
        return otp;
    }

    public int getOtp(String email) {
        try {
            log.debug("Get OTP By Email " + email);
            System.out.println("Otp stored in cache "+cache.get(email));
            return cache.get(email);
        } catch (Exception e) {
            log.error("No Otp Found For The Email = " + email);
            return 0;
        }
    }

    public void clearOTP(String email) {
        cache.invalidate(email);
    }
}
