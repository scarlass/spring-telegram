package dev.scaraz.lib.spring.telegram.config;

import dev.scaraz.lib.spring.telegram.bind.enums.UpdateType;
import dev.scaraz.lib.spring.telegram.config.process.UpdateProcessor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

@Slf4j
@Component
@RequiredArgsConstructor
class TelegramUpdateProcessorRegistry implements BeanPostProcessor {

    private final MultiValueMap<UpdateType, UpdateProcessor> processors = new LinkedMultiValueMap<>();

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        if (bean instanceof UpdateProcessor processor) {
            UpdateType type = processor.type();
            processors.add(type, processor);
            processors.get(type).sort(AnnotationAwareOrderComparator.INSTANCE);
        }
        return BeanPostProcessor.super.postProcessAfterInitialization(bean, beanName);
    }

    public UpdateProcessor getProcessor(UpdateType type) {
        if (type == null) return null;
        return processors.get(type).getLast();
    }

}
