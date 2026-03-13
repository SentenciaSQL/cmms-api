package com.afriasdev.cmms.event;

import com.afriasdev.cmms.security.model.User;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

/** Publicado cuando se registra un nuevo usuario (bienvenida). */
@Getter
public class UserRegisteredEvent extends ApplicationEvent {

    private final User user;

    public UserRegisteredEvent(Object source, User user) {
        super(source);
        this.user = user;
    }
}
