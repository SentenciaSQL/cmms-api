package com.afriasdev.cmms.event;

import com.afriasdev.cmms.model.WorkOrder;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

/** Publicado por el scheduler cuando detecta una OT vencida. */
@Getter
public class WorkOrderOverdueEvent extends ApplicationEvent {

    private final WorkOrder workOrder;

    public WorkOrderOverdueEvent(Object source, WorkOrder workOrder) {
        super(source);
        this.workOrder = workOrder;
    }
}
