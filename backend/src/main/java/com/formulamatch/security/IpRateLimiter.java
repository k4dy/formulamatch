package com.formulamatch.security;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class IpRateLimiter {

    static final int DAILY_LIMIT = 100;
    private static final long DAY_MS = 24 * 60 * 60 * 1000L;

    private final ConcurrentHashMap<String, Deque<Long>> windows = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Integer> consecutiveDays = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, LocalDate> lastViolationDay = new ConcurrentHashMap<>();
    private final Set<String> banned = ConcurrentHashMap.newKeySet();

    public boolean tryConsume(String ip) {
        if (banned.contains(ip)) {
            return false;
        }
        long now = System.currentTimeMillis();
        Deque<Long> window = windows.computeIfAbsent(ip, k -> new ArrayDeque<>());
        synchronized (window) {
            while (!window.isEmpty() && now - window.peekFirst() > DAY_MS) {
                window.pollFirst();
            }
            if (window.size() >= DAILY_LIMIT) {
                return false;
            }
            window.addLast(now);
        }
        return true;
    }

    @Scheduled(cron = "0 0 0 * * *")
    public void checkConsecutiveViolators() {
        LocalDate today = LocalDate.now();
        LocalDate yesterday = today.minusDays(1);
        long now = System.currentTimeMillis();

        for (Map.Entry<String, Deque<Long>> entry : windows.entrySet()) {
            String ip = entry.getKey();
            Deque<Long> window = entry.getValue();
            long count;
            synchronized (window) {
                count = window.stream().filter(t -> now - t < DAY_MS).count();
            }
            if (count >= DAILY_LIMIT) {
                LocalDate lastDay = lastViolationDay.get(ip);
                if (yesterday.equals(lastDay)) {
                    int days = consecutiveDays.getOrDefault(ip, 0) + 1;
                    consecutiveDays.put(ip, days);
                    if (days >= 2) {
                        banned.add(ip);
                        windows.remove(ip);
                        consecutiveDays.remove(ip);
                        lastViolationDay.remove(ip);
                    } else {
                        lastViolationDay.put(ip, today);
                    }
                } else {
                    consecutiveDays.put(ip, 1);
                    lastViolationDay.put(ip, today);
                }
            } else {
                consecutiveDays.remove(ip);
                lastViolationDay.remove(ip);
            }
        }
    }
}
