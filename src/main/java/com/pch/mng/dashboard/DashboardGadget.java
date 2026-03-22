package com.pch.mng.dashboard;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
@Entity
@Table(name = "dashboard_gadget_tb")
public class DashboardGadget {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dashboard_id", nullable = false)
    private Dashboard dashboard;

    @Column(nullable = false)
    private String gadgetType;

    @Column(nullable = false)
    private int position;

    @Column(columnDefinition = "TEXT")
    private String configJson;

    @Builder
    public DashboardGadget(Dashboard dashboard, String gadgetType, int position, String configJson) {
        this.dashboard = dashboard;
        this.gadgetType = gadgetType;
        this.position = position;
        this.configJson = configJson;
    }
}
