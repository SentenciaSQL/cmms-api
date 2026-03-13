package com.afriasdev.cmms.event;

import com.afriasdev.cmms.model.WorkOrder;
import com.afriasdev.cmms.model.WorkOrderStatus;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

/** Publicado cuando cambia el estado de una OT. */
@Getter
public class WorkOrderStatusChangedEvent extends ApplicationEvent {

    private final WorkOrder workOrder;
    private final WorkOrderStatus previousStatus;
    private final WorkOrderStatus newStatus;

    public WorkOrderStatusChangedEvent(Object source, WorkOrder workOrder,
                                       WorkOrderStatus previousStatus, WorkOrderStatus newStatus) {
        super(source);
        this.workOrder     = workOrder;
        this.previousStatus = previousStatus;
        this.newStatus      = newStatus;
    }
}
