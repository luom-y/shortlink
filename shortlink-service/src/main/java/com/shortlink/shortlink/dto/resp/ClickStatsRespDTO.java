package com.shortlink.shortlink.dto.resp;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClickStatsRespDTO {

    private String shortCode;

    private String statsDate;

    private Long pv;

    private Long uv;

    private Long ipCount;
}
