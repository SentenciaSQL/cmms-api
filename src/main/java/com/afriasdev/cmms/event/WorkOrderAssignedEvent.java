package com.afriasdev.cmms.event;

import com.afriasdev.cmms.model.WorkOrder;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

/** Publicado cuando se asigna (o reasigna) un técnico a una OT. */
@Getter
public class WorkOrderAssignedEvent extends ApplicationEvent {

    private final WorkOrder workOrder;

    public WorkOrderAssignedEvent(Object source, WorkOrder workOrder) {
        super(source);
        this.workOrder = workOrder;
    }
}
