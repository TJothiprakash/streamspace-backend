package com.jp.streamspace.vidoestream.service;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.jp.streamspace.vidoestream.config.RabbitConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class VideoProducer {

    private final RabbitTemplate rabbitTemplate;
    private final ObjectMapper mapper = new ObjectMapper();
    private final Logger logger = LoggerFactory.getLogger("VideoProducer.class");

    public VideoProducer(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void sendTranscodeJob(int videoId, String inputUrl, String outputPrefix) {
        try {
            Map<String, Object> payload = new HashMap<>();
            payload.put("videoId", videoId);
            payload.put("inputUrl", inputUrl);
            payload.put("outputPrefix", outputPrefix);

            String message = mapper.writeValueAsString(payload);
            logger.info(" sending job to queue : " + message);

            rabbitTemplate.convertAndSend(RabbitConfig.TRANSCODE_QUEUE, message);
            logger.info("  job sent  to queue : " + message);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
